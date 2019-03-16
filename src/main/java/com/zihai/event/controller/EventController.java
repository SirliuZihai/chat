package com.zihai.event.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.zihai.event.service.EventService;
import com.zihai.util.MongoUtil;
import com.zihai.util.Result;


@Controller
@RequestMapping("/event")
public class EventController {

	@Autowired
	private EventService eventService;
	
	@RequestMapping(value="/saveEvent.do",method = RequestMethod.POST)
	@ResponseBody
	public Result saveEvent(@RequestBody Map<String,Object> data){
		 Document event = new Document(data);
		 Subject currentUser = SecurityUtils.getSubject();
		 event.append("username", currentUser.getPrincipal());
		 try {
			eventService.save(event);
			return Result.success("保存成功");
		} catch (Exception e) {
			return Result.failure("错误："+e.getMessage());
		}		
	}
	@RequestMapping(value="/queryHistroy.do",method = RequestMethod.POST)
	@ResponseBody
	public Result queryEvent(@RequestBody Map<String,Object> filter){
		 Subject currentUser = SecurityUtils.getSubject();
		 try {
			List<Document> events = eventService.queryHistroy(filter,(String)currentUser.getPrincipal());
			return Result.success(null,events);
		} catch (Exception e) {
			return Result.failure("错误："+e.getMessage());
		}
	}
	@RequestMapping(value="/deleteEvent.do",method = RequestMethod.POST)
	@ResponseBody
	public Result deletEvent(@RequestBody Map<String,Object> event){
		 try {
			Document filter = new Document("_id",MongoUtil.getObjectId((Map<String,Object>)event.get("_id"))).append("username", event.get("username"));
			eventService.deleteEvent(filter);
			return Result.success("删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return Result.failure(e.getMessage());
		}
	}
}
