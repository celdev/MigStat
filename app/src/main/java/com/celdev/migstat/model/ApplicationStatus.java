package com.celdev.migstat.model;

/*  Wraps the StatusType enum
*
*   contains methods for translating an integer to a StatusType
*   during parsing the status will be extracted by determining which
*   li-element in a list have the active class
*   which is a zero based index with
*       0 = received
*       1 = waiting
*       2 = finished
* */
public class ApplicationStatus {

    private int status;


    ApplicationStatus(int status) throws IllegalArgumentException{
        if (status < 0 || status > 2) {
            throw new IllegalArgumentException("status not valid");
        }
        this.status = status;
    }

    public StatusType getStatusType() {
        return StatusType.statusNumberToStatusType(status);
    }


    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ApplicationStatus{" +
                "status=" + StatusType.statusNumberToStatusType(status) +
                '}';
    }
}
