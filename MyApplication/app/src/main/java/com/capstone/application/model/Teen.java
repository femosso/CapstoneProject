package com.capstone.application.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Teen implements Parcelable {
    private String email;
    private String birthday;
    private String medicalNumber;

    private User user;

    private List<Follower> followerList;
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

    public String getSharedData() {
        return sharedData;
    }

    public void setSharedData(String sharedData) {
        this.sharedData = sharedData;
    }

    public Set<String> getSharedDataAsList() {
        if (sharedData != null) {
            return new HashSet<>(Arrays.asList(sharedData.split(",")));
        }

        return null;
    }

    public void setSharedDataAsList(Set<String> values) {
        if (values != null) {
            StringBuilder sb = new StringBuilder();
            for (String s : values) {
                sb.append(s);
                sb.append(",");
            }

            sharedData = sb.toString();
        }
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

    protected Teen(Parcel in) {
        email = in.readString();
        birthday = in.readString();
        medicalNumber = in.readString();
        sharedData = in.readString();
        user = (User) in.readValue(User.class.getClassLoader());

        if (in.readByte() == 0x01) {
            followerList = new ArrayList<>();
            in.readList(followerList, Follower.class.getClassLoader());
        } else {
            followerList = null;
        }

        if (in.readByte() == 0x01) {
            pendingFollowerList = new ArrayList<>();
            in.readList(pendingFollowerList, Follower.class.getClassLoader());
        } else {
            pendingFollowerList = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(birthday);
        dest.writeString(medicalNumber);
        dest.writeString(sharedData);
        dest.writeValue(user);

        if (followerList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(followerList);
        }

        if (pendingFollowerList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(pendingFollowerList);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Teen> CREATOR = new Parcelable.Creator<Teen>() {
        @Override
        public Teen createFromParcel(Parcel in) {
            return new Teen(in);
        }

        @Override
        public Teen[] newArray(int size) {
            return new Teen[size];
        }
    };
}