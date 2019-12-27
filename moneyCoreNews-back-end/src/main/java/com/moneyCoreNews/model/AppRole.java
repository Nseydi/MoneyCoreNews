package com.moneyCoreNews.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Entity
public class AppRole implements Serializable {

    private static final String STRING_PATTERN = "^[a-zA-Z0-9][a-zA-Z0-9 ]+$";
    //"^[a-zA-Z0-9!@#$&()-`.+,/\"]*$"

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@Column(unique=true)
    private String role;

    @Pattern(regexp = STRING_PATTERN)
    private String description;

    public AppRole() {
        super();
    }

    public AppRole(String role) {
        super();
        this.role = role;
    }

    public AppRole(String role, String description) {
        super();
        this.role = role;
        this.description = description;
    }

    public AppRole(Long id, String role, String description) {
        super();
        this.id = id;
        this.role = role;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
