package com.moneyCoreNews;

import com.moneyCoreNews.model.AUser;
import com.moneyCoreNews.model.AppRole;
import com.moneyCoreNews.security.repository.AUserRepository;
import com.moneyCoreNews.security.repository.AppRoleRepository;
import com.moneyCoreNews.security.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import java.util.ArrayList;
import java.util.List;

@EnableSwagger2
@ComponentScan(basePackages = {"com.moneyCoreNews","com.moneyCoreNews.api" })
@SpringBootApplication
@Configuration
public class MoneyCoreNewsApplication extends SpringBootServletInitializer implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(MoneyCoreNewsApplication.class);

    @Autowired
    private AUserRepository userRep;

    @Autowired
    private AppRoleRepository appRoleRepository;

    @Autowired
    private UserService userService;


    @Value("${appADMIN.email}")
    private String appAdminEmail;
    @Value("${appADMIN.lastName}")
    private String appAdminlastName;
    @Value("${appADMIN.firstName}")
    private String appAdminfirstName;
    @Value("${appADMIN.phone}")
    private String appAdminphone;
    @Value("${appADMIN.username}")
    private String appAdminUserName;
    @Value("${appADMIN.password}")
    private String appAdminPwd;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MoneyCoreNewsApplication.class);
    }

    public static void main(String[] args) {

        SpringApplication.run(MoneyCoreNewsApplication.class, args);

    }

    @Bean
    public BCryptPasswordEncoder getBCryptedPassword() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {

        //**********creation of the appADMIN user
        List<String> listEmails = new ArrayList<>();
        userRep.findAll().forEach(n -> {
            listEmails.add(n.getEmail());
        });

        if (!listEmails.contains(appAdminEmail)) {

            AUser aUser = new AUser(appAdminEmail, appAdminUserName, appAdminPwd, appAdminfirstName, appAdminlastName, appAdminphone);
            aUser.setEnabled(true);
            userService.saveUser(aUser);

            AppRole role = userService.getRoleIfElseSaveAndGet("ADMIN");
            userService.addRoleToAUser(aUser.getEmail(), role);
        }
    }
}
