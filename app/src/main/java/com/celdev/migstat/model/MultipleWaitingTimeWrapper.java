package com.celdev.migstat.model;

public class MultipleWaitingTimeWrapper {

    private WaitingTimeWrapper[] waitingTimeWrappers;

    public MultipleWaitingTimeWrapper(WaitingTimeWrapper... waitingTimeWrappers) {
        this.waitingTimeWrappers = waitingTimeWrappers;
    }

    public class WaitingTimeWrapper {

        private WaitingTime waitingTime;
        private String whatKindOfWaitingTime;

        public WaitingTimeWrapper(String whatKindOfWaitingTime, int lowMonth, int highMonth, String updatedAt) {
            this.whatKindOfWaitingTime = whatKindOfWaitingTime;
            waitingTime = new WaitingTime(lowMonth, highMonth, updatedAt);
        }

        public WaitingTimeWrapper(String whatKindOfWaitingTime, int days, String updatedAt) {
            this.whatKindOfWaitingTime = whatKindOfWaitingTime;
            waitingTime = new WaitingTime(days, updatedAt);
        }
    }
}