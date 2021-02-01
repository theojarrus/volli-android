package com.theost.volli.utils;

import android.util.Patterns;

public class AuthUtils {

    public static boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password, int minLength) {
        return password.length() >= minLength;
    }

    public static boolean requestAuth() {
        // todo request auth to firebase
        return true;
    }

}
