
package com.capstone.server.utils;

public class Constants {

    /** Project package name */
    public static final String PACKAGE_NAME = "com.capstone.server";

    /** Session constants */
    public static final String SESSION_USER = "loggedUser";

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