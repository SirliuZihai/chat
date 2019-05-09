package com.zihai.websocket;


import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zihai.event.service.EventService;
import com.zihai.util.EncrypUtil;

public class HomeEventHandler extends AbstractWebSocketHandler {
	@Autowired
	private EventService eventService;
	Logger log =  LoggerFactory.getLogger(getClass());
	public Map<String, WebSocketSession> clients = new ConcurrentHashMap<>();

	protected void handleTextMessage(WebSocketSession session,TextMessage message)throws Exception{
		 String username =  EncrypUtil.getUserName(getParams(session).get("token"));
		if("heartbeat[myapp]".equals(message.getPayload())){
			//log.debug("heartbeat from "+username);
			 session.sendMessage(new TextMessage("0003"));
			return;
		}
		if("receive Home date".equals(message.getPayload())){
			 Document event = new Document().append("username", username);
			 @SuppressWarnings("unchecked")
			 List<Document> list = eventService.queryNoSendEvent(username);		
			 String result = new ObjectMapper().writeValueAsString(list);
			 session.sendMessage(new TextMessage("0000"+result));
			 List<Document> list2 = eventService.queryNoSendMessage(username);		
			 String result2 = new ObjectMapper().writeValueAsString(list2);
			 session.sendMessage(new TextMessage("0001"+result2));
			 
			 Set<ObjectId> inArray = new HashSet<ObjectId>();
			 for(Document d : list){
				 inArray.add(new ObjectId(d.getString("_id")));
			 }
			 for(Document d : list2){
				 inArray.add(new ObjectId(d.getString("_id")));
			 }
			 eventService.updateEventState(new Document().append("username",username)
			 .append("eventId", new Document().append("$in", inArray)));
		 }
	}
	@Override
	public  void afterConnectionEstablished(WebSocketSession session) throws IOException{
		String username =  EncrypUtil.getUserName(getParams(session).get("token"));
		if(clients.containsKey(username))
			clients.get(username).close();
		clients.put(username, (WebSocketSession) session);
		log.debug(username+"esablished");
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status){
		String username =  EncrypUtil.getUserName(getParams(session).get("token"));
		clients.remove(username);
		log.debug(username+"closed");
	}
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		session.close();
	}
	public static Map<String,String> getParams(WebSocketSession session){
		Map<String,String> m = new HashMap<String,String>();
		String query = session.getUri().getQuery();
		if(StringUtils.isNotEmpty(query)){
			for(String s : query.split("&")){
				String[] s_ = s.split("=");
				if(s_.length>1)
					m.put(s_[0], s_[1]);		
			}
		}
		return m;
	}
}
