package com.theost.volli.widgets;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.fragment.app.Fragment;

import com.stephentuso.welcome.WelcomeConfiguration;
import com.stephentuso.welcome.WelcomePage;
import com.stephentuso.welcome.WelcomeTitleFragment;
import com.theost.volli.WelcomeActivity;

/**
 * A page with a large title and an image
 *
 * Created by stephentuso on 10/11/16.
 */
public class TitlePage extends WelcomePage<TitlePage> {

    private final WelcomeActivity activity;

    private final int drawableResId;
    private final String title;
    private final boolean showParallax;
    private String titleTypefacePath = null;
    private int titleColor = Color.WHITE;

    /**
     * A page with a large title and an image
     *
     * @param drawableResId The resource id of the drawable to show
     * @param title Title
     */
    public TitlePage(WelcomeActivity activity, @DrawableRes int drawableResId, String title) {
        this.activity = activity;
        this.drawableResId = drawableResId;
        this.title = title;
        this.showParallax = false;
    }

    /**
     * Set the typeface of the title
     *
     * @param typefacePath The path to a typeface in the assets folder
     *
     */
    public void titleTypeface(String typefacePath) {
        this.titleTypefacePath = typefacePath;
    }

    /**
     * Set the color of the title
     *
     * @param color Color int
     *
     * @return This TitlePage object to allow method calls to be chained
     */
    public TitlePage titleColor(@ColorInt int color) {
        this.titleColor = color;
        return this;
    }

    @Override
    public void setup(WelcomeConfiguration config) {
        super.setup(config);

        if (this.titleTypefacePath == null) {
            titleTypeface(config.getDefaultTitleTypefacePath());
        }

    }

    @Override
    public Fragment fragment() {
        return WelcomeTitleFragment.newInstance(drawableResId, title, showParallax, titleTypefacePath, titleColor);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        super.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        super.onPageScrollStateChanged(state);
        if (state > 0) {
            activity.stopSlide();
        }
    }

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);
        activity.playSlide(position);
    }

}
