package com.zihai.shiro.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.alibaba.fastjson.JSON;
import com.zihai.util.Result;

public interface UserService {
	public Boolean createUser(Map user); //创建账户
	public Boolean updateUser(Map user); //更新账户
	public Document findUser(Document user);
	public void correlationRoles(Long userId, Long... roleIds); //添加用户-角色关系
	public void uncorrelationRoles(Long userId, Long... roleIds);// 移除用户-角色关系
	/**
	 * 根据用户名查找用户
	 * */
	public Map findByUsername(String username);
	/**
	 * 根据用户名查找用户信息
	 * */
	public Map findInfoByUsername(String username);
	/**
	 * 更新userInfo
	 * */
	public void updateOrInsertUserInfo(Map user);
	public List<Map> searchUser(Map userInfo);
	public List<Map> getRelationUser(HashMap userInfo);
	/**
	 * 获得标签
	 * */
	public List<String> getRelationTags();
	public void updateRelation(Map relation);
	public void addrelation(Map relation);
	public Map findInfoByUsername2(String string);
	public void removeRelation(Map relation); 
}
