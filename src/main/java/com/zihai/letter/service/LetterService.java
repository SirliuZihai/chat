package com.zihai.letter.service;

import java.util.List;

import org.bson.Document;

public interface LetterService {
	/**
	 * 收件
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

}
