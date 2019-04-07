package com.zihai.letter.controller;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zihai.letter.service.LetterService;
import com.zihai.util.Result;

@Controller
@RequestMapping("letter")
public class LetterController {
	private final Logger log = LoggerFactory.getLogger(LetterController.class);

	@Autowired
	private LetterService letterService;
	
	@RequestMapping("getLetter.do")
	@ResponseBody
	public Result getLetter(){
		try {
			return Result.success(null, letterService.getLetters());
		} catch (Exception e) {
			log.error(e.getMessage());
			return Result.failure(e.getMessage());		
		}	
	}
	
	@RequestMapping("sendLetter.do")
	@ResponseBody
	public Result sendLetter(@RequestBody Map<String,Object> data){
		Document letterBox = new Document(data);
		try {
			letterService.sendLetter(letterBox.getString("username"), letterBox.get("letter",Document.class));
			return Result.success("投递成功");
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return Result.failure(e.getMessage());		
		}
	}
	
	@RequestMapping("getOtherLetter.do")
	@ResponseBody
	public Result getOtherLetter(String other){
		try {
			return Result.success(null, letterService.getOtherLetters(other));
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return Result.failure(e.getMessage());		
		}	
	}
	@RequestMapping("getNearrBox.do")
	@ResponseBody
	public Result getNearrBox(@RequestBody Map<String,Object> data){
		Document position = new Document(data);
		try {
			return Result.success(null, letterService.getLetterBox(position));
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return Result.failure(e.getMessage());		
		}	
	}
	@RequestMapping("delete.do")
	@ResponseBody
	public Result delete(@RequestBody Map<String,String> data){
		Subject currentUser = SecurityUtils.getSubject();
		data.put("username", currentUser.getPrincipals().toString());
		try {
			letterService.delete(data);
			return Result.success("删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return Result.failure(e.getMessage());		
		}	
	}
}
