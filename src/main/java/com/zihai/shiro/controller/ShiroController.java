package com.zihai.shiro.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.mail.MessagingException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
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
import com.zihai.util.SpringMail;

import net.sf.ehcache.CacheException;
//@CrossOrigin
@Controller
@RequestMapping("/shiro")
public class ShiroController {
	private final Logger log = LoggerFactory.getLogger(getClass());
	@Autowired
	private UserService userService;
	@Autowired
	private EventService eventService;
	@Autowired
	private EhCacheCacheManager cacheManager;
	@Value(value = "${helloworld}")
	private String helloworld;
	
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
					eventService.save(new Document("username","nicool").append("title", "问候").append("type", 3)
							.append("starttime", date).append("endtime", date).append("relationship",relationship).append("public", false)
							.append("place",Document.parse("{type: 'Point', coordinates: [], name: ''}")));
					ObjectId event_key = MongoUtil.getCollection("event_queue").find(new Document("username",uname)).first().getObjectId("eventId");
					eventService.insertMessage(new Document("sender","nicool").append("data", helloworld).append("type", "text")
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
	@RequestMapping(value = "/getMail.do")
	@ResponseBody
	public Result getMail(){
		String uname = (String) SecurityUtils.getSubject().getPrincipal();
		List<Document> l = MongoUtil.Query(new Document("username",uname), new Document("mail",1), Document.class, "user");
		if(l.size()>0){
			return Result.success(null,l.get(0));
		}else{
			return Result.failure("none of mail");
		}
		
	}
	@RequestMapping(value = "/setMail_step1.do",method=RequestMethod.GET)
	@ResponseBody
	public Result setMail(String mailName) throws MessagingException, IOException {
		String uname = (String) SecurityUtils.getSubject().getPrincipal();
		Random random = new Random();
		StringBuilder s = new StringBuilder(String.valueOf(random.nextInt(1000000)));
		while(s.length()<6){
			s.insert(0, "0");
		}
		try {
			new SpringMail().sendCode(mailName,s.toString());
			Cache cache = cacheManager.getCache("EmailCode");
			cache.put(uname, s.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return Result.failure("验证码发送失败");
		}
		return Result.success("验证码已发送");
		
	}
	@RequestMapping(value = "/setMail_step2.do", method=RequestMethod.GET)
	@ResponseBody
	public Result setMail2(String code,String mailName) throws MessagingException, IOException {
		try {
			String uname = (String) SecurityUtils.getSubject().getPrincipal();
			Cache cache = cacheManager.getCache("EmailCode");
			if(code.equals(cache.get(uname,String.class))){
				userService.updateUser(new Document("mail",mailName).append("username", uname));
				cache.evict(uname);
				return Result.success("设置成功");
			}else{
				return Result.failure("验证码错误");
			}
		} catch (Exception e) {
			return Result.failure(e.getMessage());
		}
	}

	
	@RequestMapping(value = "/resetPassword_step1.do",method=RequestMethod.GET)
	@ResponseBody
	public Result ResetPassword(String username,String mail) {
		Document user = userService.findUser(new Document("username",username));
		if(user == null){
			return Result.failure("用户不存在。");
		}
		if(StringUtils.isEmpty(user.getString("mail"))){
			return Result.failure("未绑定邮箱，无法发送验证码。");
		}
		if(!mail.equals(user.getString("mail"))){
			return Result.failure("邮箱不正确");
		}
		Random random = new Random();
		StringBuilder s = new StringBuilder(String.valueOf(random.nextInt(1000000)));
		while(s.length()<6){
			s.insert(0, "0");
		}
		try {
			new SpringMail().sendCode(user.getString("mail"),s.toString());
		} catch (Exception e) {
			return Result.failure("验证码发送失败");
		}
		Cache cache = cacheManager.getCache("EmailCode");
		cache.put(username, s.toString());
		return Result.success("验证码已发送");
	}
	@RequestMapping(value = "/resetPassword_step2.do",method=RequestMethod.GET)
	@ResponseBody
	public Result resetPassword_step2(String code,String username,String password) throws MessagingException, IOException {
		try {
			Cache cache = cacheManager.getCache("EmailCode");
			if(code.equals(cache.get(username,String.class))){
				userService.updateUser(new Document("password",password).append("username", username));
				cache.evict(username);
				return Result.success("密码重置成功");
			}else{
				return Result.failure("验证码错误");
			}
		} catch (Exception e) {
			return Result.failure("验证码错误："+e.getMessage());
		}
	}
	@RequestMapping(value="/test2.do",method = RequestMethod.GET)
	public String test(){
		Cache cache = cacheManager.getCache("EmailCode");
		cache.put("test", "testVlue");
		String s = cache.get("test",String.class);
		String s1 = cache.get("test").toString();
		log.info(s);
		log.info("testVlue".equals(s)+"");
		return "404";
	}
}
