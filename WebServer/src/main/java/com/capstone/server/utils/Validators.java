
package com.capstone.server.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.log4j.Logger;

public class Validators {

    private final static Logger sLogger = Logger.getLogger(Validators.class);

    private final static boolean DEBUG = sLogger.isDebugEnabled();

    public static boolean isValidString(String str) {
        boolean ret = str != null && !str.trim().isEmpty();
        if (DEBUG) sLogger.debug("isValidString(" + str + ") " + ret);
        return ret;
    }

    public static boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    public static boolean isValidDate(String date) {
        boolean ret = false;
        if (date != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
            simpleDateFormat.setLenient(false);
            try {
                simpleDateFormat.parse(date);
                ret = true;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (DEBUG) sLogger.debug("isValidDate(" + date + ") " + ret);
        return ret;
    }
}
