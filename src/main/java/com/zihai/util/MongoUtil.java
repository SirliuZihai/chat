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
					if(document.get("_id")!=null)
						document.put("_id", document.getObjectId("_id").toHexString());
					if(clazz == Document.class){
						list.add(document);
					}else{
						list.add(JSON.parseObject(document.toJson(), clazz));						
					}
		       }
		};
		log.info("Query filter ==="+ doc.toJson());
		getCollection(collectionName).find(doc).projection(projection).sort(new Document("_id",-1)).limit(20).forEach(block);
		log.info("Query result ==="+ doc.toJson());
		return list;
		
	}
	public static ObjectId getObjectId(Map<String,Object> id){
		return new ObjectId(new Date((long)id.get("time")),(int)id.get("machineIdentifier")
				,new Short(String.valueOf(id.get("processIdentifier"))),(int)id.get("counter"));
	}
	/**析构函数*/
	protected void finalize(){
		mongoClient.close();
	}
	public static void main(String[] args) {
		MongoUtil u = new MongoUtil();
	}
}
