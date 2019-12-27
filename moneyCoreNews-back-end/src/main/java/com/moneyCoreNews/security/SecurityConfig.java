package com.moneyCoreNews.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled=true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder);
    }

    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();

        //*****the actions authorized only to the RH
        http.authorizeRequests().antMatchers(HttpMethod.DELETE, "/users/**").hasAuthority("appADMIN");//


        //*****the actions authorized only to the ADMIN
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);  // desable session authentification
        http.authorizeRequests().antMatchers(HttpMethod.POST, "/users").hasAuthority("appADMIN");//
        //http.authorizeRequests().antMatchers(HttpMethod.DELETE,"/users/**").hasAuthority("ADMIN");
        http.authorizeRequests().antMatchers(HttpMethod.DELETE, "/users/updateUser/{Userid}").hasAuthority("appADMIN");//

        //******the actions authorized only to the RH

        //http.authorizeRequests().antMatchers(HttpMethod.POST,"/users").hasAuthority("RH");
        //http.authorizeRequests().antMatchers(HttpMethod.DELETE,"/users/{email}").hasAuthority("RH");
        http.authorizeRequests().antMatchers(HttpMethod.DELETE, "/users/updateUser/{Userid}").hasAuthority("RH");//

        http.authorizeRequests()
                .antMatchers("/**", "css/**", "/js/**", "/login/**", "/users/register/**").permitAll()
                .antMatchers("/v2/api-docs/**", "/configuration/ui", "/swagger-resources", "/configuration/security", "/swagger-ui.html/**", "/swagger-ui.html#!/**", "/webjars/**", "/swagger-resources/**", "/configuration/**", "/external-config-files/countries.json").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .addFilter(new JWTAuthenticationFilter(authenticationManager())) //Add Filter ,in fact it's two filters
                .addFilterBefore(new JWTAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);

    }

}
