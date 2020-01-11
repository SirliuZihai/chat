package com.zihai.event.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface EventService {
	/**
	 * 录入消息
	 * 向他人发送操作信息将自已排除在外
	 * @throws JsonProcessingException 
	 * @throws IOException 
	 * */
	public void insertMessage(Document message) throws IOException;
	

	/**
	 * 删除事件  （如果是本人创建的，直接删除，否则，删除关联人。）
	 * */
	public void deleteEvent(String username ,String eventId);
	/**
	 * 查询事件
	 * @param username 
	 * */
	public List queryHistroy(Map event, String username);
	/**
	 * 查询示所有发送事件
	 * */
	public List<Document> queryNoSendEvent(String username);
	/**
	 * 查询示所有发送的消息
	 * */
	public List<Document> queryNoSendMessage(String username);

	
	public void updateEventState(Document filter);
	/**
	 * 接受邀请
	 * operMan 将 addMan 加入事件中
	 * */
	public void addRelation(String _id,String addMan,String operMan) throws IOException;
	/**
	 * 保存事件，联系人邀请
	 * */
	public void save(Document event) throws IOException;
	/**
	 * 保存事件，联系人强拉
	 * */
	public void saveAndRelate(Document event) throws IOException;
	/**
	 * 查询事件,
	 * @return no found is null
	 * */
	public Document getEventById(String eventId);


	boolean hasInEvent(ObjectId eventId, String sender);



}
