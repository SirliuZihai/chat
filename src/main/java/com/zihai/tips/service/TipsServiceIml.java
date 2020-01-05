package com.zihai.tips.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.mongodb.Block;
import com.zihai.util.MongoUtil;
@Service
public class TipsServiceIml implements TipsService{
	private final Logger log = LoggerFactory.getLogger(TipsServiceIml.class);

	@Override
	public void addTip(Document document) {
		ObjectId id = new ObjectId();
		document.put("_id", id);
		document.put("eventId", new ObjectId(document.getString("eventId")));
		MongoUtil.getCollection("tips").insertOne(document);		
		MongoUtil.getCollection("support").insertOne(new Document("_id",id).append("type", 0).append("supportPeople", new ArrayList<String>()));
	}

	@Override
	public void deleteTip(Document document) {
		MongoUtil.getCollection("tips").deleteOne(document);
		
	}

	@Override
	public void updateTip(Document doc) {
		MongoUtil.getCollection("tips").updateOne(new Document("_id",doc.getObjectId("_id")), doc);
	}

	@SuppressWarnings("deprecation")
	@Override
	public List queryTip(Document document,String username) {
		
		//已关注的集合
		Set<String> focus = new HashSet<String>();
		focus.add(username);
		MongoUtil.getCollection("realation").find(new Document("username",username)
				.append("interest", true)).forEach(
				new Block<Document>() {
					@Override
					public void apply(Document t) {
						focus.add(t.getString("people"));
					}
				});
		
		 List<Document> criteria = new ArrayList<Document>();
		 //事件ID
		if(StringUtils.isNotEmpty(document.getString("eventId"))){
			 criteria.add(Document.parse(String.format("{'$match': {'eventId': '%s'}}}", document.getString("eventId"))));
		}
		//关注人
		 criteria.add(Document.parse(String.format("{'$match': {'publisher': {'$in': %s}}}", JSON.toJSONString(focus))));
		 //关联 联系人
		 criteria.add(new Document().append("$lookup", new Document().append("from", "relationship")
				.append("localField", "publisher").append("foreignField", "username").append("as", "r_list"))); //r_list.tag in range
		 //关联 事件
		 criteria.add(new Document().append("$lookup", new Document().append("from", "event")
					.append("localField", "eventId").append("foreignField", "_id").append("as", "e_list"))); //r_list.tag in range
		 //过滤r_list元素，获取关联人
		 criteria.add(Document.parse(String.format("{'$addFields':{r_list:{$filter:{input: '$r_list',as: 'item',cond: { $eq: [ '$$item.people', '%s' ] }}},'e_list.relationship':{$concatArrays: ['$e_list.relationship','$e_list.username']}}}",username)));
		 criteria.add(new Document().append("$unwind",new Document().append("path", "$r_list").append("preserveNullAndEmptyArrays", true)));
		 //获取 动态与联系人标签交集   存在可见		
		 criteria.add(Document.parse("{$addFields:{'r_list.intersection':{$setIntersection:['$range','$r_list.tags']}}}"));
		 
		 //允许看到的集合
		 criteria.add(new Document().append("$match", Document.parse(String.format("{$or: [{'r_list.intersection':{$ne:[]},'r_list.intersection':{$ne:null}},{range:{$eq:[]}},{publisher:{$eq:'yanzi'}}]}", username))));
		 
		 criteria.add(Document.parse("{ $sort: { _id : -1 } },"));
		 criteria.add(Document.parse(String.format("{ $skip:%d },",document.getInteger("skipNum"))));
		 log.info("queryTip filter ==="+ JSON.toJSONString(criteria)); 
		
		List<Document> list = new ArrayList<Document>();
		MongoUtil.getCollection("tips").aggregate(criteria).forEach(new Block<Document>() {
			@Override
			public void apply(Document d) {
				//Document d1 = (Document) d.get("_id");
				//d1.put("_id", d1.getObjectId("_id").toHexString());
				d.put("_id", d.getObjectId("_id").toHexString());
				list.add(d);
			}
		});
		return list;
	}

	@Override
	public void support(Document document) {
		//up or down
		if(true == document.getBoolean("support")){
			
		}else{
			
		}
		
	}

	@Override
	public void addCommend(Document document) {
		ObjectId id = new ObjectId();
		document.put("_id", id);
		MongoUtil.getCollection("comments").insertOne(document);		
		MongoUtil.getCollection("support").insertOne(new Document("_id",id).append("type", 1).append("supportPeople", new ArrayList<String>()));

	}

	@Override
	public void removeCommend(Document document) {
		MongoUtil.getCollection("comments").deleteOne(document);		
	}

}
