package com.capstone.application.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.capstone.application.utils.Constants.SignInProvider;
import com.capstone.application.utils.Constants.UserType;

public class User implements Parcelable {

    private String email;
    private String password;
    private String facebookId;
    private String firstName;
    private String lastName;
    private SignInProvider provider;
    private UserType type;

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    protected User(Parcel in) {
        email = in.readString();
        password = in.readString();
        facebookId = in.readString();
        provider = (SignInProvider) in.readSerializable();
        type = (UserType) in.readSerializable();
        teen = (Teen) in.readValue(Teen.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(facebookId);
        dest.writeSerializable(provider);
        dest.writeSerializable(type);
        dest.writeValue(teen);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}