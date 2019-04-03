package com.zihai.shiro.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zihai.shiro.service.UserService;
import com.zihai.util.EncrypUtil;
import com.zihai.util.Result;
//@CrossOrigin
@Controller
@RequestMapping("/shiro")
public class ShiroController {
	private final Logger log = Logger.getLogger(ShiroController.class);
	@Autowired
	private UserService userService;
	
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
			if(!req.getRemoteHost().equals(token.getHost()))
				return  Result.failure("token失效，请重新登录！");
		}
		try{
			token.setRememberMe(true);
			token.setHost(req.getRemoteHost());
			SecurityUtils.getSubject().login(token);
			Map data = userService.findInfoByUsername(token.getUsername());
			Calendar expire = Calendar.getInstance();
			expire.add(Calendar.DATE, 7);
			data.put("token", EncrypUtil.encode(token.getUsername()+"&"+new String(token.getPassword())+"&"+token.getHost()+"&"+expire.getTime()));
			log.info(token.getUsername() + "has loged In from " + token.getHost());
			return Result.success("登录成功",data);
		}catch(UnknownAccountException e){
			return Result.failure("用户名或密码不正确");
		}catch(LockedAccountException e){
			return Result.failure("该用户已禁用，请与管理员联系");
		}catch(AuthenticationException e){
			return Result.failure("用户名或密码不正确");
		}catch(Exception e){
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
	public String newUser(@RequestBody Map user) {
		try{
			if(userService.createUser(user))
				return "OK";
			throw new Exception();
		}catch(Exception e){
			return "添加用户失败";
		}
	}
	
	@RequestMapping(value = "/unauthorized.do")
	@ResponseBody
	public String unauthorized() {
		return "无权限";
	}

}
