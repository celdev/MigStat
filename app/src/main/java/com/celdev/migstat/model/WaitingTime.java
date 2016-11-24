package com.celdev.migstat.model;

import com.celdev.migstat.controller.utils.DateUtils;

import java.util.Comparator;

public class WaitingTime implements WaitingTimeInterface {


    public enum WaitingTimeMode{
        CUSTOM,
        MIGRATIONSVERKET
    }

    private int lowMonth = -1, highMonth = -1;
    private String updatedAtDate;
    private int customMonths = 0;
    private boolean useCustomMonths;

    private String query;

    public WaitingTime(int lowMonth, int highMonth, String updatedAtDate, String query) {
        this.lowMonth = lowMonth;
        this.highMonth = highMonth;
        this.updatedAtDate = updatedAtDate;
        this.query = query;
    }

    public WaitingTime(int lowMonth, int highMonth, String updatedAtDate, String query, boolean useCustomMonths, int customMonths) {
        this(lowMonth, highMonth, updatedAtDate, query);
        this.useCustomMonths = useCustomMonths;
        this.customMonths = customMonths;
    }

    public WaitingTime(int customMonths) {
        this.customMonths = customMonths;
        this.updatedAtDate = "";
        this.useCustomMonths = true;
    }

    public boolean hasQuery() {
        return query != null && !query.isEmpty();
    }

    public boolean hasCustomWaitingTime() {
        return customMonths != 0;
    }

    //returns true if lowMonth == highMonth, or if useCustomMonths is set
    //is set
    public boolean lowMonthAndHighMonthIsEqual() {
        return useCustomMonths || highMonth == lowMonth;
    }


    public WaitingTime setUseCustomMonthsMode(int customMonths) {
        this.customMonths = customMonths;
        this.useCustomMonths = true;
        return this;
    }

    public void setWaitingTimeMode(WaitingTimeMode waitingTimeMode) {
        useCustomMonths = !waitingTimeMode.equals(WaitingTimeMode.MIGRATIONSVERKET);
    }

    public boolean isUseCustomMonths() {
        return useCustomMonths;
    }

    public int getCustomMonths() {
        return customMonths;
    }

    public void disableCustomMonthsMode() throws IllegalWaitingTimeStateException {
        if (lowMonth == -1 && highMonth == -1) {
            throw new IllegalWaitingTimeStateException();
        }
        this.useCustomMonths = false;
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
                ", updatedAtDate='" + updatedAtDate + '\'' +
                ", average= " + getAverage() +
                ", query= " + query +
                '}';
    }

    public String getQuery() {
        return query;
    }
}
