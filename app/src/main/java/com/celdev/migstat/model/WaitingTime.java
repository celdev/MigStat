package com.celdev.migstat.model;

public class WaitingTime {

    private enum DayOrMonth{
        DAY,
        MONTH
    }

    private int lowMonth, highMonth;
    private int days;
    private long updatedAt;
    private DayOrMonth dayOrMonth;

    public WaitingTime(int lowMonth, int highMonth, long updatedAt) {
        this.dayOrMonth = DayOrMonth.MONTH;
        this.lowMonth = lowMonth;
        this.highMonth = highMonth;
        this.updatedAt = updatedAt;
    }

    public WaitingTime(int days, long updatedAt) {
        this.days = days;
        this.dayOrMonth = DayOrMonth.DAY;
        this.updatedAt = updatedAt;
    }



}
