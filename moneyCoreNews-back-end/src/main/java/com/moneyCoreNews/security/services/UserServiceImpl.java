package com.moneyCoreNews.security.services;

import com.moneyCoreNews.model.AUser;
import com.moneyCoreNews.model.AppRole;
import com.moneyCoreNews.security.repository.AUserRepository;
import com.moneyCoreNews.security.repository.AppRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private AUserRepository userRepository;

    @Autowired
    private AppRoleRepository appRoleRepository;

    @Override
    public AUser saveUser(AUser user) {
        String hashPN = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(hashPN);
        return userRepository.save(user);
    }

    @Override
    public AUser confirmPW(AUser auser, String password) {
        String hashPN = bCryptPasswordEncoder.encode(password);
        auser.setPassword(hashPN);
        AUser user1 = userRepository.save(auser);
        return user1;
    }

    @Override
    public AUser confirmPW1(AUser auser, String password) {
        auser.setPassword(password);
        AUser user1 = userRepository.save(auser);
        return user1;
    }

    @Override
    public AppRole getRoleIfElseSaveAndGet(String role) {

        AppRole appRole = null;
        List<String> listAllRoleNames = new ArrayList<>();
        appRoleRepository.findAll().forEach(r -> {
            listAllRoleNames.add(r.getRole());
        });
        if (!(listAllRoleNames.contains(role))) {
            appRole = new AppRole(role);
            appRole = appRoleRepository.save(appRole);
        } else {
            appRole = appRoleRepository.findByRole("ADMIN");
        }
        return appRole;
        //return roleRepository.save(role);
    }

    @Override
    public void addRoleToUser(String username, String role) {
        AppRole role1 = appRoleRepository.findByRole(role);
        AUser user = userRepository.findByEmail(username);
        user.getRoles().add(role1);
    }

    @Override
    public void removeAUserRole(String username, AppRole role) {
        AUser user = userRepository.findByEmail(username);
        user.getRoles().remove(role);

    }

    @Override
    public void addRoleToAUser(String username, AppRole role) {
        AUser user = userRepository.findByEmail(username);
        user.getRoles().add(role);
    }

    @Override
    public AUser findUserByEmail(String username) {
        return userRepository.findByEmail(username);
    }

    @Override
    public AUser findByConfirmationToken(String confirmationToken) {
        return userRepository.findByConfirmationToken(confirmationToken);
    }
}
