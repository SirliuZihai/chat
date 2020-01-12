package com.zihai.event.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.RuntimeErrorException;

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
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.Block;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.zihai.notify.service.NotifyService;
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
	@Autowired
	private NotifyService notifyService;
	
	/**
	 * 
	 * message	
		username
		relateId
		sender
		receiver  接收list
		data
		type
	 * */
	@Override
	public void insertMessage(Document message) {
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
			//对部分人操作的指令不包括自，比如删除某人
			if(!"operate".equals(message.getString("type")))
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
				
				try {
					String result = new ObjectMapper().writeValueAsString(list_event);				
					session.sendMessage(new TextMessage("0001"+result));
				} catch (JsonProcessingException e) {
					new RuntimeException(e);
				} catch (IOException e) {
					new RuntimeException(e);
				}	
				updateEventState(send);
			}
		}

	}
	/**
	 * 用户是否存在于事件中
	 * */
	@Override
	public boolean hasInEvent(ObjectId eventId,String sender){
		ArrayList<String> senders = new ArrayList<String>();
		senders.add(sender);
		ArrayList<Document> criteria = new ArrayList<Document>();
		criteria.add(new Document("username",sender));
		criteria.add(new Document("relationship",new Document("$in",senders)));
		Document d = MongoUtil.getCollection("event").find(new Document("_id",eventId).append("$or",criteria)).first();
		return d!=null;
	}
	@Override
	public Document getEventById(String eventId) {
		 Document event = new Document("_id",new ObjectId(eventId));
		 List result = MongoUtil.Query(event, null, Document.class, "event");
		 if(result.size()>0){
			 return (Document)result.get(0);
		 }
		 return null;
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
				if(e !=null){
					e.put("_id", e.getObjectId("_id").toHexString());
					l.add(e);
				}
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
	public void save(Document event) {
		String username = event.getString("username");
		event.remove("num");
		String message; //save or update
		List<String> rela = new ArrayList<String>(event.getList("relationship", String.class));
 		if(!event.containsKey("_id")){
			event.append("_id", new ObjectId());
			//邀请所有
			inviteOther(username,event.getObjectId("_id").toHexString(),rela); 
			//去除所有
			event.put("relationship", new ArrayList<String>());
			MongoUtil.getCollection("event").insertOne(event);
			message = username+"新建了该事件";
		}else{
			event.put("_id", new ObjectId(event.getString("_id")));
			Document criteria = new Document("_id", event.getObjectId("_id")).append("username",username);
			Document f_event = MongoUtil.getCollection("event").find(criteria).first();
			//邀请新人员
			rela.removeAll(f_event.getList("relationship", String.class));
			inviteOther(username,event.getObjectId("_id").toHexString(),rela); 
			//去除新人员
			event.getList("relationship", String.class).removeAll(rela);
			Document theupdate = MongoUtil.getCollection("event").findOneAndReplace(criteria, event);				
			if(theupdate == null)
				throw new BusinessException("该记录已被移除");
			//获取删除人员的记录
			List<String> deletePeple = f_event.getList("relationship", String.class);
			deletePeple.removeAll(event.getList("relationship", String.class));
			if(!CollectionUtils.isEmpty(deletePeple)){
				insertMessage(new Document("relateId",f_event.getObjectId("_id")).append("data", "您被移除了该事件").append("type", "operate")
						.append("sender", username).append("receiver", deletePeple));

			}
			message = username+"更新了该事件";
		}
 		sendHomeEvent(event);
		insertMessage(new Document("relateId",new ObjectId(event.getString("_id"))).append("data", message).append("type", "operate").append("sender", username));
	}
	@Override
	public void saveAndRelate(Document event){
		String username = event.getString("username");
		event.remove("num");
		String message; //save or update
		List<String> rela = new ArrayList<String>(event.getList("relationship", String.class));
 		if(!event.containsKey("_id")){
			event.append("_id", new ObjectId());
			MongoUtil.getCollection("event").insertOne(event);
			message = username+"新建了该事件";
		}else{
			event.put("_id", new ObjectId(event.getString("_id")));
			Document criteria = new Document("_id", event.getObjectId("_id")).append("username",username);
			Document f_event = MongoUtil.getCollection("event").find(criteria).first();
			Document theupdate = MongoUtil.getCollection("event").findOneAndReplace(criteria, event);				
			if(theupdate == null)
				throw new BusinessException("该记录已被移除");
			//获取删除人员的记录
			List<String> deletePeple = f_event.getList("relationship", String.class);
			deletePeple.removeAll(event.getList("relationship", String.class));
			if(!CollectionUtils.isEmpty(deletePeple)){
				insertMessage(new Document("relateId",f_event.getObjectId("_id")).append("data", "您被移除了该事件").append("type", "operate")
						.append("sender", username).append("receiver", deletePeple));
			}
			message = username+"更新了该事件";
		}
 		sendHomeEvent(event);
		insertMessage(new Document("relateId",new ObjectId(event.getString("_id"))).append("data", message).append("type", "operate").append("sender", username));
	}
		
	
	@Override
	public void addRelation(String _id,String people,String username) {
		String message =people +"加入了事件中";
		Document filter = new Document("_id",new ObjectId(_id)).append("username", username);
		Document update = new Document("$addToSet",new Document("relationship",people));
		UpdateResult result = MongoUtil.getCollection("event").updateOne(filter, update);
		if(result.isModifiedCountAvailable()&&result.getModifiedCount()>0){
			Document event = getEventById(_id);
			sendHomeEvent(event);
			insertMessage(new Document("relateId",new ObjectId(_id)).append("data", message).append("type", "operate").append("sender", username));
		}else{
			throw new BusinessException("您无权限修改该事件");
		}
	}
	/**
	 * evet id: OjectId
	 * */
	private void sendHomeEvent(Document event ){
		//add notify others and self into event_queue
		List<Document> list = new ArrayList<Document>();
		if(!CollectionUtils.isEmpty(event.getList("relationship", String.class)))
		for(String other : event.getList("relationship", String.class)){
			list.add(new Document().append("_id", new ObjectId()).append("username", other).append("eventId", event.get("_id")).append("type", 0).append("state", 0));
		}
		list.add(new Document().append("_id", new ObjectId()).append("username", (String)event.get("username")).append("eventId", event.get("_id")).append("type", 0).append("state", 0));
		MongoUtil.getCollection("event_queue").insertMany(list);
		
		//have a try to send event directory
		if(event.get("_id") instanceof ObjectId){
			event.put("_id", event.getObjectId("_id").toHexString());
		}
		for(Document send : list){
			WebSocketSession session = homeEventHandler.clients.get((String)send.get("username"));
			if(session!=null){
				List list_event = new ArrayList<Document>();
				list_event.add(event);
				try {
					String result = new ObjectMapper().writeValueAsString(list_event);				
					session.sendMessage(new TextMessage("0000"+result));
				} catch (Exception e) {
					new RuntimeException(e);
				}
				updateEventState(send);
			}
		}	
	}
	
	private void inviteOther(String username,String event_id,List<String> rela){
		//if has relationship ,clear and notify it 邀请
		if((!CollectionUtils.isEmpty(rela))&&(!"nicool".equals(username))){
			for(String other : rela){
				Document d =  new Document("relateId",new ObjectId(event_id))
						.append("sender",username).append("receiver", other)
						 .append("state", 0).append("type", 6);
				 		notifyService.addNotify(d);
			}
			log.info("已邀请:"+JSON.toJSONString(rela));
		}
	}
	@Override
	public void updateEventState(Document filter) {
		MongoUtil.getCollection("event_queue").updateMany(filter, new Document().append("$set",new Document("state",1)));		
	}

	@Override
	public void deleteEvent(String username ,String eventId) {
		Document event = getEventById(eventId);
		if(event == null||StringUtils.isEmpty(event.getString("username"))){
			throw new BusinessException("该事件已被移除");
		}
		event.put("_id", new ObjectId(event.getString("_id")));
		List<String> relationship = new ArrayList<String>(); //待通知对象
		String message = null; //通知内容
		if(event.getString("username").equals(username)){			
			relationship = event.getList("relationship", String.class);
			if(CollectionUtils.isEmpty(relationship)){
				MongoUtil.getCollection("event").findOneAndUpdate(new Document("_id",event.getObjectId("_id")), new Document("$set",new Document("username", "")));
			}else{
				String newAdmin = relationship.remove(0);
				MongoUtil.getCollection("event").findOneAndUpdate(new Document("_id",event.getObjectId("_id")), new Document("$set",new Document("username", newAdmin).append("relationship", relationship)));
				message = username + "退出事件,新的管理员为："+newAdmin;
			}
			
			/*//删除相关消息
			DeleteResult result = MongoUtil.getCollection("message").deleteMany(new Document("relateId",event.getObjectId("_id")));
			log.info("删除相关消息数量："+result.getDeletedCount());
			//删除相关动态
			DeleteResult result2 =MongoUtil.getCollection("tips").deleteMany(new Document("eventId",event.getObjectId("_id")));
			log.info("删除相关动态数量："+result2.getDeletedCount());*/
		}else{
			List<String> relation = event.getList("relationship", String.class);
			for(String name : relation){
				if(!name.equals(username))
					relationship.add(name);
			}
			message = username + "退出事件";
			MongoUtil.getCollection("event").findOneAndUpdate(event, new Document("$pull",new Document("relationship", username)));
		}
		
		//通知其他人
		try {
			insertMessage(new Document("relateId",event.getObjectId("_id")).append("sender",username).append("receiver",relationship).append("data",message).append("type", "operate"));
		} catch (Exception e) {
			new RuntimeException(e);
		}
	}
	public static void main(String[] args) {
		new EventServiceImpl().queryNoSendMessage("yanzi");	
	}
	
	

	

}
