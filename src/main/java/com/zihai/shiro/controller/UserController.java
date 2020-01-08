package com.zihai.shiro.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.jboss.logging.Logger;
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
import com.zihai.event.controller.EventController;
import com.zihai.shiro.service.UserService;
import com.zihai.util.BusinessException;
import com.zihai.util.Result;
@Controller
@RequestMapping("/user")
public class UserController {
	private final Logger log = Logger.getLogger(UserController.class);
	@Autowired
	private UserService userService;
	@Value(value = "${imagePath}")
	private String imagePath;
	
	UserController(){
		log.info("init");
	}
	
	@RequestMapping(value = "/saveUserInfo.do" ,method = RequestMethod.POST)
	@ResponseBody
	public Result saveuserInfo(@RequestBody Map user) {
		 Subject currentUser = SecurityUtils.getSubject();
		 user.put("username", (String)currentUser.getPrincipal());
		 try {
			userService.updateOrInsertUserInfo(user);
			return Result.success("保存成功");
		 } catch (Exception e) {
			return Result.failure("保存失败["+e.getMessage()+"]");
		 }
	}
	@RequestMapping(value = "/userInfo.do" ,method = RequestMethod.POST)
	@ResponseBody
	public Result getuserInfo(@RequestBody Map user) {
		 Map result = userService.findInfoByUsername2((String)user.get("username"));
		 return Result.success(null,result);
	}
	@RequestMapping(value = "/userInfo.do" ,method = RequestMethod.GET)
	@ResponseBody
	public Result getuserInfo() {
		 Subject currentUser = SecurityUtils.getSubject();
		 Map result = userService.findInfoByUsername((String)currentUser.getPrincipal());
		 return Result.success(null,result);
	}
	@RequestMapping(value = "/searchUser.do" ,method = RequestMethod.POST)
	@ResponseBody
	public Result searchUser(@RequestBody Map userInfo) {
		 List<Map> result = userService.searchUser(userInfo);
		 return Result.success(null,result);
	}
	@RequestMapping(value = "/relationUser.do" ,method = RequestMethod.GET)
	@ResponseBody
	public Result relationUser() {
		 Subject currentUser = SecurityUtils.getSubject();
		 HashMap<String, Object> user = new HashMap<String,Object>();
		 user.put("username", currentUser.getPrincipal());
		 List<Map> result = userService.getRelationUser(user);
		 return Result.success(null,result);
	}
	@RequestMapping(value = "/updateRelation.do" ,method = RequestMethod.POST)
	@ResponseBody
	public Result updateRelation(@RequestBody Map relation) {
		 log.info(JSON.toJSONString(relation));
		 Subject currentUser = SecurityUtils.getSubject();
		 relation.put("username", currentUser.getPrincipal());
		 try {
			userService.updateRelation(relation);
			return Result.success("保存成功");
		 } catch (BusinessException e) {
			return Result.failure(e.getMessage());
		 }
	}
	@RequestMapping(value = "/addrelation.do" ,method = RequestMethod.POST)
	@ResponseBody
	public Result addrelation(@RequestBody Map relation) {
		 Subject currentUser = SecurityUtils.getSubject();
		 relation.put("username", currentUser.getPrincipal());
		  try {
			userService.addrelation(relation);
			return Result.success("添加成功");
		} catch (BusinessException e) {
			return Result.failure(e.getMessage());
		}
	}
	@RequestMapping(value = "/removeRelation.do" ,method = RequestMethod.POST)
	@ResponseBody
	public Result removeRelation(@RequestBody Map relation) {
		 Subject currentUser = SecurityUtils.getSubject();
		 relation.put("username", currentUser.getPrincipal());
		  try {
			userService.removeRelation(relation);
			return Result.success("删除成功");
		} catch (BusinessException e) {
			return Result.failure(e.getMessage());
		}
	}
	@RequestMapping(value = "/uploadHeadImg.do" ,method = RequestMethod.POST)
	@ResponseBody
	public Result uploadHeadImg(@RequestParam(value="headImageFile",required=false)MultipartFile headImageFile,HttpServletRequest req) throws MalformedURLException {
		 Subject currentUser = SecurityUtils.getSubject();
		 String path = imagePath+"head/"+(String)currentUser.getPrincipal()+".jpg";
		 log.info("uploadHeadImg.do path===" +path);
		 try { 
			 InputStream in = headImageFile.getInputStream();
			 File f = new File(path.substring(0,path.lastIndexOf("/")+1));
			 if(!f.exists())f.mkdirs();
			 FileOutputStream out = new FileOutputStream(path);
			IOUtils.copy(in, out);
			 return Result.success("上传成功");
		} catch (IOException e) {
			return Result.failure(e.getMessage());
		}finally{
			//1.7自动关闭流	
		}
		
	}
	public static void main(String[] args) {
		InputStream in = null;
		 FileOutputStream out = null;
		 try {
			 in = new FileInputStream("E:\\work_2019\\a.jpg");
			 String path = "/work_2019/log/b.jpg";
			 File f = new File(path.substring(0,path.lastIndexOf("/")+1));
			 if(!f.exists())f.mkdirs();
			 out = new FileOutputStream(path+"b.jpg");
			IOUtils.copy(in, out);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(in !=null)in.close();
			} catch (IOException e) {
			}
			try {
				if(out !=null)out.close();
			} catch (IOException e) {
			}	
		}
	}
}
