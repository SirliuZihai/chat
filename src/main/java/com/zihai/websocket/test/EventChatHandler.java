package com.zihai.websocket.test;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import com.zihai.event.service.EventService;

public class EventChatHandler extends AbstractWebSocketHandler {
	@Autowired
	private EventService eventService;
	Logger log =  LoggerFactory.getLogger(getClass());
	public Map<String, WebSocketSession> clients = new ConcurrentHashMap<>();

	protected void handleTextMessage(WebSocketSession session,TextMessage message)throws Exception{
		 String eventId = getParams(session).get("eventId");
		 String username = session.getPrincipal().toString();
		if("heartbeat[myapp]".equals(message.getPayload())){
			log.debug("heartbeat from "+session.getPrincipal().toString());
			return;
		}
		eventService.insertMessage(new Document("sender",username).append("data", message.getPayload())
				.append("relateId", new ObjectId(eventId)));
	}
	@Override
	public  void afterConnectionEstablished(WebSocketSession session){
		String eventId = getParams(session).get("eventId");
		clients.put(eventId +"|"+ session.getPrincipal().toString(), session);
		log.debug(eventId +"|"+ session.getPrincipal()+" is esablished");
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status){
		String eventId = getParams(session).get("eventId");
		clients.remove(eventId +"|"+ session.getPrincipal().toString());
		log.debug(eventId +"|"+ session.getPrincipal()+" is closed");
	}
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		session.close();
	}
	public static Map<String,String> getParams(WebSocketSession session){
		Map<String,String> m = new HashMap<String,String>();
		String query = session.getUri().getQuery();
		if(StringUtils.isNotEmpty(query)){
			for(String s : query.split("%")){
				String[] s_ = s.split("=");
				if(s_.length>1)
					m.put(s_[0], s_[1]);		
			}
		}
		return m;
	}
}