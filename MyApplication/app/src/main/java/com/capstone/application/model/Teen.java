package com.capstone.application.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Teen implements Parcelable {

    private long id;
    private String birthday;
    private String medicalNumber;

    private User user;

    public Teen() {
    }

    public long getId() {
        return id;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    protected Teen(Parcel in) {
        id = in.readLong();
        birthday = in.readString();
        medicalNumber = in.readString();
        user = in.readParcelable(User.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(birthday);
        dest.writeString(medicalNumber);
        dest.writeParcelable(user, flags);
    }

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