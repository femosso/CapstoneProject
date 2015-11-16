
package com.capstone.server.utils;

public class Constants {

    /** Project package name */
    public static final String PACKAGE_NAME = "com.capstone.server";

    /** Session constants */
    public static final String SESSION_USER = "loggedUser";
    public static final String IS_REDIRECT = "isRedirect";

    /**
     * Type of messages that could be sent via GCM
     */
    public static final String GCM_FOLLOW_REQUEST_TYPE = "followRequest";
    public static final String GCM_NEW_CHECK_IN_TYPE = "newCheckIn";
    public static final String GCM_ADMIN_TYPE = "admin";

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

    /** Types of question that could be created by the admin */
    public enum QuestionFormat {
        FORMAT1("Multiple Choice"),
        FORMAT2("Number"),
        FORMAT3("Text");

        private final String value;

        private QuestionFormat(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static String[] names() {
            QuestionFormat[] formats = values();
            String[] names = new String[formats.length];

            for (int i = 0; i < formats.length; i++) {
                names[i] = formats[i].getValue();
            }

            return names;
        }
    }

    /** Types of question that could be created by the admin */
    public enum QuestionType {
        TYPE1("Blood Sugar Level"),
        TYPE2("State of Teen"),
        TYPE3("General Question");

        private final String value;

        private QuestionType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static String[] names() {
            QuestionType[] types = values();
            String[] names = new String[types.length];

            for (int i = 0; i < types.length; i++) {
                names[i] = types[i].getValue();
            }

            return names;
        }
    }
}
