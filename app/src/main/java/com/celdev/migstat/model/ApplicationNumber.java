package com.celdev.migstat.model;

/*  This class contains the application number and the application number type
*
*
*   It is possible that the number should be stored as a String since it
*   may start with a zero, however, so far I haven't seen any numbers starting with zero
* */
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
