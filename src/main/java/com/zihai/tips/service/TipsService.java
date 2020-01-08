package com.zihai.tips.service;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

public interface TipsService {
	public void addTip(Document document);
	public void deleteTip(Document document);
	/**
	 * 空方法
	 * */
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
	 * document{_id:'',commentId:'',replyId:'',like:true}+ username
	 * */
	public void like(Document document);
	/**
	 * 增加评论/回复
	 * document {_id:'',commentId:'',content:''} + username
	 * @return 
	 * 		操作的id
	 * */
	public String addComment(Document document);
	/**
	 * 删除评论/回复
	 * document{_id:'',commentId:'',replyId:''} + username
	 * @param 
	 */
	public void removeComment(Document document);
	
}