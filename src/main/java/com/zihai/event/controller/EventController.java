package com.zihai.event.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.zihai.event.service.EventService;
import com.zihai.notify.service.NotifyService;
import com.zihai.util.BusinessException;
import com.zihai.util.Result;


@Controller
@RequestMapping("/event")
public class EventController {
	private Logger log = LoggerFactory.getLogger(getClass());
	@Autowired
	private EventService eventService;
	@Autowired
	private NotifyService notifyService;
	
	@Value(value="${tempFilePath}")
	private String tempFilePath;
	@Value(value="${addressPath}")
	private String addressPath;
	
	@RequestMapping(value="/saveEvent.do",method = RequestMethod.POST)
	@ResponseBody
	public Result saveEvent(@RequestBody Map<String,Object> data){
		 Document event = new Document(data);
		 String currentUser = SecurityUtils.getSubject().getPrincipal().toString();
		 event.append("username", currentUser);
		 eventService.save(event);
		 return Result.success("保存成功");
	}

	@RequestMapping(value="/getEvent.do",method = RequestMethod.GET)
	@ResponseBody
	public Result getEvent(String eventId){
		Document d = eventService.getEventById(eventId);
		return Result.success(null,d);
	}
	
	@RequestMapping(value="/queryHistroy.do",method = RequestMethod.POST)
	@ResponseBody
	public Result queryEvent(@RequestBody Map<String,Object> filter){
		 Subject currentUser = SecurityUtils.getSubject();
		 List<Document> events = eventService.queryHistroy(filter,(String)currentUser.getPrincipal());
		 return Result.success(null,events);
	}
	@RequestMapping(value="/deleteEvent.do",method = RequestMethod.POST)
	@ResponseBody
	public Result deletEvent(@RequestBody Map<String,Object> event){
		String currentUser = SecurityUtils.getSubject().getPrincipal().toString();
		eventService.deleteEvent(currentUser,(String)event.get("_id"));
		return Result.success("删除成功");
	}
	@RequestMapping(value="accept.do",method = RequestMethod.GET)
	@ResponseBody
	public Result accept(String _id,String eventId,String sender,String receiver,Integer type){
		String currentUser = SecurityUtils.getSubject().getPrincipal().toString();
		Document event = eventService.getEventById(eventId);
		if(!currentUser.equals(receiver)){
			return Result.failure("操作人非法");
		}
		String people = null; // add men
		String op_people = null; // op men
		List<String> rela = event.getList("relationship", String.class);
		if(type == 5)
			people = sender;
			op_people = receiver;
		if(type == 6)
			people = receiver;
			op_people = sender;
		if(rela.contains(people)){
			return Result.failure("不能重复加入");
		}
		eventService.addRelation(eventId, people, op_people);
		notifyService.stateNotify(_id,2,currentUser);
		return Result.success("已接受");	
	}
	@RequestMapping(value="deny.do",method = RequestMethod.GET)
	@ResponseBody
	public Result deny(String _id){
		String currentUser = SecurityUtils.getSubject().getPrincipal().toString();
		notifyService.stateNotify(_id,3,currentUser);
		return Result.success("已拒绝");
	}

	
	@RequestMapping(value="/participateEvent.do",method = RequestMethod.GET)
	@ResponseBody
	public Result participateEvent(String eventId){
		String currentUser = SecurityUtils.getSubject().getPrincipal().toString();
		 Document event = eventService.getEventById(eventId);
		 if(event==null||StringUtils.isEmpty(event.getString("username")))
			 return Result.failure("该事件已删除");
		 String receiver = event.getString("username");	 
		 Document d =  new Document("relateId",new ObjectId(eventId)).append("sender",currentUser).append("receiver", receiver)
				 .append("state", 0).append("type", 5);
		 if(notifyService.getNotify(d)==null){
			 notifyService.addNotify(d);
			 return Result.success("已发送申请");
		 }else{
			 return Result.failure("不能重复申请");
		 }
	}

	@RequestMapping(value = "/uploadtempfile.do" ,method = RequestMethod.POST)
	@ResponseBody
	public Result uploadtempfile(@RequestParam(value="tempFile",required=false)MultipartFile tempFile,HttpServletRequest req) throws MalformedURLException {
		 String path =tempFilePath+UUID.randomUUID()+tempFile.getOriginalFilename().substring(tempFile.getOriginalFilename().indexOf("."));
		 log.info(path);
		 try { 
			 InputStream in = tempFile.getInputStream();
			 File f = new File(path.substring(0,path.lastIndexOf("/")+1));
			 if(!f.exists())f.mkdirs();
			 FileOutputStream out = new FileOutputStream(path);
			 IOUtils.copy(in, out);
			 return Result.success(path.replace(addressPath, ""));
		} catch (IOException e) {
			return Result.failure(e.getMessage());
		}finally{
		}
		
	}
}
