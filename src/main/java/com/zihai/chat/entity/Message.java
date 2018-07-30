package com.zihai.chat.entity;

public class Message {
	private String name;
	private String text;
	private String avatar;
	
	public Message(String name,String text){
		this.name = name;
		this.text = text;
		this.avatar = "http://192.168.137.1/myapp/libs/image/"+name+".jpg";
			
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
}
