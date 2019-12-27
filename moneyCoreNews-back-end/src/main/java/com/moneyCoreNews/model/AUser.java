package com.moneyCoreNews.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.LocalDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@Entity
@JsonIgnoreProperties(value = {"password"}, allowGetters = false, allowSetters = true)
@Table(name = "user")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class AUser {

    //private static final String STRING_PATTERN = "^[a-zA-Z0-9][a-zA-Z0-9 ]+$";
    private static final String STRING_PATTERN = "^[\\p{L}a-zA-Z0-9_@.+,#$&][\\p{L}a-zA-Z0-9-_ @.+,#$& ]*$";
    private static final String MOBILE_PATTERN = "[0-9]{9,14}";
    private static final String PW_PATTERN = "^[a-zA-Z0-9!@#$&()\\-`.+,/\"]*$";

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    @NotBlank(message = "user.email.blank")
    @NotNull(message = "user.email.null")
    @NotEmpty(message = "user.email.empty")
    @Email(message = "user.email.format")
    private String email;

    @Column(name = "username", unique = true)
    @NotBlank(message = "user.username.blank")
    @NotNull(message = "user.username.null")
    @NotEmpty(message = "user.username.empty")
    @Pattern(regexp = STRING_PATTERN, message = "user.username.format")
    private String username;

    @Column(name = "password")
    //@Transient
    //@JsonIgnore
    @Pattern(regexp = PW_PATTERN, message = "user.email.format")
    private String password;

    @Column(name = "first_name")
    @NotNull(message = "user.firstName.null")
    @NotEmpty(message = "user.firstName.empty")
    @Pattern(regexp = STRING_PATTERN, message = "user.firstName.format")
    private String firstName;

    @Column(name = "last_name")
    @NotNull(message = "user.lastName.null")
    @NotEmpty(message = "user.lastName.empty")
    @Pattern(regexp = STRING_PATTERN, message = "user.lastName.format")
    private String lastName;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "confirmation_token")
    private String confirmationToken;

    @JsonProperty("phone")
    @NotNull(message = "user.phone.null")
    @NotEmpty(message = "user.phone.empty")
    @Pattern(regexp = MOBILE_PATTERN, message = "user.phone.format")
    private String phone;

    private LocalDate modificationDate;

    @Pattern(regexp = STRING_PATTERN, message = "user.photo.format")
    private String photo;

    @JsonProperty("roles")
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "USER_ROLES")
    @Fetch(value = FetchMode.SUBSELECT)
    private List<AppRole> roles = new ArrayList<>();


    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }


    public List<AppRole> getRoles() {
        return roles;
    }

    public AUser(String email, String username, String password) {
        super();
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRoles(List<AppRole> roles) {
        this.roles = roles;
    }

    public void addRole(AppRole r) {
        this.roles.add(r);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDate getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(LocalDate modificationDate) {
        this.modificationDate = modificationDate;
    }

    public void removeRole(AppRole r) {
        this.roles.remove(r);
    }

    public AUser() {
        super();
    }

    public AUser(String email, String username, String password, String firstName, String lastName, String phone) {
        super();
        this.email = email;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    public AUser(String email, String password, String firstName, String lastName, boolean enabled,
                 String confirmationToken, String phone) {
        super();
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enabled = enabled;
        this.confirmationToken = confirmationToken;
        this.phone = phone;
    }

    public AUser(String email, String password, String firstName, String lastName, boolean enabled,
                 String confirmationToken, String phone, List<AppRole> roles) {
        super();
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enabled = enabled;
        this.confirmationToken = confirmationToken;
        this.phone = phone;
        this.roles = roles;
    }

    public String getConfirmationToken() {
        return confirmationToken;
    }

    public void setConfirmationToken(String confirmationToken) {
        this.confirmationToken = confirmationToken;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(MOBILE_PATTERN);
        Matcher matcher = pattern.matcher(phone);

        if (matcher.matches()) {
            this.phone = phone;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        this.enabled = value;
    }


}
