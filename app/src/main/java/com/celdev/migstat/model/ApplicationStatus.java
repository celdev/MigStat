package com.celdev.migstat.model;

import android.content.Context;
import android.content.res.Resources;

import com.celdev.migstat.R;

public class ApplicationStatus {

    private int status;
    private final String statusString;

    ApplicationStatus(Context context, int status) throws IllegalArgumentException{
        if (context == null || status < 0 || status > 2) {
            throw new IllegalArgumentException("Context null or status not valid");
        }
        this.status = status;
        try {
            this.statusString = context.getResources().getStringArray(R.array.case_status_types)[status];
        } catch (Resources.NotFoundException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Error getting String from resources");
        }
    }

    private ApplicationStatus(Context context) {
        this.status = -1;
        this.statusString = context.getResources().getString(R.string.not_using_case_number);
    }

    static ApplicationStatus NoApplicationStatus(Context context) {
        return new ApplicationStatus(context);
    }

    public StatusType getStatusType() {
        return StatusType.statusNumberToStatusType(status);
    }

    public String getStatusString() {
        return statusString;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ApplicationStatus{" +
                "status=" + StatusType.statusNumberToStatusType(status) +
                ", statusString='" + statusString + '\'' +
                '}';
    }
}
