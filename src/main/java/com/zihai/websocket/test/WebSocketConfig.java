package com.zihai.websocket.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
/**
 * 使用springboot的唯一区别是要@Component声明下，而使用独立容器是由容器自己管理websocket的，但在springboot中连容器都是spring管理的。
 * */
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
@EnableWebSocket
@Configuration
public class WebSocketConfig implements WebSocketConfigurer 
{
	Logger log =  LoggerFactory.getLogger(getClass());
	
	/**内置容器添加扫描serverEndPoint*/
	@Bean
	public ServerEndpointExporter serverEndpointExporter(){
		log.info("do serverEndpointExporter……");
		return new ServerEndpointExporter();
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		log.info(getClass().getSimpleName()+" registring");
		registry.addHandler(marchHandler(), "/websocket/homeview.do")
		.setAllowedOrigins("*");
	}
	
	@Bean
	public HomeEventHandler marchHandler(){
		return new HomeEventHandler();
	}
}

