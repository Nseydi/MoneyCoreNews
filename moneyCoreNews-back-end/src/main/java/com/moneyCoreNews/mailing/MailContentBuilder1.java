package com.moneyCoreNews.mailing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class MailContentBuilder1 {

    private TemplateEngine templateEngine;
 
    @Autowired
    public MailContentBuilder1(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
 
    public String build(String expirationMsg,String messageBody,Map<String,Map<String,String>> messages,String actionName,String url,String name,String location,String signature) {
        Context context = new Context();
        context.setVariable("messages", messages);
        context.setVariable("messageBody", messageBody);
        context.setVariable("expirationMsg", expirationMsg);
        context.setVariable("name", name);
        context.setVariable("location", location);
        context.setVariable("signature", signature);
        context.setVariable("actionName", actionName);
        context.setVariable("url", url);
        return templateEngine.process("EmailTemplate1", context);
        
    }
 
}