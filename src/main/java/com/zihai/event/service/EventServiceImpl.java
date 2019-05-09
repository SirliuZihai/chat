package com.zihai.event.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.Block;
import com.zihai.util.BusinessException;
import com.zihai.util.MongoUtil;
import com.zihai.websocket.EventChatHandler;
import com.zihai.websocket.HomeEventHandler;

@Service("eventService")
public class EventServiceImpl implements EventService {
	private final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);
	
	@Autowired
	private HomeEventHandler homeEventHandler;
	
	@Autowired
	private EventChatHandler eventChatHandler;

	@Override
	public void insertMessage(Document message) throws IOException {
		//insert message 
		MongoUtil.getCollection("message").insertOne(message.append("_id", new ObjectId()));
		//insert event_queue
		List<Document> list = new ArrayList<Document>();
		if(CollectionUtils.isEmpty(message.getList("receiver", String.class))){
			//send all
			Document e = MongoUtil.getCollection("event").find(new Document("_id",message.getObjectId("relateId"))).first();
			List<String> relationship = e.getList("relationship", String.class);
			relationship.add(e.getString("username"));
			if(CollectionUtils.isNotEmpty(relationship)){
				for(String other : relationship){
					list.add(new Document("_id", new ObjectId()).append("username", other).append("eventId", message.getObjectId("_id")).append("type", 1).append("state", 0));
				}
			}
		}else{
			//send to yourself
			list.add(new Document("_id", new ObjectId()).append("username",message.getString("sender")).append("eventId", message.getObjectId("_id")).append("type", 1).append("state", 0));
			//send to receiver in events
			for(String other : message.getList("receiver", String.class)){
				list.add(new Document("_id", new ObjectId()).append("username", other).append("eventId",message.getObjectId("_id")).append("type", 1).append("state", 0));
			}
		}
		MongoUtil.getCollection("event_queue").insertMany(list);
		//have a try to send event directory
		message.put("_id", message.getObjectId("_id").toHexString());
		message.put("relateId", message.getObjectId("relateId").toHexString());
		for(Document send : list){
			WebSocketSession session;
			//is in chat room 
			session = eventChatHandler.clients.get(message.getString("relateId")+"|"+(String)send.get("username"));
			//is online in homeview
			if(session == null)
				session = homeEventHandler.clients.get((String)send.get("username"));
			if(session!=null){
				List list_event = new ArrayList<Document>();	
				list_event.add(message);
				String result = new ObjectMapper().writeValueAsString(list_event);				
				session.sendMessage(new TextMessage("0001"+result));	
				updateEventState(send);
			}
		}

	}
	@Override
	public Document getEventById(String eventId) {
		 Document event = new Document("_id",new ObjectId(eventId));
		 return (Document) MongoUtil.Query(event, null, Document.class, "event").get(0);
	}
	@Override
	public List queryHistroy(Map filter1,String username) {
		 Document filter = new Document();
		 Set<Document> and_set = new HashSet<Document>();
		 if(!StringUtils.isEmpty((String)filter1.get("title"))){
			 and_set.add(new Document("title", new Document("$regex",(String)filter1.get("title")).append("$options", "si")));
		 }
		 //关联人
		Set<Document> or_set = new HashSet<Document>();
		or_set.add(new Document("username", username));
		List<String> re_list = new ArrayList<String>();
		re_list.add(username);
		or_set.add(new Document("relationship", re_list));
		and_set.add(new Document("$or", or_set));
		//查询时间
		Set<Document> filter_time = new HashSet<Document>();
		 String temp;
		 if(!StringUtils.isEmpty(temp =(String)filter1.get("endtime"))){
			 filter_time.add(new Document("starttime",new Document("$lte",temp)));
		 }
		 if(!StringUtils.isEmpty(temp = (String)filter1.get("starttime"))){
			 filter_time.add(new Document("endtime",new Document("$gte",temp)));
		 }	 
		 if(filter_time.size()>0)
			 and_set.add(new Document("$and", filter_time));
		 Document filter2 = new Document("$and",and_set);
		return MongoUtil.Query(filter2, null, Document.class, "event");
	}
	@Override
	public List<Document> queryNoSendEvent(String username) {
		//event push
		Document filter = new Document("username", username).append("state", 0).append("type", 0);
		List<Bson> criteria = new ArrayList<Bson>();
		criteria.add(new Document().append("$lookup", new Document().append("from", "event")
				.append("localField", "eventId").append("foreignField", "_id").append("as", "e")));
		criteria.add(new Document().append("$match", filter));
		criteria.add(new Document().append("$unwind",new Document().append("path", "$e").append("preserveNullAndEmptyArrays", true)));
		criteria.add(new Document().append("$sort",new Document().append("e._id", 1)));
		criteria.add(new Document().append("$project",new Document().append("e", 1).append("_id", 0)));
		log.info("queryNoSendEvent filter1 ==="+ JSON.toJSONString(criteria)); //956518959 
		List<Document> l = new ArrayList<Document>();
		Block<Document> block = new Block<Document>() {
			@Override
		       public void apply(final Document document) {
				Document e =(Document) document.get("e");
				e.put("_id", e.getObjectId("_id").toHexString());
				l.add(e);
		       }
		};
		MongoUtil.getCollection("event_queue").aggregate(criteria).forEach(block);				
		log.info("queryNoSendEvent result ==="+ JSON.toJSONString(l)); 
		return l;
	}
	@Override
	public List<Document> queryNoSendMessage(String username) {
		//event push
		Document filter = new Document("username", username).append("state", 0).append("type", 1);
		List<Document> criteria = new ArrayList<Document>();
		criteria.add(new Document().append("$lookup", new Document().append("from", "message")
				.append("localField", "eventId").append("foreignField", "_id").append("as", "m")));
		criteria.add(new Document().append("$match", filter));
		criteria.add(new Document().append("$unwind",new Document().append("path", "$m").append("preserveNullAndEmptyArrays", true)));
		criteria.add(new Document().append("$sort",new Document().append("m._id", 1)));
		criteria.add(new Document().append("$project",new Document().append("m", 1).append("_id", 0)));
		log.info("queryNoSendMessage filter1 ==="+ JSON.toJSONString(criteria)); 
		List<Document> l = new ArrayList<Document>();
		Block<Document> block = new Block<Document>() {
			@Override
		       public void apply(final Document document) {
				Document m =(Document) document.get("m");
				if(m != null){
					m.put("_id", m.getObjectId("_id").toHexString());
					m.put("relateId", m.getObjectId("relateId").toHexString());
					l.add(m);
				}
		       }
		};
		MongoUtil.getCollection("event_queue").aggregate(criteria).forEach(block);				
		log.info("queryNoSendMessage result ==="+JSON.toJSONString(l));
		return l;
	}
	@Override
	public void save(Document event) throws IOException {
		String username = event.getString("username");
		event.remove("num");
		String message; //save or update
 		if(!event.containsKey("_id")){
			event.append("_id", new ObjectId());
			MongoUtil.getCollection("event").insertOne(event);
			message = username+"新建了该事件";
		}else{
			event.put("_id", new ObjectId(event.getString("_id")));
			Document theupdate = MongoUtil.getCollection("event").findOneAndReplace(new Document("_id", event.getObjectId("_id")), event);				
			if(theupdate == null)
				throw new BusinessException("该记录已被移除");
			message = username+"更新了该事件";
		}
		//add notify others and self into event_queue
		List<Document> list = new ArrayList<Document>();
		if(!CollectionUtils.isEmpty(event.getList("relationship", String.class)))
		for(String other : event.getList("relationship", String.class)){
			list.add(new Document().append("_id", new ObjectId()).append("username", other).append("eventId", event.get("_id")).append("type", 0).append("state", 0));
		}
		list.add(new Document().append("_id", new ObjectId()).append("username", (String)event.get("username")).append("eventId", event.get("_id")).append("type", 0).append("state", 0));
		MongoUtil.getCollection("event_queue").insertMany(list);
		
		//have a try to send event directory
		event.put("_id", event.getObjectId("_id").toHexString());
		for(Document send : list){
			WebSocketSession session = homeEventHandler.clients.get((String)send.get("username"));
			if(session!=null){
				List list_event = new ArrayList<Document>();
				list_event.add(event);
				String result = new ObjectMapper().writeValueAsString(list_event);				
				session.sendMessage(new TextMessage("0000"+result));	
				updateEventState(send);
			}
		}
		insertMessage(new Document("relateId",new ObjectId(event.getString("_id"))).append("data", message).append("type", "operate").append("sender", username));

	}

	@Override
	public void updateEventState(Document filter) {
		MongoUtil.getCollection("event_queue").updateMany(filter, new Document().append("$set",new Document("state",1)));		
	}

	@Override
	public void deleteEvent(Document event) {
		Subject currentUser = SecurityUtils.getSubject();
		String username = (String)currentUser.getPrincipal();		
		if(((String)event.get("username")).equals(username)){
			if(MongoUtil.getCollection("event").findOneAndDelete(event)==null){
				throw new BusinessException("该记录已被移除");
			}
		}else{
			List<String> relation = event.getList("relationship", String.class);
			List relationShip = new ArrayList<String>();
			for(String name : relation){
				if(!name.equals(username))
					relationShip.add(name);
			}
			MongoUtil.getCollection("event").findOneAndUpdate(event, new Document().append("relationship", relationShip));
		}
	}
	public static void main(String[] args) {
		new EventServiceImpl().queryNoSendMessage("yanzi");	
	}

	

}
