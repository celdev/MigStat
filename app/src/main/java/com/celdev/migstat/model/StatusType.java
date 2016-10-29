package com.celdev.migstat.model;

public enum StatusType {

    RECEIVED(0),
    WAITING(1),
    FINISHED(2),
    PRIVACY_MODE(-1);

    private int n;

    StatusType(int n) {
        this.n = n;
    }

    public int getNumber() {
        return n;
    }

    public static StatusType statusNumberToStatusType(int number) throws ArrayIndexOutOfBoundsException {
        if (number == -1) {
            return PRIVACY_MODE;
        }
        if (number < 0 || number > values().length - 1) {
            throw new ArrayIndexOutOfBoundsException("Status number out of range");
        }
        return values()[number];
    }
}
