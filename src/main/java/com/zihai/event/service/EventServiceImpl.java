package com.zihai.event.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import com.zihai.websocket.test.HomeEventHandler;

@Service("eventService")
public class EventServiceImpl implements EventService {
	private final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);
	
	@Autowired
	private HomeEventHandler homeEventHandler;

	@Override
	public void InsertMessage(Map message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateState(Map map) {

	}

	@Override
	public List queryHistroy(Map filter1,String username) {
		 Document filter = new Document();
		 Document filter_time = new Document();
		 if(!StringUtils.isEmpty((String)filter1.get("starttime"))){
			 ObjectId startId = new ObjectId((String)filter1.get("starttime"));
			 filter_time.append("$gt",startId);
		 }
		 if(!StringUtils.isEmpty((String)filter1.get("starttime"))){
			 ObjectId endId = new ObjectId((String)filter1.get("endtime"));
			 filter_time.append("$lt", endId);
		 }
		 if(filter_time.size()>0)
			 filter.append("_id", filter_time);
		 if(!StringUtils.isEmpty((String)filter1.get("title"))){
			 filter.append("title", new Document("$regex",(String)filter1.get("title")).append("$options", "si"));		 }
		 
		Set<Document> or_set = new HashSet<Document>();
		or_set.add(new Document("username", username));
		List<String> re_list = new ArrayList<String>();
		re_list.add(username);
		or_set.add(new Document("relationship", re_list));
		filter.append("$or", or_set);
		return MongoUtil.Query(filter, null, Document.class, "event");
	}
	@Override
	public List<Document> queryNoSendEvent(String username) {
		Document filter = new Document().append("username", username).append("state", 0);
		List<Bson> criteria = new ArrayList<Bson>();
		criteria.add(new Document().append("$lookup", new Document().append("from", "event")
				.append("localField", "eventId").append("foreignField", "_id").append("as", "e")));
		criteria.add(new Document().append("$match", filter));
		criteria.add(new Document().append("$unwind",new Document().append("path", "$e").append("preserveNullAndEmptyArrays", true)));
		criteria.add(new Document().append("$sort",new Document().append("e._id", 1)));
		criteria.add(new Document().append("$project",new Document().append("e", 1).append("_id", 0)));
		System.out.println(JSON.toJSONString(criteria));
		List<Document> l = new ArrayList<Document>();
		Block<Document> block = new Block<Document>() {
			@Override
		       public void apply(final Document document) {
				l.add((Document) document.get("e"));
		       }
		};
		MongoUtil.getCollection("event_queue").aggregate(criteria).forEach(block);;
		System.out.println(JSON.toJSONString(l));		
		return l;
	}

	@Override
	public void save(Document event) throws IOException {
		//save or update
		if(!event.containsKey("_id")){
			event.append("_id", new ObjectId());
			MongoUtil.getCollection("event").insertOne(event);
		}else{
			Object old_id = event.get("_id");
			event.put("_id", new ObjectId());
			Document theupdate = MongoUtil.getCollection("event").findOneAndUpdate(new Document().append("_id", MongoUtil.getObjectId((Map)old_id)), new Document("$set",event));	
			if(theupdate == null)
				throw new BusinessException("该记录已被移除");
		}
		//add notify others and self into event_queue
		List<Document> list = new ArrayList<Document>();
		if(!CollectionUtils.isEmpty((Collection<?>) event.get("relationship")))
		for(String other : event.getList("relationship", String.class)){
			list.add(new Document().append("_id", new ObjectId()).append("username", other).append("eventId", event.get("_id")).append("state", 0));
		}
		list.add(new Document().append("_id", new ObjectId()).append("username", (String)event.get("username")).append("eventId", event.get("_id")).append("state", 0));
		MongoUtil.getCollection("event_queue").insertMany(list);
		
		//have a try to send event directory
		for(Document send : list){
			WebSocketSession session = homeEventHandler.clients.get((String)send.get("username"));
			if(session!=null){
				List list_event = new ArrayList<Document>();
				list_event.add(event);
				String result = new ObjectMapper().writeValueAsString(list_event);				
				session.sendMessage(new TextMessage(result));	
				updateEventState(send);
			}
		}
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
		//new EventServiceImpl().queryNoSendEvent("yanzi",1);
		Document filter1 = new Document();
		filter1.put("title", "test22");
		Document id = (Document) MongoUtil.Query(filter1, new Document("_id",1), Document.class, "event").get(0);
		String hex_s ;
		System.out.println(hex_s = JSON.toJSONString(id.get("_id")));
		System.out.println(id.getObjectId("_id").toHexString());
		System.out.println(JSON.parseObject(hex_s, ObjectId.class).toHexString());
	}

}
