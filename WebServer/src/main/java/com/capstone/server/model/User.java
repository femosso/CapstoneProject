
package com.capstone.server.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.capstone.server.utils.Constants.SignInProvider;
import com.capstone.server.utils.Constants.UserType;

@Entity
@Table(name = "User")
public class User {

    @Id
    private String email;
    private String password;
    private String facebookId;
    private SignInProvider provider;
    private UserType type;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Teen teen;

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public SignInProvider getProvider() {
        return provider;
    }

    public void setProvider(SignInProvider provider) {
        this.provider = provider;
    }

    public UserType getType() {
        return type;
    }

    public void setType(UserType type) {
        this.type = type;
    }

    public Teen getTeen() {
        return teen;
    }

    public void setTeen(Teen teen) {
        this.teen = teen;
    }

}
