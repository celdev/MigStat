package com.celdev.migstat.model;

/*  Stores the application date as a long (time in ms since epoch
* */
public class ApplicationDate {

    private long applicationDate;

    public ApplicationDate(long applicationDate) {
        this.applicationDate = applicationDate;
    }

    public long getApplicationDate() {
        return applicationDate;
    }

    @Override
    public String toString() {
        return "ApplicationDate{" +
                "applicationDate=" + applicationDate +
                '}';
    }
}
