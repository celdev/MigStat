package com.celdev.migstat.model;

import com.celdev.migstat.controller.observercode.Code;
import com.celdev.migstat.controller.utils.DateUtils;

import java.util.Observable;

public class ApplicationWaitingTimePackage extends Observable{

    private Application application;
    private ApplicationNoNumber applicationNoNumber;

    private WaitingTime waitingTime;


    public ApplicationWaitingTimePackage(Application application, WaitingTime waitingTime) {
        this.application = application;
        this.waitingTime = waitingTime;
    }

    public ApplicationWaitingTimePackage(ApplicationNoNumber applicationNoNumber, WaitingTime waitingTime) {
        this.applicationNoNumber = applicationNoNumber;
        this.waitingTime = waitingTime;
    }

    public ApplicationMethod getApplication() {
        return application != null ? application : applicationNoNumber;
    }

    public void setNewApplicationWaitingTime(WaitingTime waitingTime) throws ParserException {
        if (DateUtils.isNewerDate(this.waitingTime.getUpdatedAtDate(), waitingTime.getUpdatedAtDate())) {
            this.waitingTime = waitingTime;
            setChanged();
            notifyObservers(Code.NEW_WAITING_TIME);
        } else {
            this.waitingTime = waitingTime;
            setChanged();
            notifyObservers(Code.UPDATE_WAITING_TIME);
        }
    }



}
