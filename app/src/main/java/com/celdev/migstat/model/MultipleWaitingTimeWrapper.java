package com.celdev.migstat.model;

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

        public WaitingTimeWrapper(String whatKindOfWaitingTime, int days, String updatedAt, String query) {
            this.whatKindOfWaitingTime = whatKindOfWaitingTime;
            waitingTime = new WaitingTime(days, updatedAt, query);
        }
    }
}