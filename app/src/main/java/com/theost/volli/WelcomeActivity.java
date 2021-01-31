package com.theost.volli;

import android.annotation.SuppressLint;

import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.TitlePage;
import com.stephentuso.welcome.WelcomeConfiguration;

public class WelcomeActivity extends com.stephentuso.welcome.WelcomeActivity {

    @SuppressLint("ResourceAsColor")
    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
                .defaultBackgroundColor(R.color.white)
                .page(new TitlePage(R.drawable.ic_launcher_foreground,
                        getString(R.string.welcome_app))
                        .titleColor(R.color.black)
                )
                .page(new BasicPage(R.drawable.ic_launcher_foreground,
                        getString(R.string.welcome_blank_title),
                        getString(R.string.welcome_blank_text))
                        .background(R.color.blue)
                )
                .page(new BasicPage(R.drawable.ic_launcher_foreground,
                        getString(R.string.welcome_blank_title),
                        getString(R.string.welcome_blank_text))
                        .background(R.color.yellow)
                )
                .page(new BasicPage(R.drawable.ic_launcher_foreground,
                        getString(R.string.welcome_blank_title),
                        getString(R.string.welcome_blank_text))
                        .background(R.color.green)
                )
                .page(new BasicPage(R.drawable.ic_launcher_foreground,
                        getString(R.string.welcome_blank_title),
                        getString(R.string.welcome_blank_text))
                        .background(R.color.red)
                )
                .swipeToDismiss(true)
                .showPrevButton(true)
                .canSkip(false)
                .build();
    }

}
