package com.example.nu_mad_sp2023_final_project_10;

import android.graphics.Color;

import java.util.Objects;

public class CalendarDay {
    private int year;
    private int month;
    private int day;
    private Color color;

    @Override
    public String toString() {
        return "CalendarDay{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", color=" + color +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarDay that = (CalendarDay) o;
        return year == that.year && month == that.month && day == that.day && color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month, day, color);
    }

    public CalendarDay(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.color = null;
    }

    public CalendarDay(int year, int month, int day, Color color) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.color = color;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public Color getColor() { return color; }

    public void setColor(Color color) { this.color = color; }
}
