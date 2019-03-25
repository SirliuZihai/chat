package com.zihai.letter.service;

import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.bson.Document;
import org.springframework.stereotype.Service;

import com.zihai.util.BusinessException;
import com.zihai.util.MongoUtil;
@Service
public class LetterServiceIml implements LetterService {

	@Override
	public List getLetters() {
		String uname =(String)SecurityUtils.getSubject().getPrincipal();
		Document filter = new Document("username",uname).append("letter.state", 0);
		Document d = MongoUtil.getCollection("letter").find(filter).first();
		return d.getList("letter", Map.class);
	}
	@Override
	public List getOtherLetters(String othername) {
		String uname =(String)SecurityUtils.getSubject().getPrincipal();
		Document filter = new Document("username",othername).append("public", 1);
		//公开信，或者回复自已的信
		List l = MongoUtil.Query(filter, new Document("state",0), Document.class, "letter");
		return l;
	}
	@Override
	public void sendLetter(String username ,Document letter) {
		Document old = MongoUtil.getCollection("letterBox").find(new Document("username",username)).first();
		if(old == null) throw new BusinessException("无此信箱");
		List<Document> l = old.getList("letter", Document.class);
		l.add(letter);
		old.put("letter", l);
		MongoUtil.getCollection("letterBox").findOneAndReplace(new Document("username",username), old);

	}
}
