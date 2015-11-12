package com.capstone.application.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Follower implements Parcelable {
    private String email;

    private User user;

    private List<Teen> teenList;
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

    protected Follower(Parcel in) {
        email = in.readString();
        user = (User) in.readValue(User.class.getClassLoader());

        if (in.readByte() == 0x01) {
            teenList = new ArrayList<Teen>();
            in.readList(teenList, Teen.class.getClassLoader());
        } else {
            teenList = null;
        }

        if (in.readByte() == 0x01) {
            pendingTeenList = new ArrayList<Teen>();
            in.readList(pendingTeenList, Teen.class.getClassLoader());
        } else {
            pendingTeenList = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeValue(user);

        if (teenList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(teenList);
        }

        if (pendingTeenList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(pendingTeenList);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Follower> CREATOR = new Parcelable.Creator<Follower>() {
        @Override
        public Follower createFromParcel(Parcel in) {
            return new Follower(in);
        }

        @Override
        public Follower[] newArray(int size) {
            return new Follower[size];
        }
    };
}
