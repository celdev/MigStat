package com.celdev.migstat.model;

import android.content.Context;

class ApplicationNoNumber implements ApplicationMethod {

    private ApplicationStatus applicationStatus;
    private ApplicationDate applicationDate;
    private ApplicationNumber applicationNumber;

    ApplicationNoNumber(Context context, long applicationDate) {
        this.applicationStatus = new ApplicationStatus(context, StatusType.PRIVACY_MODE.getNumber());
        this.applicationDate = new ApplicationDate(applicationDate);
        this.applicationNumber = null;
    }

    @Override
    public long getApplicationDate() {
        return applicationDate.getApplicationDate();
    }

    @Override
    public ApplicationStatus getApplicationStatus() throws NoApplicationNumberException {
        throw new NoApplicationNumberException();
    }

    @Override
    public ApplicationNumber getApplicationNumber() throws NoApplicationNumberException {
        throw new NoApplicationNumberException();
    }

    @Override
    public void newStatusType(StatusType statusType) {
        //do nothing this method shouldn't be reachable.
    }
}
