package com.capstone.application.utils;

public class Constants {

    /** Project package name */
    public static final String SERVER_URL = "http://192.168.0.105:8080/WebServer/";

    /** Types of user to interact with this application */
    public enum UserType {
        ADMIN,
        TEEN,
        FOLLOWER
    }

    /** Providers that can be used to sign in this application */
    public enum SignInProvider {
        APPLICATION,
        FACEBOOK
    }
}
