
package com.capstone.server.utils;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

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
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    // FIXME - uncomment
    public static boolean isValidDate(String date) {
        return true;
        /*boolean ret = false;
        if (date != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
            simpleDateFormat.setLenient(false);
            try {
                simpleDateFormat.parse(date);
                ret = true;
            } catch (ParseException e) {
            }
        }
        if (DEBUG) sLogger.debug("isValidDate(" + date + ") " + ret);
        return ret;*/
    }
}
