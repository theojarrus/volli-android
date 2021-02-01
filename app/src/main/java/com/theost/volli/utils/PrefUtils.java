package com.theost.volli.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class PrefUtils {

    public static final String PREFERENCES_KEY_USERNAME = "key_user_name";
    public static final String PREFERENCES_KEY_EMAIL = "key_user_email";

    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void clearSharedPreferences(SharedPreferences preferences) {
        preferences.edit().clear().apply();
    }

}
