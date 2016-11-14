package com.celdev.migstat.model;

import android.content.Context;
import android.content.res.Resources;

import com.celdev.migstat.R;

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
