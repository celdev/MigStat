package com.celdev.migstat.model;

import com.celdev.migstat.controller.utils.DateUtils;

import java.util.Comparator;

public class WaitingTime {

    public enum DayOrMonth{
        DAY(true),
        MONTH(false);

        DayOrMonth(boolean day) {
        }
    }

    public static DayOrMonth isDayMode(boolean mode) {
        return mode ? DayOrMonth.DAY : DayOrMonth.MONTH;
    }

    private int lowMonth, highMonth;
    private int days;
    private String updatedAtDate;
    private DayOrMonth dayOrMonth;
    private int customMonths;
    private boolean useCustomMonths;

    private String query;

    public WaitingTime(int lowMonth, int highMonth, String updatedAtDate, String query) {
        this.dayOrMonth = DayOrMonth.MONTH;
        this.lowMonth = lowMonth;
        this.highMonth = highMonth;
        this.updatedAtDate = updatedAtDate;
        this.query = query;
    }

    //returns true if lowMonth == highMonth, waiting time is in days or using custom months mode
    //is set
    public boolean lowMonthAndHighMonthIsEqual() {
        return useCustomMonths || highMonth == lowMonth || dayOrMonth.equals(DayOrMonth.DAY);
    }

    public void setUseCustomMonthsMode(int customMonths) {
        this.customMonths = customMonths;
        this.useCustomMonths = true;
    }

    public boolean isUseCustomMonths() {
        return useCustomMonths;
    }

    public int getCustomMonths() {
        return customMonths;
    }

    public void disableCustomMonthsMode() {
        this.useCustomMonths = false;
    }

    public WaitingTime(int days, String updatedAtDate, String query) {
        this.days = days;
        this.dayOrMonth = DayOrMonth.DAY;
        this.updatedAtDate = updatedAtDate;
        this.query = query;
    }

    public int getLowMonth() {
        return lowMonth;
    }

    public int getHighMonth() {
        return highMonth;
    }

    public double getAverage() {
        if (useCustomMonths) {
            return customMonths;
        }
        if (dayOrMonth.equals(DayOrMonth.DAY)) {
            return days;
        }
        return (highMonth + lowMonth) / 2.0;
    }

    public String getUpdatedAtDate() {
        return updatedAtDate;
    }

    public static Comparator<WaitingTime> WaitingTimeUpdatedDateComparator = new Comparator<WaitingTime>() {
        @Override
        public int compare(WaitingTime waitingTime, WaitingTime t1) {
            try {
                return DateUtils.compareDateStrings(t1.getUpdatedAtDate(), waitingTime.getUpdatedAtDate());
            } catch (ParserException e) {
                return 0;
            }
        }
    };

    @Override
    public String toString() {
        return "WaitingTime{" +
                "lowMonth=" + lowMonth +
                ", highMonth=" + highMonth +
                ", days=" + days +
                ", updatedAtDate='" + updatedAtDate + '\'' +
                ", dayOrMonth=" + dayOrMonth +
                ", average= " + getAverage() +
                ", query= " + query +
                '}';
    }

    public String getQuery() {
        return query;
    }
}
