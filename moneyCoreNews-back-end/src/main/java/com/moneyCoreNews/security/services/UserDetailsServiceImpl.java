package com.moneyCoreNews.security.services;

import com.moneyCoreNews.model.AUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserService userService;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AUser user=userService.findUserByEmail(email);

        if(user==null) throw new UsernameNotFoundException(email);
        Collection<GrantedAuthority> authorities=new ArrayList<GrantedAuthority>();
        user.getRoles().forEach(r->{

            authorities.add(new SimpleGrantedAuthority(r.getRole()));
        });
        return new User(user.getEmail(), user.getPassword(), authorities); //ce User là est un user Spring
    }
}
