
package com.capstone.server.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;

@Entity
@Table(name = "Device")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Device implements Serializable {

    @Id
    @Column(name = "email", nullable = false)
    private String email;
    private String token;

    public Device() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
