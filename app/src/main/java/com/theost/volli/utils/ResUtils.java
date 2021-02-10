package com.theost.volli.utils;

import android.content.Context;

import com.theost.volli.R;

public class ResUtils {

    public static int getStringId(Context context, String string) {
        if (string.equals(context.getString(R.string.read))) {
            return R.string.read;
        } else if (string.equals(context.getString(R.string.create))) {
            return R.string.create;
        } else if (string.equals(context.getString(R.string.settings))) {
            return R.string.settings;
        } else if (string.equals(context.getString(R.string.back))) {
            return R.string.back;
        } else if (string.equals(context.getString(R.string.previous))) {
            return R.string.previous;
        } else if (string.equals(context.getString(R.string.choose))) {
            return R.string.choose;
        } else if (string.equals(context.getString(R.string.next))) {
            return R.string.next;
        } else if (string.equals(context.getString(R.string.reset))) {
            return R.string.reset;
        } else if (string.equals(context.getString(R.string.record))) {
            return R.string.record;
        } else if (string.equals(context.getString(R.string.clear))) {
            return R.string.clear;
        } else if (string.equals(context.getString(R.string.instructions))) {
            return R.string.instructions;
        } else if (string.equals(context.getString(R.string.yes))) {
            return R.string.yes;
        } else if (string.equals(context.getString(R.string.no))) {
            return R.string.no;
        } else {
            return -1;
        }
    }

}
