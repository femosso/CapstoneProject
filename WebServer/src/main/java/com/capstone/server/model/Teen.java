
package com.capstone.server.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
@Table(name = "Teen")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Teen implements Serializable {

    @Id
    @Column(name = "teenEmail", nullable = false)
    private String email;
    private String birthday;
    private String medicalNumber;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "teen")
    @JsonBackReference
    private User user;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "TeenFollower", joinColumns = @JoinColumn(name = "teenEmail") , inverseJoinColumns = @JoinColumn(name = "followerEmail") )
    private List<Follower> followerList;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "PendingTeenFollower", joinColumns = @JoinColumn(name = "teenEmail") , inverseJoinColumns = @JoinColumn(name = "followerEmail") )
    private List<Follower> pendingFollowerList;

    private String sharedData;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getSharedData() {
        return sharedData;
    }

    public void setSharedData(String sharedData) {
        this.sharedData = sharedData;
    }

    public Set<String> getSharedDataAsList() {
        if(sharedData != null) {
            return new HashSet<>(Arrays.asList(sharedData.split(",")));
        }

        return null;
    }

    public void setSharedDataAsList(Set<String> values) {
        if (values != null) {
            StringBuilder sb = new StringBuilder();
            for(String s : values) {
                sb.append(s);
                sb.append(",");
            }

            sharedData = sb.toString();
        }
    }
}
