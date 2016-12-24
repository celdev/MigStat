package com.celdev.migstat.model;

/*  Contains all the application information
*
*   There are two different kinds of applications
*       1. An application with an application number
*       2. An application without an application number
*
*   both have an application date
*
* */
public class Application{

    private ApplicationStatus applicationStatus;
    private ApplicationDate applicationDate;
    private ApplicationNumber applicationNumber;
    private WaitingTime waitingTime;

    private boolean hasApplicationNumber;

    //constructor for the Application with an application number
    public Application(StatusType status, long applicationDate, Integer applicationNumber, ApplicationNumberType applicationNumberType) {
        this.applicationStatus = new ApplicationStatus(status.getNumber());
        this.applicationDate = new ApplicationDate(applicationDate);
        this.applicationNumber = new ApplicationNumber(applicationNumber, applicationNumberType);
        hasApplicationNumber = true;
    }

    //constructor for the Application without an application number
    public Application(long applicationDate) {
        this.applicationDate = new ApplicationDate(applicationDate);
        hasApplicationNumber = false;
    }

    //returns true if the application number is set
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

    //returns the application status if it has an application status
    //otherwise throws an exception
    public ApplicationStatus getApplicationStatus() throws NoApplicationNumberException {
        if (applicationStatus == null) {
            throw new NoApplicationNumberException();
        }
        return applicationStatus;
    }

    //returns the application number if the application number
    //has been set, otherwise throws an exception
    public ApplicationNumber getApplicationNumber() throws NoApplicationNumberException {
        if (applicationNumber == null) {
            throw new NoApplicationNumberException();
        }
        return applicationNumber;
    }

    //sets the statustype and returns true if the status type = Finished
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

    /*  Currently not used
    *
    *   allows the application to show the update in waiting time
    *   currently a dialog that informs the user that the waiting time has been updated is shown
    *   using this it's possible to display the change in months, or not show the dialog
    *   if the months are the same
    * */
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
