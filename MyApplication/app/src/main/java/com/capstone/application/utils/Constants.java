package com.capstone.application.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Constants {

    private static final String DEFAULT_SERVER_URL = "http://192.168.0.105:8080/WebServer";

    public static String getServerUrl(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(Constants.SERVER_URL_KEY, DEFAULT_SERVER_URL);
    }

    /**
     * GCM management constant to be stored in SharedPreference
     */
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";

    /**
     * Type of messages that could be sent via GCM
     */
    public static final String GCM_FOLLOW_REQUEST_TYPE = "followRequest";
    public static final String GCM_NEW_CHECK_IN_TYPE = "newCheckIn";
    public static final String GCM_ADMIN_TYPE = "admin";

    /**
     * Intent action that is sent when new follow request has come for the logged teen
     */
    public static final String NEW_FOLLOW_REQUEST_ACTION = "capstone.intent.action.NEW_FOLLOW_REQUEST";

    /**
     * Intent action that is sent when alarm to retrieve new check in has been triggered
     */
    public static final String REQUEST_NEW_CHECK_IN_ACTION = "capstone.intent.action.REQUEST_NEW_CHECK_IN";

    /**
     * Intent action that is sent when a teen decides to postpone a check in
     */
    public static final String ADD_PENDING_CHECK_IN_ACTION = "capstone.intent.action.ADD_PENDING_CHECK_IN";

    /**
     * Intent action that is sent to notify the UI to update counter of pending check ins
     */
    public static final String NOTIFY_PENDING_CHECK_IN_ACTION = "capstone.intent.action.NOTIFY_PENDING_CHECK_IN";

    /**
     * Id of a question notification that will be used to be dismissed when necessary
     */
    public static final int QUESTION_NOTIFICATION_ID = 1;

    /**
     * Types of user to interact with this application
     */
    public enum UserType {
        ADMIN,
        TEEN,
        FOLLOWER
    }

    /**
     * Providers that can be used to sign in this application
     */
    public enum SignInProvider {
        APPLICATION,
        FACEBOOK
    }

    /**
     * Some useful information related to the logged in user to be stored in SharedPreference
     */
    public static final String SIGN_IN_PROVIDER = "signInProvider";
    public static final String LOGGED_EMAIL = "loggedEmail";
    public static final String USER_TYPE = "userType";
    public static final String PENDING_FOLLOW_REQUEST_COUNTER = "pendingFollowRequestCounter";

    /**
     * SharedPreference key of the options available in PreferencesFragment
     */
    public static final String REMINDER_FREQUENCY_KEY = "reminderFrequency";
    public static final String SHARED_DATA_KEY = "sharedData";
    public static final String SERVER_URL_KEY = "serverUrl";

    /**
     * Types of question that could be created by the admin
     */
    public enum QuestionFormat {
        FORMAT1("Multiple Choice"),
        FORMAT2("Number"),
        FORMAT3("Text");

        private final String value;

        QuestionFormat(String value) {
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

    /**
     * Types of question that could be created by the admin
     */
    public enum QuestionType {
        TYPE1("Type 1"),
        TYPE2("Type 2"),
        TYPE3("Type 3");

        private final String value;

        QuestionType(String value) {
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

        public static QuestionType fromString(String value) {
            if (value != null) {
                for (QuestionType item : values()) {
                    if (value.equals(item.value)) {
                        return item;
                    }
                }
            }

            throw new IllegalArgumentException("No constant with text " + value + " found");
        }
    }

    /**
     * Constants related to date/time format that will be displayed
     */
    public static final String DATE_FORMAT = "MM/dd/yyyy";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATE_TIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;

    /**
     * Temporary path where the check in photo will be saved while it has not been sent to server
     */
    public static final String SAVE_IMAGES_PATH = "/mnt/sdcard/Capstone/Images/";
}
