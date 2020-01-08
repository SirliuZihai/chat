package com.zihai.notify.service;

import java.util.List;

import org.bson.Document;

public interface NotifyService {
	/**
	 * 获取待处理消息统计
	 * {type:notes|operate , state: 0}
	 * 0:点赞提示 1:评论提示    5申请加入操作6邀请加入操作
	 * state	0待处理；1忽略，2：接受；3 拒绝
	 * */
	public Document countNofify(String currentUser);
	/**
	 * 获取分类信息
	 * {type:notes|operate} 
	 * 0:点赞提示 1:评论提示    5申请加入操作6邀请加入操作
	 * {type:'notes',skipNum:0} + username
	 * */
	public List<Document> queryNofify(Document filter);
	/**
	 * 添加提示
	 * notify	用于点赞通知；评论通知，申请加入处理
		data
		sender
		receiver
		state 0待处理；1忽略，2：接受；3 拒绝
		type 1:评论提示    5申请加入操作6邀请加入操作
	 */
	public void addNotify(Document doc);
	/**
	 * 全部忽略   notes当点击时忽略
	 * * {type:notes|operate}
	 * 0:点赞提示 1:评论提示    2申请加入操作3邀请加入操作
	 */
	public void ignoreNotify(Integer type,String username);
	/**
	 * 改变操作状态
	 * 0待处理；1忽略，2：接受；3 拒绝
	 * */
	public void stateNotify(String id,Integer state,String username);
}
