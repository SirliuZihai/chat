package com.zihai.util;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.SearchTerm;

import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

public class SpringMail {
	private JavaMailSenderImpl sender =new JavaMailSenderImpl();;
	private  Properties property = new Properties();
	
	{
		//读数据
		InputStream stream = getClass().getClassLoader().getResourceAsStream("properties/email.properties");
		try {
			property.load(stream);
		} catch (IOException e) {
			System.out.println("load property error");
		}
		sender.setHost(property.getProperty("host"));
		sender.setUsername(property.getProperty("username"));
		sender.setPassword(property.getProperty("password"));
		sender.setJavaMailProperties(property);
	}
	
	public String sendCode(String mailName,String code)throws MessagingException, IOException{
		MimeMessage message = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, false);
		helper.setTo(mailName);
		helper.setFrom(property.getProperty("username"));
		helper.setCc(property.getProperty("username"));
		helper.setText(code);
		helper.setSubject("Shadow 验证码");
		sender.send(message);
		return mailName;
	}
	
	
	
	/**
	 * 带图片，附件  邮件协议有(小写),smtp（发送）、pop3（接收）、imap4（接收）,它们都隶属于TCP/IP协议簇,默认状态下,分别通过TCP端口25、110和143建立连接
	 * @throws IOException 
	 * */
	public void send() throws MessagingException, IOException{
		MimeMessage message = sender.createMimeMessage();
		// use the true flag to indicate you need a multipart message
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setTo(property.getProperty("sendTo"));
		helper.setFrom(property.getProperty("from"));
		// use the true flag to indicate the text included is HTML
		helper.setText(property.getProperty("Text"));
		helper.setSubject(property.getProperty("Subject"));
		FileSystemResource res = new FileSystemResource(new File("c://test.docx"));
		//helper.addInline("identifier1234", res);
		//附件
		helper.addAttachment("doc", res);
		sender.send(message);
		
	}
	
	
	public static void main(String[] args) throws MessagingException, IOException, InterruptedException {
		SpringMail mail = new SpringMail();
		mail.sendCode("345760650@qq.com","TEXT3");
	}
}

