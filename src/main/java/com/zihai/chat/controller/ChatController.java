package com.zihai.chat.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.zihai.websocket.util.SessionUtils;

@Controller
public class ChatController {
	Logger log =  LoggerFactory.getLogger(getClass());
	ChatController(){
		log.info(getClass().getSimpleName()+"initialized");
	}
	
	@RequestMapping("/pages/{args}")
	public String test(@PathVariable("args") String arg1){
		log.info(arg1);
		if(arg1.equals("index"))
			return "www/"+arg1;
		return arg1;
	}
	@RequestMapping("/test2.do")
	public String test2(){
		log.info("index4");
		return "index4";
	}
	/**
	 * @param relationId
	 * @param userCode
	 * @param message
	*/
	@RequestMapping("/sendmessage")
	//localhost:8080/coder/hellow/sendmessage.do?relationId=zihai&userCode=007&message=sended
	 public void broadcast(String relationId, String userCode, String message) {
	 if (SessionUtils.hasConnection(relationId, userCode)) {
	SessionUtils.get(relationId, userCode).getAsyncRemote().sendText(message);
	 } else {
	 throw new NullPointerException(SessionUtils.getKey(relationId, userCode) +"Connection does not exist");
	}

	}
}
