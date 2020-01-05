package com.zihai.tips.controller;

import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zihai.shiro.service.UserService;
import com.zihai.tips.service.TipsService;
import com.zihai.util.Result;

@Controller
@RequestMapping("/tips")
public class TipsController {
	private Logger log = LoggerFactory.getLogger(getClass());
	@Autowired
	private TipsService tipService;
	@Autowired
	private UserService userService;
	
	@RequestMapping(value="/addTip.do",method = RequestMethod.POST)
	@ResponseBody
	public Result add(@RequestBody Map<String,Object> data){
		String username = SecurityUtils.getSubject().getPrincipal().toString();
		try {
			data.put("publisher", username);
			tipService.addTip(new Document(data));
			return Result.success("发布成功");
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return Result.failure(e.getMessage());		
		}
	}
	@RequestMapping(value="/deleteTip.do",method = RequestMethod.POST)
	@ResponseBody
	public Result delete(@RequestBody Map<String,Object> data){
		String username = SecurityUtils.getSubject().getPrincipal().toString();
		try {
			data.put("publisher", username);
			tipService.deleteTip(new Document(data));
			return Result.success("已删除");
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return Result.failure(e.getMessage());		
		}
	}
	@RequestMapping(value="/updateTip.do",method = RequestMethod.POST)
	@ResponseBody
	public Result updateTip(@RequestBody Map<String,Object> data){
		
		
		return null;
	}
	@RequestMapping(value="/queryTips.do",method = RequestMethod.POST)
	@ResponseBody
	public Result queryTips(@RequestBody Map<String,Object> data){
		String username = SecurityUtils.getSubject().getPrincipal().toString();
		try {
			List list = tipService.queryTip(new Document(data), username);
			return Result.success(null,list);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return Result.failure(e.getMessage());		
		}	
	}
	@RequestMapping(value="/support.do",method = RequestMethod.POST)
	@ResponseBody
	public Result support(@RequestBody Map<String,Object> data){
		String username = SecurityUtils.getSubject().getPrincipal().toString();
		try {
			data.put("username", username);
			tipService.support(new Document(data));
			return Result.success(null);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return Result.failure(e.getMessage());		
		}
	}
	@RequestMapping(value="/comment.do",method = RequestMethod.POST)
	@ResponseBody
	public Result comment(@RequestBody Map<String,Object> data){
		String username = SecurityUtils.getSubject().getPrincipal().toString();
		try {
			data.put("publisher", username);
			tipService.addCommend(new Document(data));
			return Result.success("添加成功");
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return Result.failure(e.getMessage());		
		}
	}
	@RequestMapping(value="/removeComment.do",method = RequestMethod.POST)
	@ResponseBody
	public Result removeComment(@RequestBody Map<String,Object> data){
		String username = SecurityUtils.getSubject().getPrincipal().toString();
		try {
			data.put("publisher", username);
			tipService.removeCommend(new Document(data));
			return Result.success("删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return Result.failure(e.getMessage());		
		}
	}
}
