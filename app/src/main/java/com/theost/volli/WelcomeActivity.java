package com.theost.volli;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.stephentuso.welcome.WelcomeConfiguration;
import com.theost.volli.utils.DisplayUtils;
import com.theost.volli.widgets.TextSpeaker;
import com.theost.volli.widgets.TitlePage;

public class WelcomeActivity extends com.stephentuso.welcome.WelcomeActivity {

    private TextSpeaker textSpeaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textSpeaker = new TextSpeaker(this);
        onInitThread();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (textSpeaker.isInitialized()) {
            playSlide(0);
        }
    }

    @Override
    public void onBackPressed() {
        // disable back
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
                .defaultBackgroundColor(R.color.white)
                .page(new TitlePage(this, R.drawable.ic_launcher_foreground,
                        getString(R.string.welcome_title_slide_0))
                        .titleColor(R.color.black)
                )
                .page(new TitlePage(this, R.drawable.ic_calendar_24,
                        getString(R.string.welcome_title_slide_1))
                        .background(R.color.blue)
                )
                .page(new TitlePage(this, R.drawable.ic_cloud_24,
                        getString(R.string.welcome_title_slide_2))
                        .background(R.color.yellow)
                )
                .page(new TitlePage(this, R.drawable.ic_control_24,
                        getString(R.string.welcome_title_slide_3))
                        .background(R.color.green)
                )
                .swipeToDismiss(true)
                .bottomLayout(WelcomeConfiguration.BottomLayout.INDICATOR_ONLY)
                .canSkip(false)
                .build();
    }

    public void playSlide(int pageIndex) {
        int textId = getResources().getIdentifier(getString(R.string.welcome_slide_text) + pageIndex, "string", this.getPackageName());
        if (textSpeaker != null && textId != 0) {
            textSpeaker.speak(getResources().getString(textId));
        }
    }

    public void stopSlide() {
        textSpeaker.stop();
    }

    private void onInitThread() {
        WelcomeActivity activity = this;
        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                if (textSpeaker != null && textSpeaker.isInitialized()) {
                    playSlide(0);
                    cancel();
                }
            }
            public void onFinish() {
                DisplayUtils.showToast(activity, R.string.voice_speaker_error);
            }
        }.start();
    }

}
