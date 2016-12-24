package com.celdev.migstat.model;

import com.celdev.migstat.controller.utils.DateUtils;

import java.util.Comparator;

/*  This class contains the waiting time.
*
*   There are two different types of waiting time
*       custom              the user sets the waiting time themself
*       migrationsverket    the application uses the waiting time
*                           from the migrationsverket website
*
*   the migrationsverket waiting time are usually 2 months (i.e. 13-15 months)
*   the custom is only 1 month
*
*   the migrationsverket waiting time is extractable from the url stored in the query variable
* */
public class WaitingTime {


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

    //returns true if the waiting time query is set
    public boolean hasQuery() {
        return query != null && !query.isEmpty();
    }

    //returns true if the custom months value isn't set to zero
    public boolean hasCustomWaitingTime() {
        return customMonths != 0;
    }

    //returns true if lowMonth == highMonth, or if useCustomMonths is set
    //is set
    public boolean lowMonthAndHighMonthIsEqual() {
        return useCustomMonths || highMonth == lowMonth;
    }

    //sets the waiting time mode to custom months and the custom months value and
    //returns this
    public WaitingTime setUseCustomMonthsMode(int customMonths) {
        this.customMonths = customMonths;
        this.useCustomMonths = true;
        return this;
    }

    //sets the waiting time mode depending on the
    //the waitingtimemode passed as a parameter
    //if WaitingTimeMode.MIGRATIONSVERKET is passed the useCustomMonths will be
    //set to false
    public void setWaitingTimeMode(WaitingTimeMode waitingTimeMode) {
        useCustomMonths = !waitingTimeMode.equals(WaitingTimeMode.MIGRATIONSVERKET);
    }

    public boolean isUseCustomMonths() {
        return useCustomMonths;
    }

    public int getCustomMonths() {
        return customMonths;
    }

    public int getLowMonth() {
        return lowMonth;
    }

    public int getHighMonth() {
        return highMonth;
    }

    /*  returns the average months
    *   custom months if the custom months mode is set
    *   otherwise the average value of the high and low value
    * */
    public double getAverage() {
        if (useCustomMonths) {
            return customMonths;
        }
        return (highMonth + lowMonth) / 2.0;
    }

    public String getUpdatedAtDate() {
        return updatedAtDate;
    }

    /*  Comparator for the update-at value in the waiting time
    *   compares the dates of the waiting time to determine which is the newest
    * */
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
