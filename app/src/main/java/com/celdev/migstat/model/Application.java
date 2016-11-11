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

    /*  Sets the new waiting time and returns an object containing
    *   the new and the old waiting time if:
    *       * there is an old waiting time
    *       * the "updated at" date of the old waiting time is older than the new waiting time
    * */
    public OldAndNewWaitingTimeWrapper setWaitingTimeReturnBothIfNewer(WaitingTime waitingTime) {
        if (this.waitingTime != null) {
            int newer = WaitingTime.WaitingTimeUpdatedDateComparator.compare(this.waitingTime, waitingTime);
            if(newer == -1);{
                OldAndNewWaitingTimeWrapper ret = new OldAndNewWaitingTimeWrapper(this.waitingTime, waitingTime);
                this.waitingTime = waitingTime;
                return ret;
            }
        }
        this.waitingTime = waitingTime;
        return null;
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

    public class OldAndNewWaitingTimeWrapper {
        private WaitingTime oldWaitingTime;
        private WaitingTime newWaitingTime;

        public OldAndNewWaitingTimeWrapper(WaitingTime oldWaitingTime, WaitingTime newWaitingTime) {
            this.oldWaitingTime = oldWaitingTime;
            this.newWaitingTime = newWaitingTime;
        }

        public WaitingTime getOldWaitingTime() {
            return oldWaitingTime;
        }

        public WaitingTime getNewWaitingTime() {
            return newWaitingTime;
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
