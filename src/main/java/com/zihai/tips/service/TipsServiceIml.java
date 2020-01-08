package com.zihai.tips.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.mongodb.Block;
import com.mongodb.client.result.UpdateResult;
import com.zihai.util.BusinessException;
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
	}

	@Override
	public void deleteTip(Document document) {
		document.put("_id", new ObjectId(document.getString("_id")));
		MongoUtil.getCollection("tips").deleteOne(document);
	}

	@Override
	public void updateTip(Document doc) {
		
	}

	@SuppressWarnings("deprecation")
	@Override
	public List queryTip(Document document,String username) {
		
		//已关注的集合
		Set<String> focus = new HashSet<String>();
		focus.add(username);
		MongoUtil.getCollection("relationship").find(new Document("username",username)
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
		 criteria.add(Document.parse(String.format("{'$addFields':{r_list:{$filter:{input: '$r_list',as: 'item',cond: { $eq: [ '$$item.people', '%s' ] }}},likesNum:{$size: '$likes'},comments:{$size: '$comments'},likes:{$in:['%s','$likes']}}}",username,username)));
		 criteria.add(new Document().append("$unwind",new Document().append("path", "$r_list").append("preserveNullAndEmptyArrays", true)));
		 criteria.add(new Document().append("$unwind",new Document().append("path", "$e_list").append("preserveNullAndEmptyArrays", true)));
		 //获取 动态与联系人标签交集   存在可见 转ObjectId
		 criteria.add(Document.parse(String.format("{$addFields:{'r_list.intersection':{$setIntersection:['$range','$r_list.tags']},event_isJoin:{$or:[{$eq:['$e_list.username','%s']},{$in:['%s','$e_list.relationship']}]},event_title:'$e_list.title',_id:{$toString:'$_id'},eventId:{$toString:'$eventId'},event_starttime:'$e_list.starttime',event_endtime:'$e_list.endtime'}}",username,username)));
		 
		 //允许看到的集合  标签可见||公开||自已发布||已参与
		 criteria.add(new Document().append("$match", Document.parse(String.format("{$or: [{'r_list.intersection':{$gt:[]}},{range:{$eq:[]}},{publisher:{$eq:'%s'}},{'event_isJoin':true}]}", username))));
		 criteria.add(Document.parse("{$project:{r_list:0,e_list:0}},"));
		 criteria.add(Document.parse("{ $sort: { _id : -1 } },"));
		 criteria.add(Document.parse(String.format("{ $skip:%d },",document.getInteger("skipNum"))));
		 criteria.add(Document.parse("{ $limit : 20 }"));
		 log.info("queryTip filter ==="+ JSON.toJSONString(criteria)); 
		
		List<Document> list = new ArrayList<Document>();
		MongoUtil.getCollection("tips").aggregate(criteria).into(list);
		return list;
	}

	@Override
	public void like(Document document) {
		String username = document.getString("username"); //SecurityUtils.getSubject().getPrincipal().toString();
		
		Document d = null; // finded doc
		//判断是动态还是评论
		if(StringUtils.isEmpty(document.getString("commentId"))){
			//动态
			Document filter = new Document("_id",new ObjectId(document.getString("_id")));
			if(true == document.getBoolean("like")){
				 d=MongoUtil.getCollection("tips").findOneAndUpdate(filter,new Document("$addToSet", new Document("likes",username)));
			}else{
				d = MongoUtil.getCollection("tips").findOneAndUpdate(filter,new Document("$pull", new Document("likes",username)));				
			}
		}else{
			//评论
			Document filter = new Document("_id",new ObjectId(document.getString("_id"))).append("comments._id", new ObjectId(document.getString("commentId")));
			if(true == document.getBoolean("like")){
				d = MongoUtil.getCollection("tips").findOneAndUpdate(filter,new Document("$addToSet", new Document("comments.likes",username)));
			}else{
				d = MongoUtil.getCollection("tips").findOneAndUpdate(filter,new Document("$pull", new Document("comments.likes",username)));
			}
		}
		
		if(d == null){
			throw new BusinessException("无效操作");
		}
		
	}

	@Override
	public String addComment(Document document) {
		String content = document.getString("content");
		String commentId = document.getString("commentId");
		if(StringUtils.isEmpty(content)){
			throw new BusinessException("内容不能为空");
		}
		ObjectId id = new ObjectId();
		String publisher =document.getString("publisher");
		Document entity = new Document();
		entity.put("publisher", publisher);
		entity.put("likes", new ArrayList<String>());
		entity.put("_id", id);
		entity.put("content", content);
		UpdateResult result = null; // finded doc
		if(StringUtils.isEmpty(commentId)){
			Document filter = new Document("_id",new ObjectId(document.getString("_id")));
			entity.put("reply", new ArrayList<String>()); //初始化 回复
			result = MongoUtil.getCollection("tips").updateOne(filter,new Document("$push",new Document("comments",entity)));
		}else{
			ObjectId ob_commentId = new ObjectId(commentId);
			Document filter = new Document("_id",new ObjectId(document.getString("_id"))).append("comments._id", ob_commentId);
			result = MongoUtil.getCollection("tips").updateOne(filter,new Document("$push",new Document("comments.reply",entity)));
		}

		if(result.isModifiedCountAvailable()&&result.getModifiedCount() == 0){
			throw new BusinessException("无效操作");
		}
		return id.toHexString();
	}

	@Override
	public void removeComment(Document document) {
		String _id = document.getString("_id");
		String commentId = document.getString("commentId");
		String replyId = document.getString("replyId");
		String publisher = document.getString("publisher");
		if(StringUtils.isEmpty(commentId)&&StringUtils.isEmpty(_id)){
			throw new BusinessException("id 不能为空");
		}
		ObjectId o_id = new ObjectId(_id);
		ObjectId o_commentId = new ObjectId(commentId);
		UpdateResult result = null; // finded doc
		if(StringUtils.isEmpty(replyId)){	
			Document filter = new Document("_id",o_id).append("comments._id", o_commentId).append("comments.publisher", publisher);
			Document opBson  = Document.parse(String.format("{$pull:{comments:{$eq:['comments._id','$toObjectId(%s)']}}}", commentId));
			log.info("removeComment filter ==="+JSON.toJSONString(filter));
			log.info("removeComment opBson ==="+JSON.toJSONString(opBson));
			result = MongoUtil.getCollection("tips").updateOne(filter,opBson);
		}
		else {
			ObjectId o_replyId = new ObjectId(replyId); //toObjectId
			Document filter = new Document("_id",o_id).append("comments._id", o_commentId).append("comments.reply._id", o_id).append("comments.reply.publisher", publisher);
			Document opBson  = Document.parse(String.format("{$pull:{comments.reply:{$eq:['comments.reply._id','$toObjectId(%s)']}}}", replyId));
			log.info("removeComment filter ==="+JSON.toJSONString(filter));
			log.info("removeComment opBson ==="+JSON.toJSONString(opBson));
			result = MongoUtil.getCollection("tips").updateOne(filter,opBson);
		}
		if(result.isModifiedCountAvailable()&&result.getModifiedCount() == 0){
			throw new BusinessException("无效操作");
		}	
	}

}
