package com.moneyCoreNews.api;

import com.moneyCoreNews.MoneyCoreNewsApplication;
import com.moneyCoreNews.business.MoneyCoreNewsMetier;
import com.moneyCoreNews.mailing.EmailService;
import com.moneyCoreNews.services.*;
import com.moneyCoreNews.model.AUser;
import com.moneyCoreNews.model.AppRole;
import com.moneyCoreNews.model.ModelApiResponse;
import com.moneyCoreNews.model.Token;
import com.moneyCoreNews.security.repository.AUserRepository;
import com.moneyCoreNews.security.repository.AppRoleRepository;
import com.moneyCoreNews.security.services.UserService;
import com.moneyCoreNews.util.CustomErrorType;
import com.moneyCoreNews.util.InvalidFileException;
import io.swagger.annotations.ApiParam;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-02-18T14:31:08.631Z")
@Controller
public class AUserApiController implements AUserApi {

    @Autowired
    private UserService userService;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AppRoleRepository appRoleRepository;

    @Autowired
    private AUserRepository aUserRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${business.email}")
    private String businessEmail;

    @Autowired
    private MoneyCoreNewsMetier moneyCoreNewsMetier;


    @Value("${dir.Photos.users}")
    private String userPhotoDir;

    @Value("${upload.photo.extensions}")
    private String validPhotoExtensions;

    @Value("${angular.url}")
    private String appUrl;

    @Value("${angular.url.confirm.user}")
    private String urlConfirmation;

    @Value("${angular.url.reset}")
    private String urlReset;

    @Value("${angular.url.enableAccount}")
    private String urlEnable;

    @Value("${appADMIN.email}")
    private String appAdminEmail;

    private final Logger LOGGER = LoggerFactory.getLogger(MoneyCoreNewsApplication.class);

    @Value("${business.email}")
    private String teamMail;

    // ***********************************************************createAUser
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ResponseEntity<AUser> createAUser(
            @ApiParam(value = "Created user object", required = true) @Valid @RequestBody AUser appUser,
            HttpServletRequest request, BindingResult result, Authentication authentication) {

        LOGGER.info("Creating a new user with email {}.", appUser.getEmail());

        if (authentication != null) {
            if (result.hasErrors()) {
                List<String> errors = result.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
                LOGGER.error("Unable to create a new user. You have some invalid inputs: {}.", errors);
                return new ResponseEntity(
                        new CustomErrorType("Unable to create a new user. You have some invalid inputs: " + errors),
                        HttpStatus.NOT_ACCEPTABLE);
            }

            if (userAccountService.userExists(appUser)) {
                LOGGER.error("Unable to create. A user with email {} is already exist.", appUser.getEmail());
                return new ResponseEntity(new CustomErrorType("Unable to create. A user with email " + appUser.getEmail()
                        + " is already exist."), HttpStatus.CONFLICT);
            }

            // admin user 
            AUser adminUser = userAccountService.getUserByEmail(authentication.getName());

            AUser returnedUser = userAccountService.createUser(appUser, adminUser);

            return new ResponseEntity<AUser>(returnedUser, HttpStatus.OK);
        }

        return new ResponseEntity(new CustomErrorType("Y're not authorized to make this operation."), HttpStatus.UNAUTHORIZED);
    }

