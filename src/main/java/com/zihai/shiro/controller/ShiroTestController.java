package com.zihai.shiro.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("shiro")
public class ShiroTestController {
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@RequestMapping("test1")@RequiresRoles("admin")@ResponseBody
	public String test1(){
		log.info(SecurityUtils.getSubject().getPrincipal()+"拥有admin角色  通过了test1");
		return "通过了test1";
	}
	
	@RequestMapping("test2")
	@RequiresPermissions("account:create")
	@ResponseBody
	public String test2(){
		log.info(SecurityUtils.getSubject().getPrincipal()+"拥有account:create权限  通过了test2");
		return "通过了test2";
	}
}
