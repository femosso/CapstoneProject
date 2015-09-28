
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
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "Teen")
public class Teen implements Serializable {

    @Id
    @Column(name = "teenEmail", nullable = false)
    private String email;
    private String birthday;
    private String medicalNumber;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "TeenFollower",
            joinColumns = @JoinColumn(name = "teenEmail"),
            inverseJoinColumns = @JoinColumn(name = "followerEmail"))
    private List<Follower> followerList;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "PendingTeenFollower",
            joinColumns = @JoinColumn(name = "teenEmail"),
            inverseJoinColumns = @JoinColumn(name = "followerEmail"))
    private List<Follower> pendingFollowerList;

    public Teen() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getMedicalNumber() {
        return medicalNumber;
    }

    public void setMedicalNumber(String medicalNumber) {
        this.medicalNumber = medicalNumber;
    }

    public List<Follower> getFollowerList() {
        return followerList;
    }

    public void setFollowerList(List<Follower> followerList) {
        this.followerList = followerList;
    }

    public List<Follower> getPendingFollowerList() {
        return pendingFollowerList;
    }

    public void setPendingFollowerList(List<Follower> pendingFollowerList) {
        this.pendingFollowerList = pendingFollowerList;
    }

}
