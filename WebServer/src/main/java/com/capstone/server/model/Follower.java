
package com.capstone.server.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;

@Entity
@Table(name = "Follower")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Follower implements Serializable {

    @Id
    @Column(name = "followerEmail", nullable = false)
    private String email;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "follower")
    @JsonBackReference
    private User user;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "TeenFollower",
            joinColumns = @JoinColumn(name = "followerEmail"),
            inverseJoinColumns = @JoinColumn(name = "teenEmail"))
    private List<Teen> teenList;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "PendingTeenFollower",
            joinColumns = @JoinColumn(name = "followerEmail"),
            inverseJoinColumns = @JoinColumn(name = "teenEmail"))
    private List<Teen> pendingTeenList;

    public Follower() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Teen> getTeenList() {
        return teenList;
    }

    public void setTeenList(List<Teen> teenList) {
        this.teenList = teenList;
    }

    public List<Teen> getPendingTeenList() {
        return pendingTeenList;
    }

    public void setPendingTeenList(List<Teen> pendingTeenList) {
        this.pendingTeenList = pendingTeenList;
    }

}
