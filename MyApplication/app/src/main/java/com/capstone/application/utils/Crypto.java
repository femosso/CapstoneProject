package com.capstone.application.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Helper class to get the hash of password to be sent over to the app's server when performing login
 */
public class Crypto {

    public static String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte item : messageDigest) {
                String h = Integer.toHexString(0xFF & item);
                while (h.length() < 2) {
                    h = "0" + h;
                }
                hexString.append(h);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }
}
