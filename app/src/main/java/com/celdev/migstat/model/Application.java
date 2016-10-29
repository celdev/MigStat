package com.celdev.migstat.model;

import android.content.Context;

public class Application implements ApplicationMethod{

    private ApplicationStatus applicationStatus;
    private ApplicationDate applicationDate;
    private ApplicationNumber applicationNumber;

    public Application(Context context, StatusType status, long applicationDate, Integer applicationNumber, ApplicationNumberType applicationNumberType) {
        this.applicationStatus = new ApplicationStatus(context, status.getNumber());
        this.applicationDate = new ApplicationDate(applicationDate);
        this.applicationNumber = new ApplicationNumber(applicationNumber, applicationNumberType);
    }
}
