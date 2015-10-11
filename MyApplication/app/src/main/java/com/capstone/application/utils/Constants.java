package com.capstone.application.utils;

public class Constants {

    /** Project package name */
    public static final String SERVER_URL = "http://192.168.0.109:8080/WebServer/";

    /** GCM management constant to be stored in SharedPreference */
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";

    /** GCM management action to notify UI thread when new GCM message has come */
    public static final String REGISTRATION_COMPLETE_ACTION = "registrationComplete";
    public static final String NEW_FOLLOW_REQUEST_ACTION = "newFollowRequest";

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

    /** Email and Type of login made by user to be stored in SharedPreference */
    public static final String SIGN_IN_PROVIDER = "signInProvider";
    public static final String LOGGED_EMAIL = "loggedEmail";

    public static final String NOTIFICATION_COUNTER = "notificationCounter";
}
