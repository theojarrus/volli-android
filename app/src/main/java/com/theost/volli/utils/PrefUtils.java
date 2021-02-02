package com.theost.volli.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class PrefUtils {

    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void clearSharedPreferences(SharedPreferences preferences) {
        preferences.edit().clear().apply();
    }

    public static void putStringSharedPreferences(Context context, String key, String value) {
        SharedPreferences preferences = PrefUtils.getSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

}
