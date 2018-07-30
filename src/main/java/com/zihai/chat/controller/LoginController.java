package com.zihai.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {
	@RequestMapping("/login.do")
	@ResponseBody
	public String login(String username,String password){
		if("liuyizhi".equals(username)&&"123456".equals(password)){
			return "true";
		}else if("wangmeng".equals(username)&&"123456".equals(password)){
			return "true";
		}else {
			return "false";
		}
	}
}
