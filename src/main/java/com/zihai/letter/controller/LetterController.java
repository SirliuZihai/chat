package com.zihai.letter.controller;

import java.util.Map;

import org.bson.Document;
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
	@Autowired
	private LetterService letterService;
	
	@RequestMapping("getLetter.do")
	@ResponseBody
	public Result getLetter(){
		try {
			return Result.success(null, letterService.getLetters());
		} catch (Exception e) {
			e.printStackTrace();
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
			return Result.failure(e.getMessage());		
		}	
	}
}
