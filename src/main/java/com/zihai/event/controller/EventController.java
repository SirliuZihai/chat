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

import com.zihai.event.service.EventService;
import com.zihai.notify.service.NotifyService;
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
		 Subject currentUser = SecurityUtils.getSubject();
		 event.append("username", currentUser.getPrincipal());
		 try {
			eventService.save(event);
			return Result.success("保存成功");
		} catch (Exception e) {
			e.printStackTrace();
			return Result.failure("错误："+e.getMessage());
		}
	}

	@RequestMapping(value="/getEvent.do",method = RequestMethod.GET)
	@ResponseBody
	public Result getEvent(String eventId){
		 try {
			Document d = eventService.getEventById(eventId);
			return Result.success(null,d);
		} catch (Exception e) {
			e.printStackTrace();
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
			Document filter = new Document("_id",new ObjectId((String)event.get("_id"))).append("username", event.get("username"));
			eventService.deleteEvent(filter);
			return Result.success("删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return Result.failure(e.getMessage());
		}
	}
	@RequestMapping(value="/participateEvent.do",method = RequestMethod.GET)
	@ResponseBody
	public Result participateEvent(String eventId){
		try {
		 String currentUser = SecurityUtils.getSubject().getPrincipal().toString();
		 String receiver = eventService.getEventById(eventId).getString("username");
		 
		 Document d =  new Document("relateId",new ObjectId(eventId)).append("sender",currentUser).append("receiver", receiver)
				 .append("state", 0).append("type", 5);
		 if(notifyService.getNotify(d)==null){
			 notifyService.addNotify(d);
			 return Result.success("已发送申请");
		 }else{
			 return Result.failure("不能重复申请");
		 }
		} catch (Exception e) {
			e.printStackTrace();
			log.info(e.getMessage());
			return Result.failure("错误："+e.getMessage());
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
