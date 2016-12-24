package com.celdev.migstat.model;

/*  Currently not implemented
*
*   some of the "application types" waiting time have multiple waiting times
*   The types:
*   CITIZENSHIP;
*   TRAVEL_DOCUMENT;
*   UT_CARD_FOR_EU_FAMILY;
*   VISIT_SWEDEN;
*
*   show multiple waiting times and will require the user to choose which waiting time they want
*
*   This class enables the storage of multiple waiting times and their "condition"
*
* */
public class MultipleWaitingTimeWrapper {

    private WaitingTimeWrapper[] waitingTimeWrappers;

    public MultipleWaitingTimeWrapper(WaitingTimeWrapper... waitingTimeWrappers) {
        this.waitingTimeWrappers = waitingTimeWrappers;
    }

    public class WaitingTimeWrapper {

        private WaitingTime waitingTime;
        private String whatKindOfWaitingTime;

        public WaitingTimeWrapper(String whatKindOfWaitingTime, int lowMonth, int highMonth, String updatedAt, String query) {
            this.whatKindOfWaitingTime = whatKindOfWaitingTime;
            waitingTime = new WaitingTime(lowMonth, highMonth, updatedAt, query);
        }

    }
}