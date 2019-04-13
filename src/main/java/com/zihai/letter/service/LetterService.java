package com.zihai.letter.service;

import java.util.List;
import java.util.Map;

import org.bson.Document;

public interface LetterService {
	/**
	 * 收件 
	 * @deprecated
	 * */
	public List getLetters();
	/**
	 * 寄件
	 * */
	public void sendLetter(String username, Document letter);
	/**
	 * 查看他们信箱
	 * */
	public List getOtherLetters(String othername);
	/**
	 * */
	public List getLetterBox(Document position);
	/**
	 * 删除信件
	 * */
	public void delete(Map<String, String> data);
	/**
	 * 设置信箱位置
	 * */
	public void setBoxPlace(String username, Document position);
	/**
	 * 新建信箱位置
	 * */
	void setUpBox(String username, Document position);

}
