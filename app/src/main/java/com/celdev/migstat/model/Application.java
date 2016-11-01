package com.celdev.migstat.model;

import android.content.Context;


public class Application{

    private ApplicationStatus applicationStatus;
    private ApplicationDate applicationDate;
    private ApplicationNumber applicationNumber;
    private WaitingTime waitingTime;

    private boolean hasApplicationNumber;

    public Application(Context context, StatusType status, long applicationDate, Integer applicationNumber, ApplicationNumberType applicationNumberType) {
        this.applicationStatus = new ApplicationStatus(context, status.getNumber());
        this.applicationDate = new ApplicationDate(applicationDate);
        this.applicationNumber = new ApplicationNumber(applicationNumber, applicationNumberType);
        hasApplicationNumber = true;
    }

    public Application(long applicationDate) {
        this.applicationDate = new ApplicationDate(applicationDate);
        hasApplicationNumber = false;
    }

    public boolean isHasApplicationNumber() {
        return hasApplicationNumber;
    }

    public WaitingTime getWaitingTime() {
        return waitingTime;
    }

    public boolean setWaitingTimeReturnIsNewer(WaitingTime waitingTime) {
        boolean isNewerWaitingTime = false;
        if (this.waitingTime != null) {
            int newer = WaitingTime.WaitingTimeUpdatedDateComparator.compare(this.waitingTime, waitingTime);
            isNewerWaitingTime = newer == -1;
        }
        this.waitingTime = waitingTime;
        return isNewerWaitingTime;
    }

    public long getApplicationDate() {
        return applicationDate.getApplicationDate();
    }

    public ApplicationStatus getApplicationStatus() throws NoApplicationNumberException {
        if (applicationStatus == null) {
            throw new NoApplicationNumberException();
        }
        return applicationStatus;
    }

    public ApplicationNumber getApplicationNumber() throws NoApplicationNumberException {
        if (applicationNumber == null) {
            throw new NoApplicationNumberException();
        }
        return applicationNumber;
    }

    public void newStatusType(StatusType statusType) {
        try {
            getApplicationStatus().setStatus(statusType.getNumber());
        } catch (NoApplicationNumberException e) {
            //ignore
        }
    }

    @Override
    public String toString() {
        return "Application{" +
                "applicationStatus=" + applicationStatus +
                ", applicationDate=" + applicationDate +
                ", applicationNumber=" + applicationNumber +
                ", waitingTime=" + waitingTime +
                ", hasApplicationNumber=" + hasApplicationNumber +
                '}';
    }
}
