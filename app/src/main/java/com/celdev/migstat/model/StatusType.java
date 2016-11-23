package com.celdev.migstat.model;

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
