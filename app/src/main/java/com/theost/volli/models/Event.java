package com.theost.volli.models;

import java.util.Objects;

public class Event {

    private int day;
    private int hours;
    private int minutes;
    private int month;
    private int year;

    private String id;
    private String text;
    private String title;

    public void setDay(int day) {
        this.day = day;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDay() {
        return day;
    }

    public int getHours() {
        return hours;
    }

    public String getId() {
        return id;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public String getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }

    public boolean isInitialized() {
        return id != null && text != null && title != null && month >= 0 && month <= 12 && hours >= 0 && hours < 24 && minutes >= 0 && minutes < 60;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return day == event.day &&
                hours == event.hours &&
                minutes == event.minutes &&
                month == event.month &&
                year == event.year &&
                id.equals(event.id) &&
                text.equals(event.text) &&
                title.equals(event.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, hours, minutes, month, year, id, text, title);
    }
}
