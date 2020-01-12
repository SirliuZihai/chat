package com.zihai.notify.controller;

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

import com.zihai.notify.service.NotifyService;
import com.zihai.util.Result;

@Controller
@RequestMapping("notify")
public class NotifyController {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private NotifyService notifyService;
	
	
	@RequestMapping(value="countNofify.do",method = RequestMethod.GET)
	@ResponseBody
	public Result countNofify(){
		String currentUser = SecurityUtils.getSubject().getPrincipal().toString();
		List<Document> result = notifyService.countNofify(currentUser);
		return Result.success(null,result);
	}
	@RequestMapping(value="queryNofify.do",method = RequestMethod.POST)
	@ResponseBody
	public Result queryNofify(@RequestBody Map<String,Object> filter){
		filter.put("username", SecurityUtils.getSubject().getPrincipal().toString());
		List<Document> result = notifyService.queryNofify(new Document(filter));
		return Result.success(null,result);
	}

	@RequestMapping(value="ignoreNotify.do",method = RequestMethod.GET)
	@ResponseBody
	public Result ignoreNotify(Integer type){
		String currentUser = SecurityUtils.getSubject().getPrincipal().toString();
		if(type == 0 || type ==1){
			notifyService.ignoreNotify(1,currentUser);
			notifyService.ignoreNotify(0,currentUser);
		}else{
			notifyService.ignoreNotify(type,currentUser);
		}
		return Result.success(null);
	}
}
