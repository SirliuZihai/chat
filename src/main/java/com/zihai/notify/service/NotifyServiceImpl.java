package com.zihai.notify.service;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.zihai.util.MongoUtil;

public class NotifyServiceImpl implements NotifyService {

	@Override
	public Document countNofify(String currentUser) {
		List<Document> pipeline = new ArrayList<Document>();
		pipeline.add(Document.parse(String.format("{$match:{receiver:'%s',state:0}}", currentUser)));
		pipeline.add(Document.parse(" $group: {_id:'$type',count: { $sum: 1 }}"));
		Document result = MongoUtil.getCollection("notify").aggregate(pipeline).first();
		return result;
	}

	@Override
	public List<Document> queryNofify(Document filter) {
		List<Document> pipeline = new ArrayList<Document>();
		if("notes".equals(filter.getString("type"))){
			pipeline.add(Document.parse(String.format("{$match:{receiver:'%s'，type:{$lte:4}}}",filter.getString("username"))));
		}else if("operate".equals(filter.getString("type"))){
			pipeline.add(Document.parse(String.format("{$match:{receiver:'%s'，type:{$gte:5}}}",filter.getString("username"))));
		}else{
			return new ArrayList<Document>();
		}
		pipeline.add(new Document("$skip",filter.getInteger("skipNum")));
		pipeline.add(new Document("$limit",20));
		List<Document> result  = new ArrayList<Document>();
		result = MongoUtil.getCollection("notify").aggregate(pipeline).into(result);
		return result;
	}

	@Override
	public void addNotify(Document doc) {
		MongoUtil.getCollection("notify").insertOne(doc);
	}

	@Override
	public void ignoreNotify(Integer type ,String username) {
		Document filter = new Document("receiver",username).append("type", type);
		Document update = Document.parse("{$set:{state:1}}");
		MongoUtil.getCollection("notify").updateMany(filter,update);
	}

	@Override
	public void stateNotify(String id, Integer state,String receiver) {
		Document filter = new Document("_id",new ObjectId(id)).append("receiver",receiver);
		Document update = Document.parse(String.format("{$set:{state:%d}}",state));
		MongoUtil.getCollection("notify").updateOne(filter,update);	
	}

}
