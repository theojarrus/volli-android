package com.theost.volli.widgets;

import android.graphics.drawable.Drawable;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

public class TodayDecorator implements DayViewDecorator {

    private CalendarDay today;
    private final Drawable drawable;
    private final int color;

    public TodayDecorator(CalendarDay day, Drawable drawable, int color) {
        this.today = day;
        this.drawable = drawable;
        this.color = color;
    }

    public void changeDay(CalendarDay day) {
        this.today = day;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return day.equals(today);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(color));
        view.setBackgroundDrawable(drawable);
    }

}