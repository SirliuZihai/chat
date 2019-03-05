package com.zihai.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.BsonString;
import org.bson.Document;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoUtil {
	private static MongoDatabase database;
	private static MongoClient mongoClient;
	static{
		mongoClient = new MongoClient( "127.0.0.1" , 27017 );
		database = mongoClient.getDatabase("secreatary");
	}
	public static MongoCollection<Document> getCollection(String name){
		return database.getCollection(name);
	}
	public static List Query(Document doc , Document projection,Class<? extends Object> clazz,String collectionName){
		List list = new ArrayList();
		Block<Document> block = new Block<Document>() {
		       @Override
		       public void apply(final Document document) {
		    	   list.add(JSON.parseObject(document.toJson(), clazz));
		       }
		};
		getCollection(collectionName).find(doc).projection(projection).limit(20).forEach(block);
		return list;
		
	}
	/**析构函数*/
	protected void finalize(){
		mongoClient.close();
	}
	void insertTest(){	
		Document doc = new Document().append("username", new BsonString("liu"))
				.append("password",  new BsonString(EncrypUtil.encryptPassword("111111")));
		getCollection("user").insertOne(doc);
	}
	void queryTest(){
		Document doc =Document.parse("{'username':{$regex:/^xiao/}}");	
		System.out.println(JSON.toJSONString(doc));
		List<Map> list = Query(doc,Document.parse("{'_id':0}"),Map.class,"user");
		for(Map m :list){
			System.out.println(JSON.toJSONString(m));
		}
	}
	void DeletTest(){
		Document doc =Document.parse("{'username':'liu'}");	
		System.out.println(getCollection("user").deleteMany(doc).getDeletedCount());	
	}
	void UpdateTest(){
		Document doc =Document.parse("{'username':'zihai'}");	
		Document doc2 =Document.parse("{$set:{'username':'zihai','like':'bbb','book':''}}");
		getCollection("userInfo").updateMany(doc, doc2);
	}
	void saveOrUpdate(){
		Document doc =Document.parse("{'username':'zihai'}");
		String json_doc2 = "{'username':'zihai','like':'bbb2s','book':''}";
		Document doc2 =Document.parse("{$set:"+json_doc2+"}");
			if(getCollection("userInfo").findOneAndUpdate(doc,doc2)==null)
				getCollection("userInfo").insertOne(doc2);;	
	}
	public static void main(String[] args) {
		MongoUtil u = new MongoUtil();
		u.queryTest();
	}
}
