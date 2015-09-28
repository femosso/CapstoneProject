
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
import javax.persistence.Table;

@Entity
@Table(name = "Follower")
public class Follower implements Serializable {

    @Id
    @Column(name = "followerEmail", nullable = false)
    private String email;

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
