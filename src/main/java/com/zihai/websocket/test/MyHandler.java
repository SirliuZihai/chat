package com.zihai.websocket.test;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

public class MyHandler extends AbstractWebSocketHandler {
	Logger log =  LoggerFactory.getLogger(getClass());
	public Map<String, Session> clients = new ConcurrentHashMap<>();

	protected void handleTextMessage(WebSocketSession session,TextMessage message)throws Exception{
		log.debug(" Received message:"+ message.getPayload());
		session.sendMessage(message);
	}
	
	@Override
	public  void afterConnectionEstablished(WebSocketSession session){
		clients.put(session.getId(), (Session) session);
		log.debug(session.getRemoteAddress()+"esablished");
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status){
		clients.remove(session.getId(), (Session) session);
		log.debug(session.getRemoteAddress()+"closed");

		
	}
}
