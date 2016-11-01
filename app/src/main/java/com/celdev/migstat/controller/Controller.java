package com.celdev.migstat.controller;

import com.celdev.migstat.controller.parser.AsyncTaskResultReceiver;
import com.celdev.migstat.controller.parser.SimpleCaseStatusParser;
import com.celdev.migstat.model.ApplicationNumber;
import com.celdev.migstat.model.ApplicationNumberType;
import com.celdev.migstat.view.ViewUpdateReceiver;

public class Controller implements AsyncTaskResultReceiver{

    private ViewUpdateReceiver viewUpdateReceiver;

    public Controller(ViewUpdateReceiver viewUpdateReceiver) {
        this.viewUpdateReceiver = viewUpdateReceiver;
    }

    public void checkApplication(int applicationNumber, ApplicationNumberType applicationNumberType) {
        new SimpleCaseStatusParser.Worker(this).execute(new ApplicationNumber(applicationNumber, applicationNumberType));
    }

    @Override
    public void receiveResult(Object waitingTime) {
        if (waitingTime == null) {
            viewUpdateReceiver.receiveUpdate(null);
        } else {
            if (waitingTime instanceof SimpleCaseStatusParser.StatusAndDate) {
                SimpleCaseStatusParser.StatusAndDate s = (SimpleCaseStatusParser.StatusAndDate) waitingTime;
                viewUpdateReceiver.receiveUpdate(s);
            }
        }
    }
}
