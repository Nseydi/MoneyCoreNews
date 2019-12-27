package com.moneyCoreNews.mailing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailContentBuilder {

    private TemplateEngine templateEngine;
 
    @Autowired
    public MailContentBuilder(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
 
    public String build(String expirationMsg,String message,String actionName,String url,String name,String location,String signature) {
        Context context = new Context();
        context.setVariable("message", message);
        context.setVariable("expirationMsg", expirationMsg);
        context.setVariable("name", name);
        context.setVariable("location", location);
        context.setVariable("signature", signature);
        context.setVariable("actionName", actionName);
        context.setVariable("url", url);
        return templateEngine.process("EmailTemplate", context);
        
    }
 
}