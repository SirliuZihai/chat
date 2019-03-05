package com.zihai.shiro.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zihai.shiro.service.UserService;
import com.zihai.util.Result;
//@CrossOrigin
@Controller
@RequestMapping("/shiro")
public class ShiroController {
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
	public String LoginIn(@RequestBody Map<String,String> user){
		UsernamePasswordToken token = new UsernamePasswordToken((String)user.get("username"),(String)user.get("password"));		
		if(StringUtils.isEmpty(user.get("username"))||StringUtils.isEmpty(user.get("password"))){
			return "用户名或密码不能为空";
		}
		try{
			SecurityUtils.getSubject().login(token);
			token.setRememberMe(true);
			return "OK";
		}catch(UnknownAccountException e){
			return "用户名或密码不正确";
		}catch(LockedAccountException e){
			return "该用户已禁用，请与管理员联系";
		}catch(AuthenticationException e){
			return "用户名或密码不正确";
		}catch(Exception e){
			return "登录失败";
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
