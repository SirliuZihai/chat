package com.zihai.tips.controller;

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
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.bson.Document;
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

import com.zihai.shiro.service.UserService;
import com.zihai.tips.service.TipsService;
import com.zihai.util.Result;

@Controller
@RequestMapping("/tips")
public class TipsController {
	private Logger log = LoggerFactory.getLogger(getClass());
	@Autowired
	private TipsService tipService;

	@Value(value = "${imagePath}")
	private String imagePath;
	@Value(value = "${addressPath}")
	private String addressPath;
	
	@RequestMapping(value = "/uploadImage.do" ,method = RequestMethod.POST)
	@ResponseBody
	public Result uploadtempfile(@RequestParam(value="tempFile",required=false)MultipartFile tempFile,HttpServletRequest req,
			@RequestParam(value="classify",defaultValue="tips")String classify) throws MalformedURLException {
		 String path =imagePath+classify+'/'+UUID.randomUUID()+tempFile.getOriginalFilename().substring(tempFile.getOriginalFilename().indexOf("."));
		 log.info("uploadtempfile.do path===" +path);
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
	@RequestMapping(value="/addTip.do",method = RequestMethod.POST)
	@ResponseBody
	public Result add(@RequestBody Map<String,Object> data){
		String username = SecurityUtils.getSubject().getPrincipal().toString();
		data.put("publisher", username);
		tipService.addTip(new Document(data));
		return Result.success("发布成功");
	}
	@RequestMapping(value="/deleteTip.do",method = RequestMethod.POST)
	@ResponseBody
	public Result delete(@RequestBody Map<String,Object> data){
		String username = SecurityUtils.getSubject().getPrincipal().toString();
		data.put("publisher", username);
		tipService.deleteTip(new Document(data));
		return Result.success("已删除");
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
		List list = tipService.queryTip(new Document(data), username);
		return Result.success(null,list);
	}
	@RequestMapping(value="/like.do",method = RequestMethod.POST)
	@ResponseBody
	public Result support(@RequestBody Map<String,Object> data){
		String username = SecurityUtils.getSubject().getPrincipal().toString();
		data.put("username", username);
		tipService.like(new Document(data));
		return Result.success(null);
		
	}
	@RequestMapping(value="/getComments.do",method = RequestMethod.GET)
	@ResponseBody
	public Result getComments(String tipId){
		String username = SecurityUtils.getSubject().getPrincipal().toString();
		Document comments = tipService.getComments(tipId,username);
		return Result.success(null,comments);
	}
	@RequestMapping(value="/comment.do",method = RequestMethod.POST)
	@ResponseBody
	public Result comment(@RequestBody Map<String,Object> data){
		String username = SecurityUtils.getSubject().getPrincipal().toString();
		data.put("publisher", username);
		tipService.addComment(new Document(data));
		return Result.success("添加成功");
	}
	@RequestMapping(value="/removeComment.do",method = RequestMethod.POST)
	@ResponseBody
	public Result removeComment(@RequestBody Map<String,Object> data){
		String username = SecurityUtils.getSubject().getPrincipal().toString();
		data.put("publisher", username);
		tipService.removeComment(new Document(data));
		return Result.success("删除成功");
	}
}
