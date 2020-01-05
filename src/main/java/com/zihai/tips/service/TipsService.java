package com.zihai.tips.service;

import java.util.List;

import org.bson.Document;

public interface TipsService {
	public void addTip(Document document);
	public void deleteTip(Document document);
	public void updateTip(Document document);
	/**
	 * 
	 * 查该用户可见动态
	 * 		规则：事件ID+自已发的+已关注的+公开状态||参与的||属于标签
	 * */
	public List queryTip(Document document,String username);
	/**
	 * 点赞/取消
	 * 
	 * type{tip comment}
	 * */
	public void support(Document document);
	
	public void addCommend(Document document);
	
	public void removeCommend(Document document);
	
}
