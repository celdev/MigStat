package com.celdev.migstat.model;

public class WaitingTime {

    private enum DayOrMonth{
        DAY,
        MONTH
    }

    private int lowMonth, highMonth;
    private int days;
    private String updatedAtDate;
    private DayOrMonth dayOrMonth;

    public WaitingTime(int lowMonth, int highMonth, String updatedAtDate) {
        this.dayOrMonth = DayOrMonth.MONTH;
        this.lowMonth = lowMonth;
        this.highMonth = highMonth;
        this.updatedAtDate = updatedAtDate;
    }

    public WaitingTime(int days, String updatedAtDate) {
        this.days = days;
        this.dayOrMonth = DayOrMonth.DAY;
        this.updatedAtDate = updatedAtDate;
    }

    public double getAverage() {
        if (dayOrMonth.equals(DayOrMonth.DAY)) {
            return days;
        }
        return (highMonth + lowMonth) / 2.0;
    }

    @Override
    public String toString() {
        return "WaitingTime{" +
                "lowMonth=" + lowMonth +
                ", highMonth=" + highMonth +
                ", days=" + days +
                ", updatedAtDate='" + updatedAtDate + '\'' +
                ", dayOrMonth=" + dayOrMonth +
                ", average= " + getAverage() +
                '}';
    }
}
