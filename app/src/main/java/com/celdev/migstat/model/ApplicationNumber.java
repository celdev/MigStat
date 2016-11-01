package com.celdev.migstat.model;

public class ApplicationNumber {

    private final int applicationNumber;
    private final ApplicationNumberType applicationNumberType;

    public ApplicationNumber(int applicationNumber, ApplicationNumberType applicationNumberType) {
        this.applicationNumber = applicationNumber;
        this.applicationNumberType = applicationNumberType;
    }

    public int getApplicationNumber() {
        return applicationNumber;
    }

    public ApplicationNumberType getApplicationNumberType() {
        return applicationNumberType;
    }

    @Override
    public String toString() {
        return "ApplicationNumber{" +
                "applicationNumber=" + applicationNumber +
                ", applicationNumberType=" + applicationNumberType +
                '}';
    }
}
