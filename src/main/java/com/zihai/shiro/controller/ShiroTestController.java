package com.zihai.shiro.controller;

import java.io.IOException;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zihai.util.FileUtil;


@Controller
@ConfigurationProperties
@RequestMapping("/shiro")
public class ShiroTestController {
	private Logger log = LoggerFactory.getLogger(getClass());
	@Value(value = "${helloworld}")
	private String helloworld;
	
	@RequestMapping("test1")@RequiresRoles("admin")@ResponseBody
	public String test1(){
		log.info(SecurityUtils.getSubject().getPrincipal()+"拥有admin角色  通过了test1");
		return "通过了test1";
	}
	
	@RequestMapping("/test2")
	@RequiresPermissions("account:create")
	@ResponseBody
	public String test2(){
		log.info(SecurityUtils.getSubject().getPrincipal()+"拥有account:create权限  通过了test2");
		return "通过了test2";
	}
	@RequestMapping("/test3")
	@ResponseBody
	public String test3() throws IOException{
		String s = FileUtil.getText("introduce.txt");
		log.info("心跳测试"+s);
		return "通过了test2"+s;
	}
}
