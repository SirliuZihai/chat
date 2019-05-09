package com.zihai.shiro.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zihai.event.service.EventService;
import com.zihai.shiro.service.UserService;
import com.zihai.util.BusinessException;
import com.zihai.util.DateUtil;
import com.zihai.util.EncrypUtil;
import com.zihai.util.MongoUtil;
import com.zihai.util.Result;
//@CrossOrigin
@Controller
@RequestMapping("/shiro")
public class ShiroController {
	private final Logger log = Logger.getLogger(ShiroController.class);
	@Autowired
	private UserService userService;
	@Autowired
	private EventService eventService;
	
	@RequestMapping(value = "/manager.do")
	public String manager() {
		 Subject currentUser = SecurityUtils.getSubject();
	        if(currentUser.isAuthenticated()){
	            return "shiro/shiroManager";
	        }
			return "redirect:login.do";
	}
	
	@RequestMapping(value = "/login.do", method = RequestMethod.GET)
	@ResponseBody
	public Result login(HttpServletRequest request) {
		 Subject currentUser = SecurityUtils.getSubject();
	        if(currentUser.isAuthenticated()){
	            return Result.success("已认证");
	        }
			return Result.failure("请登录");
	}
	
	@RequestMapping(path="/login.do",method=RequestMethod.POST)
	@ResponseBody
	public Result LoginIn(@RequestBody Map<String,String> user,HttpServletRequest req){
		UsernamePasswordToken token;
		String gettoken = (String)user.get("token");
		if(StringUtils.isEmpty(gettoken)){
			token = new UsernamePasswordToken((String)user.get("username"),(String)user.get("password"));		
			if(StringUtils.isEmpty(user.get("username"))||StringUtils.isEmpty(user.get("password"))){
				return Result.failure("用户名或密码不能为空");
			}
		}else{
			token = EncrypUtil.getAuthInfo(gettoken);	
			//校验token失效
			/*if(!req.getRemoteHost().equals(token.getHost()))
				return  Result.failure("token失效，请重新登录！");*/
		}
		try{
			//token.setRememberMe(true);  org.apache.shiro.crypto.CryptoException: Unable to execute 'doFinal' with cipher instance
			token.setHost(req.getRemoteHost());
			SecurityUtils.getSubject().login(token);
			Map data = userService.findInfoByUsername2(token.getUsername());
			Calendar expire = Calendar.getInstance();
			expire.add(Calendar.DATE, 14);
			data.put("token", EncrypUtil.encode(token.getUsername()+"&"+new String(token.getPassword())+"&"+token.getHost()+"&"+expire.getTime()));
			log.info(token.getUsername() + "has loged In from " + token.getHost());
			return Result.success("登录成功",data);
		}catch(UnknownAccountException e){
			return Result.failure("用户名或密码不正确");
		}catch(LockedAccountException e){
			return Result.failure("该用户已禁用，请与管理员联系");
		}catch(AuthenticationException e){
			return Result.failure("用户名或密码不正确");
		}catch(BusinessException e){
			return Result.failure(e.getMessage());
		}catch(Exception e){
			e.printStackTrace();
			return Result.failure("登录失败");
		}
		
	}
	@RequestMapping(value = "/loginOut.do",method=RequestMethod.POST)
	@ResponseBody
	public String loginOut() {
		try{
			SecurityUtils.getSubject().logout();
			return "log out";
		}catch(Exception e){
			return "退出失败";
		}
	}
	
	@RequestMapping(value = "/newUser.do")
	@ResponseBody
	public String newUser(@RequestBody Map user,HttpServletRequest req) {
		try{
			if(userService.createUser(user)){
				//添加默认头像，
				 String path = req.getServletContext().getResource("/").getPath()+"image/head/";
				 File f = new File(path);
				 if(!f.exists())f.mkdirs();
				 InputStream in = new FileInputStream(path+"none.jpg");
				 FileOutputStream out = new FileOutputStream(path+(String)user.get("username")+".jpg");
				IOUtils.copy(in, out);
				//新人问候
				String uname = (String)user.get("username");
				if(!uname.equals("nicool")){
					String date = DateUtil.DateToString(new Date(), "yyyyMMdd");
					List<String> relationship = new ArrayList<String>();
					relationship.add((String)user.get("username"));
					eventService.save(new Document("username","nicool").append("title", "问候")
							.append("starttime", date).append("endtime", date).append("relationship",relationship).append("public", false));
					ObjectId event_key = MongoUtil.getCollection("event_queue").find(new Document("username",uname)).first().getObjectId("eventId");
					eventService.insertMessage(new Document("sender","nicool").append("data", "您好，我叫nicool，很高兴能为您服务。").append("type", "text")
							.append("relateId", event_key).append("receiver", relationship));
				}
				return "OK";
			}
			return "注册失败";
		}catch(Exception e){
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	@RequestMapping(value = "/unauthorized.do")
	@ResponseBody
	public String unauthorized() {
		return "无权限";
	}

}
