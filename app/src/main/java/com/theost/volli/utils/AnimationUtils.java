package com.theost.volli.utils;

import android.view.View;
import android.widget.TextView;

public class AnimationUtils {

    public static void animateScaleX(View view, float scale, int duration) {
        view.animate().setDuration(duration).scaleX(scale).withEndAction(() -> view.animate().setDuration(duration).scaleX(1.0f));
    }

    public static void animateScaleY(View view, float scale, int duration) {
        view.animate().setDuration(duration).scaleY(scale).withEndAction(() -> view.animate().setDuration(duration).scaleY(1.0f));
    }

    public static void animateFadeOutIn(TextView[] views, String[] textReplacements, int duration, int delay) {
        for (int i = 0; i < views.length && i < textReplacements.length; i++) {
            int j = i;
            views[i].animate().setStartDelay(delay).setDuration(duration).alpha(0.0f).withEndAction(() -> {
                views[j].setText(textReplacements[j]);
                views[j].animate().setDuration(duration).alpha(1.0f);
            });
        }
    }

}