    // ***********************************************************confirmRegistration
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ResponseEntity<Void> confirmRegistration(
            @NotNull @ApiParam(value = "The token that is sent to your mail", required = true)
            @PathVariable(value = "token", required = true) String tokenValue,
            @ApiParam(value = "give your password ", required = true) @RequestBody String password) {

        // Find the user associated with the reset token
        Token token = userAccountService.getTokenByValue(tokenValue);
        AUser user = userAccountService.getUserByConfirmationToken(tokenValue);
        if (token == null || !token.checkValidToken()) {
            userAccountService.deleteRegistration(user, token);
            return new ResponseEntity(new CustomErrorType("Your access token has expired, no access is granted for you"),
                    HttpStatus.UNAUTHORIZED);
        }

        userAccountService.confirmRegistration(user, token, password);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    // ***********************************************************processForgotPasswordForm
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<Void> processForgotPasswordForm(@RequestParam("email") String userEmail,
                                                          HttpServletRequest request) {

        // Lookup user in database by e-mail
        AUser user = userAccountService.getUserByEmail(userEmail);

        if (user == null) {
            LOGGER.error("Unable to process. The user with email {} doesn't exist.", userEmail);
            return new ResponseEntity(new CustomErrorType("Unable to process. The user with email " + userEmail
                    + " doesn't exist."), HttpStatus.NOT_FOUND);
        }

        userAccountService.forgotPassword(userEmail);

        return new ResponseEntity<Void>(HttpStatus.OK);

    }

    // *********************************************************** 
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<Void> spoofingAttemptHandle(@RequestParam("email") String email, HttpServletRequest request) {

        AUser user = userAccountService.getUserByEmail(email);
        if (user == null) {
            LOGGER.error("Unable to process. There no user with this email {}", email);
            return new ResponseEntity(new CustomErrorType("Unable to process. There no user with this email " + email),
                    HttpStatus.NOT_FOUND);
        }

        userAccountService.spoofingAttempt(email);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    // *********************************************************** enableAccount
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<Void> enableAccount(
            @NotNull @ApiParam(value = "The token that is sent to your mail", required = true)
            @PathVariable(value = "token", required = true) String token) {

        // Find the user associated with the reset token
        AUser resetUser = userAccountService.getUserByConfirmationToken(token);
        if (resetUser == null) {
            LOGGER.error("Unable to enable account. User doesn't exist.");
            return new ResponseEntity(new CustomErrorType("Unable to enable account. User doesn't exist."),
                    HttpStatus.NOT_FOUND);
        }

        userAccountService.enableAccount(token);
        return new ResponseEntity<Void>(HttpStatus.OK);

    }

    // ***********************************************************setNewPassword
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<Void> setNewPassword(
            @NotNull @ApiParam(value = "The token that is sent to your mail", required = true)
            @PathVariable(value = "token", required = true) String token,
            @NotNull @ApiParam(value = "The password for login in clear text", required = true) @RequestBody String password) {

        // Find the user associated with the reset token
        AUser resetUser = userAccountService.getUserByConfirmationToken(token);
        if (resetUser == null) {
            LOGGER.error("Unable to set new password. The user doesn't exist");
            return new ResponseEntity(new CustomErrorType("Unable to set new password. The user doesn't exist"),
                    HttpStatus.NOT_FOUND);
        }

        userAccountService.setNewPassword(token, password);
        return new ResponseEntity<Void>(HttpStatus.OK);

    }

    // ***********************************************************addRoleToUser
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ResponseEntity<AppRole> addRoleToUser(
            @ApiParam(value = "the id of the user", required = true) @PathVariable("userId") Long userId,
            @ApiParam(value = "the role to add to user", required = true) @Valid @RequestBody AppRole appRole,
            Authentication authentication) {
        if (authentication != null) {

            AUser user = userAccountService.getUserById(userId);
            if (user == null) {
                return new ResponseEntity(new CustomErrorType("A user with id: " + userId + " not found."),
                        HttpStatus.NOT_FOUND);
            }

            if (userAccountService.roleIsExist(userId, appRole)) {
                return new ResponseEntity(new CustomErrorType("Unable to create. This user has already a role."),
                        HttpStatus.CONFLICT);
            }

            AppRole ar = userAccountService.addRoleToUser(userId, appRole);

            return new ResponseEntity<AppRole>(ar, HttpStatus.OK);
        }

        return new ResponseEntity(new CustomErrorType("Y're not authorized to make this operation."), HttpStatus.UNAUTHORIZED);

    }
    // ***********************************************************addRoleToUser

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<AppRole> setUserRole(
            @ApiParam(value = "The id of the user", required = true) @PathVariable("userId") Long userId,
            @ApiParam(value = "Created user object", required = true) @Valid @RequestBody AppRole appRole,
            Authentication authentication) {

        if (!(authentication == null)) {
            AUser user = aUserRepository.findOne(userId);
            if (user == null) {
                return new ResponseEntity(new CustomErrorType("Utilisateur avec identifiant " + userId + " pas trouvé"),
                        HttpStatus.NOT_FOUND);
            }
            AppRole currentRole = user.getRoles().get(0);
            List<AppRole> listRoles = user.getRoles();
            List<String> listRoleNames = new ArrayList<>();

            listRoles.forEach(r -> {
                listRoleNames.add(r.getRole());
            });
            List<String> listAllRoleNames = new ArrayList<>();
            appRoleRepository.findAll().forEach(r -> {
                listAllRoleNames.add(r.getRole());
            });
            ;
            String roleName = appRole.getRole();
            if (listRoleNames.contains(roleName)) {
                return new ResponseEntity(new CustomErrorType(
                        "Erreur de création. cet utilisateur a déjà ce rôle."),
                        HttpStatus.CONFLICT);
            }
            if (!(listAllRoleNames.contains(roleName))) {
                appRole.setRole(roleName);
                currentRole.setDescription(appRole.getDescription());
                currentRole.setRole(appRole.getRole());
                appRoleRepository.save(currentRole);
            }
            AppRole ar = appRoleRepository.findByRole(roleName);
            String email = user.getEmail();
            currentRole.setDescription(ar.getDescription());
            currentRole.setRole(ar.getRole());
            userService.addRoleToAUser(email, currentRole);
            user.addRole(currentRole);
            return new ResponseEntity<AppRole>(currentRole, HttpStatus.OK);
        } else {
            return new ResponseEntity(new CustomErrorType("Vous n'êtes pas autorisé à faire cette opération"),
                    HttpStatus.UNAUTHORIZED);
        }
    }
    // ***********************************************************deleteAUser

    @SuppressWarnings({"unchecked", "unused", "rawtypes"})
    public ResponseEntity<Void> deleteAUser(
            @ApiParam(value = "The name that needs to be deleted", required = true) @PathVariable("id") Long id,
            Authentication authentication) {
        AUser user = aUserRepository.findOne(id);

        AUser user1 = aUserRepository.findByEmail(authentication.getName());
        if (!(authentication == null)) {

            if (user == null) {
                return new ResponseEntity(
                        new CustomErrorType("Impossible à supprimer. Candidat avec identifiant" + id + " pas trouvé."),
                        HttpStatus.NOT_FOUND);
            }

            if (!(user.getPhoto() == null)) {
                String pathDir1 = userPhotoDir + "user_" + user.getId() + "_photos";
                File userfiles = new File(pathDir1);
                Path path = Paths.get(pathDir1);

                try {
                    FileUtils.cleanDirectory(userfiles);
                    Files.delete(path);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
            if (!(user.getRoles().isEmpty())) {
                user.setRoles(null);
                user.getRoles().forEach(r -> {
                    user.removeRole(r);
                });
            }

            aUserRepository.delete(user);
            return new ResponseEntity<Void>(HttpStatus.OK);

        } else {
            return new ResponseEntity(new CustomErrorType("Vous n'êtes pas autorisé à faire cette opération"),
                    HttpStatus.UNAUTHORIZED);
        }

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ResponseEntity<Void> deleteUnactivatdUser(
            @ApiParam(value = "The name that needs to be deleted", required = true) @PathVariable("id") Long id, Authentication authentication) {
        AUser user = aUserRepository.findOne(id);
        /*
		AUser user1=aUserRepository.findByEmail(authentication.getName());
         */
        if (!(authentication == null)) {

            if (user == null) {
                return new ResponseEntity(
                        new CustomErrorType("Unable to delete. Candidate with id " + id + " not found."),
                        HttpStatus.NOT_FOUND);
            }


            user.getRoles().forEach(r -> {
                appRoleRepository.delete(r);
            });
            user.setRoles(null);
            user.getRoles().forEach(r -> {
                user.removeRole(r);
            });

            aUserRepository.delete(user);
            return new ResponseEntity<Void>(HttpStatus.OK);

        } else {
            return new ResponseEntity(new CustomErrorType("y're not authorized to make this operation"),
                    HttpStatus.UNAUTHORIZED);
        }

    }
    // ***********************************************************getAUserByName

    public ResponseEntity<AUser> getAUserByName(
            @ApiParam(value = "The name that needs to be fetched. Use user1 for testing. ", required = true) @PathVariable("email") String email) {
        AUser user = userService.findUserByEmail(email);
        return new ResponseEntity<AUser>(user, HttpStatus.OK);
    }

    // ***********************************************************getAUserByToken
    public ResponseEntity<AUser> getAUserByToken(
            @ApiParam(value = "The name that needs to be fetched. Use user1 for testing. ", required = true) @PathVariable("token") String token) {
        AUser user = aUserRepository.findByConfirmationToken(token);
        return new ResponseEntity<AUser>(user, HttpStatus.OK);
    }

    // ***********************************************************getAUserRoles
    public ResponseEntity<List<AppRole>> getAUserRoles(
            @ApiParam(value = "The name that needs to be fetched. Use user1 for testing. ", required = true) @PathVariable("email") String email) {
        List<AppRole> lroles = aUserRepository.findAllAUserRoles(email);
        return new ResponseEntity<List<AppRole>>(lroles, HttpStatus.OK);
    }

    // ***********************************************************updateAUser
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<Void> updateAUser(
            @ApiParam(value = "name that need to be updated", required = true) @PathVariable("Userid") Long id,
            BindingResult result, @ApiParam(value = "Updated user object", required = true) @Valid @RequestBody AUser aUser, Authentication authentication) {

        if (!(authentication == null)) {
            if (result.hasErrors()) {
                List<String> errors = result.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.toList());
                return new ResponseEntity(new CustomErrorType("you have some invalid inputs " + ": " + errors), HttpStatus.NOT_ACCEPTABLE);
            }
            AUser adminUser = aUserRepository.findByEmail(authentication.getName());
            AUser currentUser = aUserRepository.findById(id);
            if (currentUser == null) {
                return new ResponseEntity(
                        new CustomErrorType("Unable to update. user with id " + id + " not found."),
                        HttpStatus.NOT_FOUND);
            }
            AUser existingUser = aUserRepository.findByEmail(aUser.getEmail());
            if (!(existingUser == null)) {
                if (appRoleRepository.exists(existingUser.getId())) {
                    return new ResponseEntity(
                            new CustomErrorType(
                                    "Unable to create. A user with name " + aUser.getEmail() + " already exist."),
                            HttpStatus.CONFLICT);
                }
            }

            currentUser.setFirstName(aUser.getFirstName());
            currentUser.setLastName(aUser.getLastName());
            currentUser.setModificationDate(new LocalDate());
            currentUser.setPhone(aUser.getPhone());
            currentUser.setEmail(aUser.getEmail());
            String password = aUser.getPassword();
            if (!(password == null) || (password == "")) {
                userService.confirmPW1(currentUser, currentUser.getPassword());
            } else {
                userService.confirmPW(currentUser, password);
            }

            return new ResponseEntity<Void>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    // ***********************************************************updateAUser
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<Void> updateMyAccount(BindingResult result,
                                                @ApiParam(value = "Updated user object", required = true) @Valid @RequestBody AUser aUser, Authentication authentication) {
        //AUser currentUser = aUserRepository.findById(1L);
        if (!(authentication == null)) {
            if (result.hasErrors()) {
                List<String> errors = result.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.toList());
                return new ResponseEntity(new CustomErrorType("you have some invalid inputs " + ": " + errors), HttpStatus.NOT_ACCEPTABLE);
            }
            AUser currentUser = aUserRepository.findByEmail(authentication.getName());
            if (currentUser == null) {
                return new ResponseEntity(
                        new CustomErrorType("Unable to update. user with id " + 1 + " not found."),
                        HttpStatus.NOT_FOUND);
            }
            AUser existingUser = aUserRepository.findByEmail(aUser.getEmail());
            if (!(existingUser == null)) {
                if (appRoleRepository.exists(existingUser.getId())) {
                    return new ResponseEntity(
                            new CustomErrorType(
                                    "Unable to create. A user with name " + aUser.getEmail() + " already exist."),
                            HttpStatus.CONFLICT);
                }
            }
            currentUser.setFirstName(aUser.getFirstName());
            currentUser.setLastName(aUser.getLastName());
            currentUser.setModificationDate(new LocalDate());
            currentUser.setPhone(aUser.getPhone());
            currentUser.setEmail(aUser.getEmail());
            String password = aUser.getPassword();
            if (!(password == null) || (password == "")) {
                userService.confirmPW1(currentUser, currentUser.getPassword());
            } else {
                userService.confirmPW(currentUser, password);
            }
            currentUser.setEnabled(false);
            // Email message
            String subject = "[KAKOORH][Kakoo Software]: Mise à jour du compte";
            String message = "Nous voulons vous informer que votre compte a été mis à jour avec votre responsable, votre compte est maintenant désactivé, pour le réactiver, cliquez sur le lien ci-dessous.";
            String expirationMsg = "N.B: votre accès expirera dans 12 heures à partir de maintenant.";
            String name = currentUser.getLastName() + " " + currentUser.getFirstName();
            String location = "KAKOORH,Paris, FRANCE";
            String signature = "Kakoo TEAM";
            String actionName = "Activate account";
            String url = appUrl + urlEnable + currentUser.getConfirmationToken();
            emailService.prepareAndSend(subject, currentUser.getEmail(), expirationMsg, message, actionName, url, name, location, signature);
            return new ResponseEntity<Void>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
    // ***********************************************************setNewPassword

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<Void> connectedSetNewPassword(@NotNull @ApiParam(value = "The old password for login", required = true) @RequestParam(value = "oldPassword", required = true) String oldPassword,
                                                        @NotNull @ApiParam(value = "The password for login in clear text", required = true) @RequestBody String password,
                                                        Authentication authentication) {
        if (!(authentication == null)) {
            AUser user = aUserRepository.findByEmail(authentication.getName());
            //AUser user = aUserRepository.findOne(1L);
            if (bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
                userService.confirmPW(user, password);
                return new ResponseEntity<Void>(HttpStatus.OK);
            }

        }

        return new ResponseEntity(new CustomErrorType("there is no connected user found"), HttpStatus.NOT_FOUND);

    }
    // ***********************************************************loginAUser

    public ResponseEntity<String> loginAUser(
            @NotNull @ApiParam(value = "The user name for login", required = true) @RequestParam(value = "email", required = true) String email,
            @NotNull @ApiParam(value = "The password for login in clear text", required = true) @RequestParam(value = "password", required = true) String password) {
        String sucess = "congratulation you're in";
        return new ResponseEntity<String>(sucess, HttpStatus.OK);
    }

    // ***********************************************************logoutAUser
    public ResponseEntity<Void> logoutAUser() {

        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    // ***********************************************************findAllUsers
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ResponseEntity<List<AUser>> findAllUsers() {
        List<AUser> listUsers = (List<AUser>) aUserRepository.findAll();
        if (listUsers.isEmpty()) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);

        }
        return new ResponseEntity<List<AUser>>(listUsers, HttpStatus.OK);
    }

    // ***********************************************************findAllUsers
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ResponseEntity<List<AUser>> findAllManagers() {
        AppRole r = appRoleRepository.findByRole("ADMIN");
        List<AUser> listUsers = (List<AUser>) aUserRepository.findAllAdmin(r);
        if (listUsers.isEmpty()) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);

        }
        return new ResponseEntity<List<AUser>>(listUsers, HttpStatus.OK);
    }
    // ***********************************************************findUnActivatedAccounts

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ResponseEntity<List<AUser>> findUnActivatedAccounts() {
        List<AUser> listUsers = (List<AUser>) aUserRepository.findUnActivatedAccounts();
        if (listUsers.isEmpty()) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);

        }
        return new ResponseEntity<List<AUser>>(listUsers, HttpStatus.OK);
    }
    // ***********************************************************findCompanyUsers

    // ***********************************************************getConnectedUser

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<AUser> getConnectedUser(Authentication authentication) {
        if (!(authentication == null)) {
            AUser user = aUserRepository.findByEmail(authentication.getName());
            return new ResponseEntity<AUser>(user, HttpStatus.OK);
        }

        return new ResponseEntity(new CustomErrorType("there is no connected user found"), HttpStatus.NOT_FOUND);
    }
    // **********************************************************uploadFile/Photo

    @SuppressWarnings({"unchecked", "unused", "rawtypes"})
    @Override
    public ResponseEntity<ModelApiResponse> uploadImage(
            @ApiParam(value = "Additional data to pass to server") @RequestPart(value = "additionalMetadata", required = false) String additionalMetadata,
            @ApiParam(value = "file detail") @RequestPart("file") MultipartFile file,
            final HttpServletRequest request, Authentication authentication)
            throws IllegalStateException, IOException, ServletException, InvalidFileException {
        //AUser user = aUserRepository.findOne(3L);
        AUser user = aUserRepository.findByEmail(authentication.getName());
        if (!(authentication == null)) {
            String pathDir = userPhotoDir + "user_" + user.getId() + "_photos";
            if (user.getPhoto() == null || user.getPhoto() == "") {
                if (!(file.isEmpty())) {

                    File file1 = new File(pathDir);
                    if (!file1.exists()) {
                        if (file1.mkdir()) {
                            file1.setExecutable(true);
                            file1.setReadable(true);
                            file1.setWritable(true);
                        } else {
                            System.out.println("Failed to create directory!");
                        }
                    }
                    Part filePart = request.getPart("file");
                    String type = filePart.getContentType();
                    if (!moneyCoreNewsMetier.isPhotoValidExtension(type.substring(type.lastIndexOf("/") + 1))) {
                        throw new InvalidFileException("Invalid Photo Extension");
                    }
                    ;
                    type = "." + type.substring(type.lastIndexOf("/") + 1);
                    String namepattern = "PHOTO_" + user.getLastName() + "_" + user.getFirstName() + type;
                    user.setPhoto(namepattern);
                    String path = pathDir + "/" + namepattern;
                    file.transferTo(new File(path));
                    aUserRepository.save(user);
                }

                return new ResponseEntity<ModelApiResponse>(HttpStatus.OK);
            } else {
                String path1 = pathDir + "/" + user.getPhoto();
                Path p = Paths.get(path1);
                try {
                    Files.delete(p);
                } catch (FileNotFoundException e) {
                    user.setPhoto(null);
                    aUserRepository.save(user);
                    return new ResponseEntity<ModelApiResponse>(HttpStatus.BAD_REQUEST);
                }
                Part filePart = request.getPart("file");
                String type = filePart.getContentType();
                if (!moneyCoreNewsMetier.isPhotoValidExtension(type.substring(type.lastIndexOf("/") + 1))) {
                    throw new InvalidFileException("Invalid Photo Extension");
                }
                ;
                type = "." + type.substring(type.lastIndexOf("/") + 1);
                String namepattern = "PHOTO_" + user.getLastName() + "_" + user.getFirstName() + type;
                user.setPhoto(namepattern);
                String path = pathDir + "/" + namepattern;
                file.transferTo(new File(path));
                aUserRepository.save(user);
                return new ResponseEntity<ModelApiResponse>(HttpStatus.OK);
            }

        } else {
            return new ResponseEntity(new CustomErrorType("y're not authorized to make this operation"),
                    HttpStatus.UNAUTHORIZED);
        }
    }

    //********************************************************** downloadFile	
    @Override
    public void downloadPhoto(HttpServletResponse response, @ApiParam(value = "ID of photo to return", required = true)
    @PathVariable("idUser") Long idUser) throws IOException {
        // AUser user=aUserRepository.findOne(5L);
        AUser user = aUserRepository.findOne(idUser);
        String pathDir = userPhotoDir + "user_" + user.getId() + "_photos";

        File file = null;
        String location = pathDir + "/" + user.getPhoto();
        file = new File(location);

        if (!file.exists()) {
            String errorMessage = "Sorry. The file you are looking for does not exist";

            OutputStream outputStream = response.getOutputStream();
            outputStream.write(errorMessage.getBytes(Charset.forName("UTF-8")));
            outputStream.close();
            return;
        }

        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        if (mimeType == null) {
            System.out.println("mimetype is not detectable, will take default");
            mimeType = "application/octet-stream";
        }

        System.out.println("mimetype : " + mimeType);

        response.setContentType(mimeType);

        response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));

        response.setContentLength((int) file.length());

        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

        //Copy bytes from source to destination(outputstream in this example), closes both streams.
        FileCopyUtils.copy(inputStream, response.getOutputStream());

    }


    /**
     * if user choose an offre we should redirect him to pay process (signup -> activate account -> payment)
     *
     * @param: mail
     * return type: Link activation value form json
     */
    @Override
    public ResponseEntity<String> getActivationLink(String email) throws Exception {

        AUser user = this.aUserRepository.findByEmail(email);
        String activationLink = "{ \"link\" : \"" + user.getConfirmationToken() + "\"}";

        return new ResponseEntity<String>(activationLink, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> isEmailExist(String email) throws Exception {
        return new ResponseEntity<Boolean>(this.aUserRepository.findByEmail(email) != null, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> isUsernameExist(String username) throws Exception {
        return new ResponseEntity<Boolean>(this.aUserRepository.findByUsername(username) != null, HttpStatus.OK);
    }
}
