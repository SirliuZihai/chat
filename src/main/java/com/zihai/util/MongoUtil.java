package com.zihai.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.IteratorUtils;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoUtil {
	private static Logger log = LoggerFactory.getLogger(MongoUtil.class);
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
					if(clazz == Document.class){
						list.add(document);
					}else{
						list.add(JSON.parseObject(document.toJson(), clazz));						
					}
		       }
		};
		log.info(doc.toJson());
		getCollection(collectionName).find(doc).projection(projection).sort(new Document("_id",-1)).limit(20).forEach(block);
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
	void queryInTest(){
		Set<String> s = new HashSet<String>();
		s.add("yanzi");
		s.add("xiaotong");
		FindIterable<Document> it = getCollection("userInfo").find(new Document().append("username", 
				new Document("$in",s)));
		List<Document> l = IteratorUtils.toList(it.iterator());
		System.out.println(JSON.toJSONString(l));
	}
	void queryTest0(){
		Document doc = new Document().append("username", new BsonString("yanzi"));	
		List<Document> list = Query(doc,null,Document.class,"userInfo");
		Document doc2 = list.get(0);
		Date d = ((ObjectId)list.get(0).get("_id")).getDate();
		System.out.println(d);
		System.out.println(list.get(0).toJson());
		doc2.put("like", "刺绣5");
		ObjectId key = (ObjectId) doc2.get("_id");
		getCollection("userInfo").deleteOne(new Document().append("_id", key));
		doc2.remove("_id");
		getCollection("userInfo").insertOne(doc2);
		Document doc3 = new Document().append("username", "yanzi");
		List<Document> list3 = Query(doc3,null,Document.class,"userInfo");
		Date d2 = ((ObjectId)list3.get(0).get("_id")).getDate();
		System.out.println(d2);
		System.out.println(list.get(0).toJson());
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
		u.queryInTest();
	}
}
