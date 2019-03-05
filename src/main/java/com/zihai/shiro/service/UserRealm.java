package com.zihai.shiro.service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class UserRealm extends AuthorizingRealm {
	private Logger log = LoggerFactory.getLogger(getClass());
	@Autowired
	private UserService userService;
	
	@Override
	//@Caching(cacheable={@Cacheable(cacheNames="auth",key="#principals.getPrimaryPrincipal()")}
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		String username = (String)principals.getPrimaryPrincipal();
		Map user = userService.findByUsername(username);
		String[] roles = ((String)user.get("auth")).substring(1).split(";");
		HashSet<String> set_role = new HashSet<String>();
		HashSet<String> set_perm = new HashSet<String>();
		Properties pro = null;
		try {
			pro = PropertiesLoaderUtils.loadAllProperties("property/shiro.properties");
		} catch (IOException e) {
			log.info("shiro.properties not found");
			return null;
		}
		for(String role : roles){
			boolean b = set_role.add(role);
			log.info(role+"has added :"+ b);
			if(StringUtils.isNotEmpty(pro.getProperty(role)))
				for(String perm :pro.getProperty(role).split(",")){
					set_perm.add(perm);log.info(perm+"is added");
				}
					
		}
		SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
		authorizationInfo.setRoles(set_role);
		authorizationInfo.setStringPermissions(set_perm);
		return authorizationInfo;
	}
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		String username = (String)token.getPrincipal();
		Map user = userService.findByUsername(username);
		if(user == null) 
			throw new UnknownAccountException();//没找到帐号
		String auth = (String)user.get("auth");
		if(auth != null&&auth.startsWith("0")) 
			throw new LockedAccountException(); //帐号锁定
		
		//交给 AuthenticatingRealm 使用 CredentialsMatcher 进行密码匹配，如果觉得人家的不好可以在此判断或自定义实现
		SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(((String)user.get("username")), //用户名
				((String)user.get("password")), getName() );//realm name
		log.info("passowrd ==== "+((String)user.get("password")));
		return authenticationInfo;
	}
}
