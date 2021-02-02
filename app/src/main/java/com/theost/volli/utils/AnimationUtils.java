package com.theost.volli.utils;

import android.view.View;

public class AnimationUtils {

    public static void animateScaleX(View view, float scale, int duration) {
        view.animate().setDuration(duration).scaleX(scale).withEndAction(() -> view.animate().setDuration(duration).scaleX(1.0f));
    }

    public static void animateScaleY(View view, float scale, int duration) {
        view.animate().setDuration(duration).scaleY(scale).withEndAction(() -> view.animate().setDuration(duration).scaleY(1.0f));
    }

    public static void animateFadeOutIn(View view, int duration, int delay) {
        view.animate().setStartDelay(delay).setDuration(duration).alpha(0.0f).withEndAction(() -> view.animate().setDuration(duration).alpha(1.0f));
    }

}
