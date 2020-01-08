package com.zihai.shiro.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.bson.Document;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCursor;
import com.zihai.util.BusinessException;
import com.zihai.util.MongoUtil;

@Service
public class UserServiceImpl implements UserService {
	public Boolean createUser(Map user) {
		try {
			MongoUtil.getCollection("user").insertOne(Document.parse(JSON.toJSONString(user)));
			MongoUtil.getCollection("userInfo").insertOne(new Document("username",(String)user.get("username")).append("alia", "").append("UIM", (String)user.get("UIM")));
			return true;
		} catch(MongoWriteException e){
			if(e.getMessage().contains("duplicate")){
				throw new BusinessException("该用户已被注册");
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Boolean updateUser(Map user) {
		Document doc = MongoUtil.getCollection("user").findOneAndUpdate(Document.parse("{'username':'"+user.get("username")+"'}"), Document.parse("{$set:"+JSON.toJSONString(user)+"}"));
		return doc!=null;
	}

	
	public void correlationRoles(Long userId, Long... roleIds) {
		// TODO Auto-generated method stub

	}

	
	public void uncorrelationRoles(Long userId, Long... roleIds) {
		// TODO Auto-generated method stub

	}


	@Override
	public Map findByUsername(String username) {
		Iterator<Document> it = MongoUtil.getCollection("user").find(Document.parse("{'username':'"+username+"'}")).projection(Document.parse("{'_id':0}")).iterator();
		if(it.hasNext()){
			return (Map)it.next();
		}else{
			return null;
		}				
	}

	public Map findInfoByUsername(String username) {
		Iterator<Document> it = MongoUtil.getCollection("userInfo").find(Document.parse("{'username':'"+username+"'}")).projection(Document.parse("{'_id':0}")).iterator();
		if(it.hasNext()){
			return (Map)it.next();
		}else{
			return null;
		}				
	}
	@Override
	public Map findInfoByUsername2(String username) {
		Document it = MongoUtil.getCollection("userInfo").find(new Document("username",username)).projection(Document.parse("{'alias':1,'doing':1,'place':1}")).first();
		if(it!=null){
			return it;
		}else{
			return new HashMap();
		}
	}
	@Override
	public void updateOrInsertUserInfo(Map user) {
		Document doc = Document.parse("{'username':'"+user.get("username")+"'}");
		Document doc2 =Document.parse("{$set:"+JSONObject.toJSONString(user)+"}");
			if(MongoUtil.getCollection("userInfo").findOneAndUpdate(doc,doc2)==null)
				MongoUtil.getCollection("userInfo").insertOne(Document.parse(JSONObject.toJSONString(user)));;
	}

	@Override
	public List<Map> searchUser(Map userInfo) {
		Document doc,doc2;
		try {
			doc = Document.parse("{'username':{$regex:/^"+userInfo.get("username")+"/,$options:'si'}}");
			doc2 = Document.parse("{'username':1,'alias':1}");
			return MongoUtil.Query(doc, doc2, Map.class, "userInfo");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}	
	}

	@Override
	public List<Map> getRelationUser(HashMap userInfo) {
		Document doc;
		try {
			doc = Document.parse(JSONObject.toJSONString(userInfo));
			return MongoUtil.Query(doc, Document.parse("{'_id':0}"), Map.class, "relationship");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void updateRelation(Map relation) {
		Document doc;
		doc = Document.parse("{'username':'"+relation.get("username")+"','people':'"+relation.get("people")+"'}");
		Document doc2 =Document.parse("{$set:"+JSONObject.toJSONString(relation)+"}");
		if(MongoUtil.getCollection("relationship").findOneAndUpdate(doc,doc2)==null)
			throw new BusinessException("更新失败");		
	}

	@Override
	public void addrelation(Map relation) {
		Document doc;
		doc = Document.parse("{'username':'"+relation.get("username")+"','people':'"+relation.get("people")+"'}");
		if(MongoUtil.getCollection("relationship").count(doc)==0){
			MongoUtil.getCollection("relationship").insertOne(Document.parse(JSONObject.toJSONString(relation)));;
		}else{
			throw new BusinessException("已经添加了");
		}
		
	}
	@Override
	public void removeRelation(Map relation) {
		Document doc;
		doc = Document.parse("{'username':'"+relation.get("username")+"','people':'"+relation.get("people")+"'}");
		if(MongoUtil.getCollection("relationship").findOneAndDelete(doc) == null)
			throw new BusinessException("删除失败");		
	}

	@Override
	public Document findUser(Document user) {
		return MongoUtil.getCollection("user").find(user).first();
	}

	@Override
	public List<String> getRelationTags() {
		/*String username = (String)SecurityUtils.getSubject().getPrincipal();
		List<String> list = new ArrayList<String>();
		MongoCursor<Document> it =MongoUtil.getCollection("relationship").find(
				Document.parse(String.format("{username:'%s'}",username)))
				.projection(Document.parse("{tag:1}")).iterator();
		Set<String> set = new HashSet<String>();
		while(it.hasNext()){
			Document d = it.next();
			
			list.add(d.getString("_id"));
		}*/
		return null;
	}
}
