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

    private Follower follower;

    public User(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

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

    public Follower getFollower() {
        return follower;
    }

    public void setFollower(Follower follower) {
        this.follower = follower;
    }

    protected User(Parcel in) {
        email = in.readString();
        password = in.readString();
        facebookId = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        provider = (SignInProvider) in.readValue(SignInProvider.class.getClassLoader());
        type = (UserType) in.readValue(UserType.class.getClassLoader());
        teen = (Teen) in.readValue(Teen.class.getClassLoader());
        follower = (Follower) in.readValue(Follower.class.getClassLoader());
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
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeValue(provider);
        dest.writeValue(type);
        dest.writeValue(teen);
        dest.writeValue(follower);
    }

    @SuppressWarnings("unused")
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