package com.theost.volli.models;

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

}
