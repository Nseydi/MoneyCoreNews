package com.moneyCoreNews.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneyCoreNews.MoneyCoreNewsApplication;
import com.moneyCoreNews.model.AUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final Logger logger = LoggerFactory.getLogger(MoneyCoreNewsApplication.class);

    @Autowired
    private AuthenticationManager authenticationManager;


    public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
        super();
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        AUser aUser = null;

        try {
            aUser = new ObjectMapper().readValue(request.getInputStream(), AUser.class); //deserialise les données du JSON vers données normals

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.info("**************");
        logger.info("username: " + aUser.getUsername());
        logger.info("**************");
        UsernamePasswordAuthenticationToken user = new UsernamePasswordAuthenticationToken(aUser.getUsername(), aUser.getPassword());


        return authenticationManager.authenticate(user);

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        User springUser = (User) authResult.getPrincipal(); //on utilise les infos du springUser pour generer le Token

        String jwt = Jwts.builder()
                .setSubject(springUser.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SecurityConstants.SECRET)
                .claim("roles", springUser.getAuthorities())
                .compact();

        response.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + jwt);
    }

}
