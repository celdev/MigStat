package com.celdev.migstat.model;

import android.content.Context;

class Application implements ApplicationMethod{



    private ApplicationStatus applicationStatus;
    private ApplicationDate applicationDate;
    private ApplicationNumber applicationNumber;

    Application(Context context, StatusType status, long applicationDate, Integer applicationNumber, ApplicationNumberType applicationNumberType) {
        this.applicationStatus = new ApplicationStatus(context, status.getNumber());
        this.applicationDate = new ApplicationDate(applicationDate);
        this.applicationNumber = new ApplicationNumber(applicationNumber, applicationNumberType);
    }


    @Override
    public long getApplicationDate() {
        return applicationDate.getApplicationDate();
    }

    @Override
    public ApplicationStatus getApplicationStatus() throws NoApplicationNumberException {
        return applicationStatus;
    }

    @Override
    public ApplicationNumber getApplicationNumber() throws NoApplicationNumberException {
        return applicationNumber;
    }
}
