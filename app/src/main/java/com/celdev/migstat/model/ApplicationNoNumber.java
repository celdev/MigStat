package com.celdev.migstat.model;

import android.content.Context;

public class ApplicationNoNumber implements ApplicationMethod {

    private ApplicationStatus applicationStatus;
    private ApplicationDate applicationDate;
    private ApplicationNumber applicationNumber;

    public ApplicationNoNumber(Context context, long applicationDate) {
        this.applicationStatus = new ApplicationStatus(context, StatusType.PRIVACY_MODE.getNumber());
        this.applicationDate = new ApplicationDate(applicationDate);
        this.applicationNumber = null;
    }

}
