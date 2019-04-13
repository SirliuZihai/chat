package com.zihai.letter.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.mongodb.Block;
import com.mongodb.client.result.UpdateResult;
import com.zihai.util.BusinessException;
import com.zihai.util.MongoUtil;
@Service
public class LetterServiceIml implements LetterService {
	

	@Override
	public List getLetters() {
		String uname =(String)SecurityUtils.getSubject().getPrincipal();
		Document filter = new Document("username",uname).append("letter.state", 0);
		Document d = MongoUtil.getCollection("letter").find(filter).first();
		if(d!=null){
			return d.getList("letter", Map.class);
		}
		return new ArrayList();
	}
	@Override
	public List getOtherLetters(String othername) {
		String uname =(String)SecurityUtils.getSubject().getPrincipal();
		Document filter = new Document("username",othername);
		if(othername.equals(uname)){
		}else{
			//公开信，或者回复自已的信
			Set<Document> or_set = new HashSet<Document>();
			or_set.add(new Document("letter.public", true));
			or_set.add(new Document("letter.receiver",uname));
			or_set.add(new Document("letter.sender",uname));
			filter.append("$or", or_set);
		}
		List<Document> l = new ArrayList<Document>();
		Block<Document> block = new Block<Document>() {
			@Override
		       public void apply(final Document document) { //只查出一条
				List<Document> e = (List<Document>) document.get("letter");
				if(e!=null)
					for(Document d : e){
						d.put("_id", d.getObjectId("_id").toHexString());
					}
					l.addAll(e);
		       }
		};
		MongoUtil.getCollection("letterBox").find(filter).projection(new Document("letter",1)).forEach(block);
		return l;
	}
	@Override
	public List getLetterBox(Document position) {
		Double x = position.getDouble("longitude");
		Double y = position.getDouble("latitude");
		BigDecimal range = new BigDecimal(500).divide(new BigDecimal(6378137),10,BigDecimal.ROUND_HALF_DOWN);//  500/6378137
		Document doc = Document.parse("{'letterPlace':{$geoWithin:{$centerSphere:[["+x+","+y+"],"+range+"]}}}");
		List l = MongoUtil.Query(doc, new Document("username",1).append("letterPlace",1), Document.class, "letterBox");
		return l;
	}
	@Override
	public void sendLetter(String username ,Document letter) {
		letter.put("_id", new ObjectId());
		Document old = MongoUtil.getCollection("letterBox").findOneAndUpdate(new Document("username",username), 
				new Document("$push",
						new Document("letter",letter)));
		if(old == null) 
			throw new BusinessException("无此信箱");
	}
	@Override
	public void delete(Map<String, String> data) {
		if(StringUtils.isEmpty(data.get("_id")))throw new BusinessException("_id不能为空");
		UpdateResult result = MongoUtil.getCollection("letterBox").updateOne(new Document("username",data.get("username")),
				new Document("$pull",
						new Document("letter",
								new Document("_id", new ObjectId(data.get("_id"))
										))));
		if(result.getModifiedCount()==0)
			throw new BusinessException("删除失败");
	}
	@Override
	public void setBoxPlace(String username, Document position) {
		List<Double> array = new ArrayList<Double>();
		array.add(position.getDouble("longitude"));
		array.add(position.getDouble("latitude"));
		MongoUtil.getCollection("letterBox").updateOne(new Document("username",username),
				new Document("$set",
						new Document("letterPlace.coordinates", array)));
		
	}
	@Override
	public void setUpBox(String username, Document position) {
		List<Double> array = new ArrayList<Double>();
		array.add(position.getDouble("longitude"));
		array.add(position.getDouble("latitude"));
		MongoUtil.getCollection("letterBox").insertOne(new Document("username",username).append("letterPlace", 
				new Document("type","Point").append("coordinates", array)));
						
		
	}
}
