package com.moneyCoreNews.mailing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring4.SpringTemplateEngine;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;


@Service("emailService1")   
public class EmailService1 {

	private JavaMailSender mailSender;
	@Autowired
	private MailContentBuilder1 mailContentBuilder1;
	
	 @Value("${dir.logos}")
	  private String pathlogo;
	
	@Autowired
	public EmailService1(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
	@Autowired
    private SpringTemplateEngine templateEngine;
	@Async
	public void sendEmail(SimpleMailMessage email) {
		mailSender.send(email);
	}
	
	 public void prepareAndSend(String subject, String recipient, String expirationMsg, String messageBody, 
			 Map<String,Map<String,String>> messages, String actionName, String url, String name, String location, 
			 String signature) {
		 
		 MimeMessagePreparator messagePreparator = mimeMessage -> {
	            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, 
	            		MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
		                StandardCharsets.UTF_8.name());
	            FileSystemResource file = new FileSystemResource(new File(pathlogo+"logo_kakoo.png"));
	            messageHelper.addAttachment("logo_kakoo.png", file);
	            messageHelper.setTo(recipient);
	            messageHelper.setFrom("noreply@domain.com");
	            messageHelper.setSubject(subject);
	      
	            String content = mailContentBuilder1.build(expirationMsg, messageBody, messages, actionName, url, name, location, 
	            		signature);
	            messageHelper.setText(content, true);

	        };
	  
	        try {
	            mailSender.send(messagePreparator);
	        } catch (MailException e) {
	            // runtime exception; compiler will not force you to handle it
	        }
	    }
	 
	 
	 
	 
	 
}
