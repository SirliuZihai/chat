package com.zihai.shiro.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.zihai.util.Result;

public interface UserService {
	public Boolean createUser(Map user); //创建账户
	public Boolean updateUser(Map user); //创建账户
	public Boolean changePassword(Map user);//修改密码
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
	public Boolean updateOrInsertUserInfo(Map user);
	public List<Map> searchUser(Map userInfo);
	public List<Map> getRelationUser(HashMap userInfo);
	public void updateRelation(Map relation);
	public void addrelation(Map relation);
	public Map findInfoByUsername2(String string);
	public void removeRelation(Map relation); 
}