package com.zihai.websocket.test;

import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zihai.chat.entity.Message;
import com.zihai.websocket.util.SessionUtils;



/**
 * 功能说明：websocket处理类, 使用J2EE7的标准
 * 切忌直接在该连接处理类中加入业务处理代码
 * 作者：liu(2014-11-14 04:20)
*/
//relationId和userCode是我的业务标识参数,websocket.ws是连接的路径，可以自行定义
@ServerEndpoint("/websocket/{relationId}/{userCode}")
@Component
public class WebsocketEndPoint {

 private Log log = LogFactory.getLog(WebsocketEndPoint.class);
 
/* WebsocketEndPoint(){
	 log.info("websockt JAVAEE7 initialized");
 }*/

/**
 * 打开连接时触发
 * @param relationId
 * @param userCode
 * @param session
*/
@OnOpen
 public void onOpen(@PathParam("relationId") String relationId,
 @PathParam("userCode") String userCode, Session session){
	SessionUtils.put(relationId, userCode, session);
	System.out.println(relationId+userCode);
}

/**
 * 收到客户端消息时触发
 * @param relationId
 * @param userCode
 * @param message
 * @return
 * @throws JsonProcessingException 
*/
@OnMessage
 public void onMessage(@PathParam("relationId") String relationId,
 @PathParam("userCode") String userCode,String text) throws JsonProcessingException {
 if("heartbeat[myapp]".equals(text)){
	 log.info("heartbeat from "+userCode);
	 return;
 }
 String str = new ObjectMapper().writeValueAsString(new Message(userCode,text));
 for(Session session :SessionUtils.getRelativeSession(relationId)){
	 session.getAsyncRemote().sendText(str);
 }
}

/**
 * 异常时触发
 * @param relationId
 * @param userCode
 * @param session
 * @throws IOException 
*/
@OnError
 public void onError(@PathParam("relationId") String relationId,
 @PathParam("userCode") String userCode,Throwable throwable,Session session) throws IOException {
	SessionUtils.remove(relationId, userCode);
	if(session!=null){
		session.close();
	}
  log.info("error and close :"+userCode);
}

/**
 * 关闭连接时触发
 * @param relationId
 * @param userCode
 * @param session
*/
@OnClose
 public void onClose(@PathParam("relationId") String relationId,
 @PathParam("userCode") String userCode,
 Session session) {
	SessionUtils.remove(relationId, userCode);
 System.out.println("close");
}

}