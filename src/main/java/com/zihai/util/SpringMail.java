package com.zihai.util;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;
import javax.mail.search.SearchTerm;

import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StreamUtils;

public class SpringMail {
	private JavaMailSenderImpl sender =new JavaMailSenderImpl();;
	private  Properties property = new Properties();
	
	{
		//读数据
		InputStream stream = getClass().getClassLoader().getResourceAsStream("property/db.properties");
		try {
			property.load(stream);
		} catch (IOException e) {
			System.out.println("load property error");
		}
		sender.setHost(property.getProperty("mail.host"));
		sender.setUsername(property.getProperty("username"));
		sender.setPassword(property.getProperty("password"));
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
    
    /**
     * 
     * @throws IOException 
     * @throws MessagingException 
     * @throws InterruptedException 
     * */
	public void receive() throws IOException, MessagingException, InterruptedException  {
		InputStream stream = getClass().getClassLoader().getResourceAsStream("property/db.properties");
		Properties property = new Properties();
		property.load(stream);
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", property.getProperty("protocol"));
		props.setProperty("mail.pop3.host", property.getProperty("mail.host")); 
		Session session=Session.getInstance(props);
		//从会话对象中获得POP3协议的Store对象  
		Store store = session.getStore("pop3"); 
		//连接邮件服务器  
		store.connect(property.getProperty("mail.host"), property.getProperty("username"), property.getProperty("password"));
		 Folder folder = store.getFolder("INBOX"); //固定的
		//循环处理
		while(true){
			try {
				 folder.open(Folder.READ_WRITE); 
				 System.out.println(folder.getURLName());
				 Message[] message = null;
				 //过滤 folder.getMessages(); OR搜索
				 SearchTerm term = new SearchTerm() {
					@Override
					public boolean match(Message msg) {
						try {
							return  msg.getSubject().startsWith("测试");
						} catch (MessagingException e) {
							e.printStackTrace();
						}
						return true;
					}
				};;
				
				 doMessages(folder.search(term));} 
			catch (Exception e) {
				System.out.println("receive errr");
				e.printStackTrace();}
			finally{
				folder.close(true);
				store.close(); 
			}
			Thread.sleep(5000);
		}
	}
	
	/**
	 * pop3并不支持Flag判断已读，只能是吧邮件的uid取出来，然后进行比较如果UID存在说明是读过的邮件，不存在说明是新邮件
	 * Flags flags = message.getFlags();
	 * if (flags.contains(Flags.Flag.SEEN)) 
	 * 		System.out.println("这是一封已读邮件");
	 * else {
	 * 		System.out.println("未读邮件");
	 * 		message.setFlag(Flags.Flag.SEEN, true);
	 * } 
	 * used for POP3
	 * */
	public void doMessages(Message[] messages) throws MessagingException, IOException{
		for(Message message :messages){   
            System.out.println(message.getSubject());
			Multipart mp = (Multipart)message.getContent();  
			/*for (int i=0, n=mp.getCount(); i<n; i++) {
				BodyPart part = mp.getBodyPart(i);
				System.out.println("getContentType=="+part.getContentType());
				String disposition = part.getDisposition();   
				if (disposition != null &&(disposition.equals(Part.ATTACHMENT) ||(disposition.equals(Part.INLINE)))) {     
					System.out.println("has a "+disposition);
					continue;
					//saveFile(part.getFileName(), part.getInputStream());  
				 }
				System.out.println("the part"+i+"=="+part.getContent().toString());

			}*/
			message.setFlag(Flags.Flag.DELETED, false);//是滞删除
			//reply
			MimeMessage message_re = (MimeMessage) message.reply(false);//只回复sender
			MimeMessageHelper helper = new MimeMessageHelper(message_re, true);
			FileSystemResource res = new FileSystemResource(new File("c://test.docx"));
			helper.addAttachment("test.docx", res);
			Multipart old = (Multipart)message.getContent();
			String old_content="";
			for(int i=0 ; i<old.getCount();i++){
				BodyPart part = old.getBodyPart(i);
				System.out.println("getContentType=="+part.getContentType());
				if("text/plain".equals(part.getContentType()))
					old_content +="get"+part.getContent().toString();
			}
			//mp.addBodyPart(new BodyPart(), 0);
			helper.setText("回复内容" + "\r\n\r\n————————\r\n" +old_content);
			System.out.println("old_ontent=="+old_content);
			 // 设置收件人：原发件人
			helper.setTo((InternetAddress[]) message_re.getAllRecipients());
			helper.setFrom(property.getProperty("from"));
			//sender.send(message_re);System.out.println("已回复");
		}
	}
	public static void main(String[] args) throws MessagingException, IOException, InterruptedException {
		SpringMail mail = new SpringMail();
		
		new Thread(()->{try {
			mail.receive();
		} catch (Exception e) {
			e.printStackTrace();
		}}).start();
		Thread.sleep(3000);
		System.out.println("send mail");
		//new SpringMail().send();
	}
}

