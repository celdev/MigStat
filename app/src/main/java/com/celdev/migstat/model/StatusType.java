package com.celdev.migstat.model;

/*  This enum represents the different statuses an application can have
*
*   Received = the application has been received by the migrationsverket
*               it seems like this status is never used, it's possible that this
*               status type is used when submitting a paper application and the
*               paper hasn't been processed by the migrationsverket
*
*   Waiting =  the application is waiting for a decision
*
*   Finished = A decision has been made for the application
*
*   It also contains a static method for converting an integer to a StatusType
*
* */
public enum StatusType {

    RECEIVED(0),
    WAITING(1),
    FINISHED(2);

    private int n;

    StatusType(int n) {
        this.n = n;
    }

    public int getNumber() {
        return n;
    }

    public static StatusType statusNumberToStatusType(int number) throws ArrayIndexOutOfBoundsException {
        return values()[number];
    }
}
