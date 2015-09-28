package com.capstone.application.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Teen implements Parcelable {

    private String email;
    private String birthday;
    private String medicalNumber;

    private List<Follower> followerList;

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

    protected Teen(Parcel in) {
        email = in.readString();
        birthday = in.readString();
        medicalNumber = in.readString();
        if (in.readByte() == 0x01) {
            followerList = new ArrayList<Follower>();
            in.readList(followerList, Follower.class.getClassLoader());
        } else {
            followerList = null;
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
        if (followerList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(followerList);
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