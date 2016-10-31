package com.celdev.migstat.model;

import android.content.Context;

import java.util.Observable;

public class ApplicationCreator{

    public static ApplicationMethod createApplication(Context context, StatusType status, long applicationDate, int applicationNumber, ApplicationNumberType applicationNumberType) {
        if (status.equals(StatusType.PRIVACY_MODE)) {
            return new ApplicationNoNumber(context, applicationDate);
        }
        return new Application(context, status, applicationDate, applicationNumber, applicationNumberType);
    }
}

