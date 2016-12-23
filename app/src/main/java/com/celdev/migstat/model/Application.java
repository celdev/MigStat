package com.celdev.migstat.model;


public class Application{

    private ApplicationStatus applicationStatus;
    private ApplicationDate applicationDate;
    private ApplicationNumber applicationNumber;
    private WaitingTime waitingTime;

    private boolean hasApplicationNumber;

    public Application(StatusType status, long applicationDate, Integer applicationNumber, ApplicationNumberType applicationNumberType) {
        this.applicationStatus = new ApplicationStatus(status.getNumber());
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
    public synchronized OldAndNewWaitingTimeWrapper setWaitingTimeReturnBothIfNewer(WaitingTime waitingTime) {
        if (this.waitingTime != null) {
            int newer = WaitingTime.WaitingTimeUpdatedDateComparator.compare(this.waitingTime, waitingTime);
            int customMonths = this.waitingTime.getCustomMonths();
            boolean customMonthsMode = this.waitingTime.isUseCustomMonths();
            if (customMonthsMode) {
                waitingTime.setUseCustomMonthsMode(customMonths);
            }
            if(newer == -1){
                OldAndNewWaitingTimeWrapper ret = new OldAndNewWaitingTimeWrapper(this.waitingTime, waitingTime);
                this.waitingTime = waitingTime;
                return ret;
            }
        }
        this.waitingTime = waitingTime;
        return null;
    }

    public void setWaitingTime(WaitingTime waitingTime) {
        this.waitingTime = waitingTime;
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

    public boolean newStatusTypeReturnTrueIfGetDecision(StatusType statusType) {
        if (hasApplicationNumber) {
            try {
                getApplicationStatus().setStatus(statusType.getNumber());
                return statusType.equals(StatusType.FINISHED);
            } catch (NoApplicationNumberException e) {
                e.printStackTrace();
                // will never happen
            }
        }
        return false;
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
