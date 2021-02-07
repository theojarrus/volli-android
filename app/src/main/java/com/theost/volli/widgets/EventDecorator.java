package com.theost.volli.widgets;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;

public class EventDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> dates;
    private final int color;

    public EventDecorator(Collection<CalendarDay> dates, int color) {
        this.dates = new HashSet<>(dates);
        this.color = color;
    }

    public EventDecorator(int color) {
        this.dates = new HashSet<>();
        this.color = color;
    }

    public boolean addDay(CalendarDay day) {
        return dates.add(day);
    }

    public boolean removeDay(CalendarDay day) {
        return dates.add(day);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(5, color));
    }

}