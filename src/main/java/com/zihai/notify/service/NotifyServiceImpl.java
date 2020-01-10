package com.zihai.notify.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.alibaba.fastjson.JSON;
import com.zihai.util.BusinessException;
import com.zihai.util.MongoUtil;
import com.zihai.websocket.HomeEventHandler;
@Service
public class NotifyServiceImpl implements NotifyService {
	private final Logger log = LoggerFactory.getLogger(getClass());
	@Autowired
	private HomeEventHandler homeEventHandler;

	@Override
	public List<Document> countNofify(String currentUser) {
		List<Document> pipeline = new ArrayList<Document>();
		pipeline.add(Document.parse(String.format("{$match:{receiver:'%s',state:0}}", currentUser)));
		pipeline.add(Document.parse(" {$group: {_id:'$type',count: { $sum: 1 }}}"));
		ArrayList<Document> l = new ArrayList<Document>();
		MongoUtil.getCollection("notify").aggregate(pipeline).into(l);
		return l;
	}

	@Override
	public List<Document> queryNofify(Document filter) {
		List<Document> pipeline = new ArrayList<Document>();
		Integer type = filter.getInteger("type");
		if(type>=5){
			if(type == 5){ //申请的
				pipeline.add(Document.parse(String.format("{$match:{sender:'%s',type:{$gte:5}}}",filter.getString("username"))));
			}else if(filter.getInteger("type") == 6){//待处理的
				pipeline.add(Document.parse(String.format("{$match:{receiver:'%s',type:{$gte:5}}}",filter.getString("username"))));
			}else {
				throw new BusinessException("无效参数");
			}
			pipeline.add(Document.parse("{'$lookup': {'as': 'e_list','foreignField': '_id','from': 'event','localField': 'relateId'}}"));			
			pipeline.add(Document.parse("{'$unwind': {'path': '$e_list','preserveNullAndEmptyArrays': true}}"));
			pipeline.add(new Document("$addFields",Document.parse("{e_title:'$e_list.title'}")));
		}else{
			if(type == 0||type ==1 ){ //通知的
				pipeline.add(Document.parse(String.format("{$match:{receiver:'%s',type:{$lte:4}}}}",filter.getString("username"))));
			}else {
				throw new BusinessException("无效参数");
			}
		}
		
		pipeline.add(new Document("$sort",new Document("_id",-1)));
		pipeline.add(new Document("$addFields",Document.parse("{_id:{$toString:'$_id'},relateId:{$toString:'$relateId'}}")));
		pipeline.add(new Document("$skip",filter.getInteger("skipNum")));
		pipeline.add(new Document("$limit",20));
		log.info(JSON.toJSONString(pipeline));
		List<Document> result  = new ArrayList<Document>();
		result = MongoUtil.getCollection("notify").aggregate(pipeline).into(result);
		return result;
	}

	@Override
	public void addNotify(Document doc) {
		MongoUtil.getCollection("notify").insertOne(doc);
		WebSocketSession session = homeEventHandler.clients.get(doc.getString("receiver"));
		try {
			if(session != null)
				session.sendMessage(new TextMessage("0004"));
		} catch (IOException e) {
			log.error(e.getMessage());
		}	
	}

	@Override
	public void ignoreNotify(Integer type ,String username) {
		Document filter = new Document("receiver",username).append("state", 0).append("type", type);
		Document update = Document.parse("{$set:{state:1}}");
		log.info(JSON.toJSONString(filter));
		MongoUtil.getCollection("notify").updateMany(filter,update);
	}

	@Override
	public void stateNotify(String id, Integer state,String receiver) {
		Document filter = new Document("_id",new ObjectId(id)).append("receiver",receiver);
		Document update = Document.parse(String.format("{$set:{state:%d}}",state));
		log.info(JSON.toJSONString(filter));
		MongoUtil.getCollection("notify").updateOne(filter,update);	
	}

	@Override
	public Document getNotify(Document filter) {
		return MongoUtil.getCollection("notify").find(filter).first();
	}

}
