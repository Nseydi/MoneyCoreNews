package com.moneyCoreNews.security.services;

import com.moneyCoreNews.model.AUser;
import com.moneyCoreNews.model.AppRole;
import org.springframework.stereotype.Service;

public interface UserService {

    public AUser saveUser(AUser user);

    public AppRole getRoleIfElseSaveAndGet(String role);

    public void addRoleToUser(String username, String role);

    public void addRoleToAUser(String username, AppRole role);

    public void removeAUserRole(String username, AppRole role);

    public AUser findUserByEmail(String username);

    public AUser confirmPW(AUser auser, String password);

    public AUser confirmPW1(AUser auser, String password);

    public AUser findByConfirmationToken(String confirmationToken);


}
