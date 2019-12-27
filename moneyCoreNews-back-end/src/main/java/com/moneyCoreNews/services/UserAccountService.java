package com.moneyCoreNews.services;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.moneyCoreNews.mailing.EmailService;
import com.moneyCoreNews.model.AUser;
import com.moneyCoreNews.model.AppRole;
import com.moneyCoreNews.model.SimpleToken;
import com.moneyCoreNews.model.Token;
import com.moneyCoreNews.security.repository.AUserRepository;
import com.moneyCoreNews.security.repository.AppRoleRepository;
import com.moneyCoreNews.security.repository.TokenRepository;
import com.moneyCoreNews.security.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserAccountService {

    @Autowired
    private AUserRepository aUserRepository;


    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService accountService;

    @Autowired
    private AppRoleRepository appRoleRepository;

    @Value("${angular.url}")
    private String appUrl;

    @Value("${angular.url.confirm.user}")
    private String urlConfirmation;

    @Value("${business.email}")
    private String teamMail;

    @Value("${angular.url.reset}")
    private String urlReset;

    @Value("${angular.url.enableAccount}")
    private String urlEnable;

    public AUser getUserByEmail(String email) {
        return aUserRepository.findByEmail(email);
    }

    public AUser getUserById(Long userId) {
        return aUserRepository.findOne(userId);
    }

    public AUser getUserByUsername(String username) {
        return aUserRepository.findByUsername(username);
    }

    public AUser getUserByConfirmationToken(String tokenValue) {
        return accountService.findByConfirmationToken(tokenValue);
    }

    public boolean userExists(AUser aUser) {
        return getUserByEmail(aUser.getEmail()) != null && getUserByUsername(aUser.getUsername()) != null;
    }

    public Token getTokenByValue(String tokenValue) {
        return tokenRepository.findByTokenValue(tokenValue);
    }

    public void deleteRegistration(AUser aUser, Token token) {
        aUserRepository.delete(aUser);
        tokenRepository.delete(token);
    }

    // *********************************************************** Create a new
    public AUser createUser(AUser newUser, AUser adminUser) {

        newUser.setEnabled(false);

        // Generate random 36-character string token for confirmation link
        newUser.setConfirmationToken(UUID.randomUUID().toString());
        SimpleToken token = tokenRepository
                .save(new SimpleToken(UUID.randomUUID().toString(), java.time.LocalDate.now()));
        newUser.setConfirmationToken(token.getTokenValue());

        String subject = "[MoneyCoreNews]:Confirmation d'enregistrement";
        String expirationMsg = "N.B: Votre accès au test expirera d'ici 12 heures.";
        String message = "Un compte a été créé sur le site web MoneyCore-News par Mr " + newUser.getFirstName() + " "
                + newUser.getLastName() + ", pour activer votre compte, veuillez cliquer sur le lien ci-dessous: ";
        String name = newUser.getLastName() + " " + newUser.getFirstName();
        String location = "MoneyCoreNews, Paris, France";
        String actionName = "Confirmer l'inscription";
        String signature = "MoneyCoreNews TEAMs";
        String url = appUrl + urlConfirmation + newUser.getConfirmationToken();
        emailService.prepareAndSend(subject, newUser.getEmail(), expirationMsg, message, actionName, url, name,
                location, signature);

        return accountService.saveUser(newUser);
    }

    // *********************************************************** Confirm the
    // registration
    public void confirmRegistration(AUser user, Token token, String password) {

        user.setEnabled(true);
        user.setConfirmationToken(null);
        tokenRepository.delete(token);
        accountService.confirmPW(user, password);
    }

    // *********************************************************** Forget password
    // process
    public void forgotPassword(String userEmail) {

        AUser user = getUserByEmail(userEmail);

        // Generate random 36-character string token for reset password
        user.setConfirmationToken(UUID.randomUUID().toString());

        // Save token to database
        accountService.saveUser(user);

        // String appUrl = request.getScheme() + "://" + request.getServerName()+
        // ":8080";
        // Email message
        String subject = "[MoneyCoreNews]: Demande de réinitialisation du mot de passe";
        String message = "Si vous avez perdu votre mot de passe ou si vous souhaitez le réinitialiser, veuillez cliquer sur "
                + "le lien ci-dessous: ";
        String expirationMsg = "N.B: votre accès au resiliation de mot de passe expirera dans 12 heures à partir de maintenant.";
        String name = user.getFirstName() + " " + user.getLastName();
        String location = "MoneyCoreNews, Paris, FRANCE";
        String actionName = "réinitialiser le mot de passe";
        String signature = "MoneyCoreNews TEAM";
        String url = appUrl + urlReset + user.getConfirmationToken();
        emailService.prepareAndSend(subject, user.getEmail(), expirationMsg, message, actionName, url, name, location,
                signature);
    }

    // *********************************************************** Spoofing attempt
    // process
    public void spoofingAttempt(String email) {

        AUser user = getUserByEmail(email);

        // Generate random 36-character string token for reset password
        user.setConfirmationToken(UUID.randomUUID().toString());
        user.setEnabled(false);

        // Save token to database
        accountService.confirmPW1(user, user.getPassword());

        // Email message
        String subject = "[MoneyCoreNews]: Alerte d'usurpation d'identité";
        String message = "Nous voulons vous informer qu'il y a quelqu'un qui essaie d'usurper votre compte, votre compte est "
                + "maintenant désactivé, pour le réactiver cliquez sur le lien ci-dessous";
        String expirationMsg = "N.B: Votre accès au test expirera dans 12 heures à partir de maintenant.";
        String name = user.getLastName() + " " + user.getFirstName();
        String location = "MoneyCoreNews,Paris, France";
        String actionName = "Activer le compte";
        String signature = "MoneyCoreNews TEAM";
        String url = appUrl + urlEnable + user.getConfirmationToken();
        emailService.prepareAndSend(subject, user.getEmail(), expirationMsg, message, actionName, url, name, location,
                signature);
    }

    // *********************************************************** Enable user's
    // account process
    public void enableAccount(String token) {

        // Find the user associated with the reset token
        AUser resetUser = getUserByConfirmationToken(token);

        resetUser.setEnabled(true);

        accountService.confirmPW1(resetUser, resetUser.getPassword());
    }

    // *********************************************************** Set a new
    // password
    public void setNewPassword(String token, String password) {

        AUser resetUser = getUserByConfirmationToken(token);

        resetUser.setConfirmationToken(null);
        resetUser.setEnabled(true);
        accountService.confirmPW(resetUser, password);

    }

    // ***********************************************************
    public boolean roleIsExist(Long userId, AppRole appRole) {

        AUser user = getUserById(userId);
        List<AppRole> listRoles = user.getRoles();
        List<String> listRoleNames = new ArrayList<>();

        listRoles.forEach(r -> {
            listRoleNames.add(r.getRole());
        });

        String roleName = appRole.getRole();
        return listRoleNames.contains(roleName);
    }

    // *********************************************************** Add role to the
    // user
    public AppRole addRoleToUser(Long userId, AppRole appRole) {

        AUser user = getUserById(userId);

        AppRole role = accountService.getRoleIfElseSaveAndGet(appRole.getRole());

        String email = user.getEmail();
        accountService.addRoleToAUser(email, role);
        user.addRole(role);

        return role;
    }
}
