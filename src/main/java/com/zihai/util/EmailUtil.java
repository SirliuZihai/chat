package com.zihai.util;
//
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;


//
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;



/**
 * 发送邮件的公用方法.
 */
public class EmailUtil {

	/**
	 * 发送邮件给单个收件人.
	 * 
	 * @param smtpHost the SMTP email server address
	 * @param senderAddress the sender email address
	 * @param senderName the sender name
	 * @param receiverAddress the recipient email address
	 * @param sub the subject of the email
	 * @param msg the message content of the email
	 */
	public static void sendEmail(String smtpHost, String senderAddress, String senderName,
					String receiverAddress, String sub, String msg){
		List<String> recipients = new ArrayList<String>();
		recipients.add(receiverAddress);

		sendEmail(smtpHost, senderAddress, senderName, recipients, sub, msg);
	}
	/**
	 * 发送邮件给多个收件人
	 * 不带密码
	 * @param smtpHost
	 * @param senderAddress
	 * @param password
	 * @param senderName
	 * @param recipients
	 * @param sub
	 * @param msg
	 */
	public static void sendEmail(String smtpHost, String senderAddress, String senderName,
			List<String> recipients, String sub, String msg){
		sendEmail(smtpHost, senderAddress, senderName, null, null, recipients, sub, msg);
	}
	/**
	 * 发送邮件给多个收件人
	 * 
	 * @param smtpHost the SMTP email server address
	 * @param senderAddress the sender email address
	 * @param senderName the sender name
	 * @param recipients a list of receipients email addresses
	 * @param sub the subject of the email
	 * @param msg the message content of the email
	 */
	public static void sendEmail(String smtpHost, String senderAddress, String senderName, String accountName, String password,
					List<String> recipients, String sub, String msg){
		if (smtpHost == null) {
			String errMsg = "不能发送邮件: smtp 服务器地址为空";
			throw new RuntimeException(errMsg);
		}

		System.out.println(senderAddress);
		System.out.println(smtpHost);
		
		try {
			Properties props = System.getProperties();
			props.put("mail.smtp.host", smtpHost);
			props.put("mail.debug","true");
			Session session = null ;
			
			final String user = accountName;
			final String pwd  = password;
			System.out.println(user);
			System.out.println(pwd);
			if(user!=null&&pwd!=null){  
				props.put("mail.smtp.auth", "true");
				props.put("mail.smtp.user", user);// 自己信箱的用户名
				props.put("mail.smtp.password", pwd);// 信箱密码
				//System.out.println("auth=true");
			}else{
				props.put("mail.smtp.auth", "false");
				//System.out.println("auth=false");
				//session = Session.getDefaultInstance(props, null );
			}
	            session = Session.getInstance(props, new javax.mail.Authenticator() {
	                protected PasswordAuthentication getPasswordAuthentication() {
	                    return new PasswordAuthentication(user, pwd);
	                }
	            });
			
			MimeMessage message = new MimeMessage( session );
			message.addHeader("Content-type", "text/plain");
			message.setSubject(sub);
			message.setFrom(new InternetAddress(senderAddress, senderName));

			for (Iterator<String> it = recipients.iterator(); it.hasNext();) {
				String email = (String)it.next();
				message.addRecipients(Message.RecipientType.TO, email);
			}

			message.setText(msg);
			message.setSentDate( new Date() );
			Transport.send(message);
		 } catch (Exception e) {
		 	//String errorMsg = "邮件发送失败";
			throw new RuntimeException(e);
		 }
	}
	/**
	 * 发送HTML格式邮件给多个收件人
	 * 
	 * @param smtpHost the SMTP email server address
	 * @param senderAddress the sender email address
	 * @param senderName the sender name
	 * @param recipients a list of receipients email addresses
	 * @param sub the subject of the email
	 * @param msg the message content of the email
	 */
	public static void sendHtmlMail(String smtpHost, String senderAddress, String senderName, String accountName, String password,
					List<?> recipients, String sub, String msg){
		if (smtpHost == null) {
			String errMsg = "不能发送邮件: smtp 服务器地址为空";
			throw new RuntimeException(errMsg);
		}

		System.out.println(senderAddress);
		System.out.println(smtpHost);
		
		try {
			Properties props = System.getProperties();
			props.put("mail.smtp.host", smtpHost);
			props.put("mail.debug","true");
			Session session = null ;
			
			final String user = accountName;
			final String pwd  = password;
			System.out.println(user);
			System.out.println(pwd);
			if(user!=null&&pwd!=null){  
				props.put("mail.smtp.auth", "true");
				props.put("mail.smtp.user", user);// 自己信箱的用户名
				props.put("mail.smtp.password", pwd);// 信箱密码
				//System.out.println("auth=true");
			}else{
				props.put("mail.smtp.auth", "false");
				//System.out.println("auth=false");
				//session = Session.getDefaultInstance(props, null );
			}
	            session = Session.getInstance(props, new javax.mail.Authenticator() {
	                protected PasswordAuthentication getPasswordAuthentication() {
	                    return new PasswordAuthentication(user, pwd);
	                }
	            });
			
			MimeMessage message = new MimeMessage( session );
			message.addHeader("Content-type", "text/html");
			message.setSubject(sub);
			message.setFrom(new InternetAddress(senderAddress, senderName));

			for (Iterator<?> it = recipients.iterator(); it.hasNext();) {
				String email = (String)it.next();
				message.addRecipients(Message.RecipientType.TO, email);
			}
            Multipart mp = new MimeMultipart(); 
            BodyPart bp = new MimeBodyPart();
            bp.setContent(msg, "text/html;charset=GBK");
            mp.addBodyPart(bp);
			message.setContent(mp);
			message.setSentDate( new Date() );
			Transport.send(message);
		 } catch (Exception e) {
		 	//String errorMsg = "邮件发送失败";
			throw new RuntimeException(e);
		 }
	}
}
