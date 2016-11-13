package com.celdev.migstat.controller;

import com.celdev.migstat.controller.parser.AsyncTaskResultReceiver;
import com.celdev.migstat.controller.parser.SimpleCaseStatusParser;
import com.celdev.migstat.model.Application;
import com.celdev.migstat.model.ApplicationNumber;
import com.celdev.migstat.model.ApplicationNumberType;
import com.celdev.migstat.model.NoApplicationNumberException;
import com.celdev.migstat.view.ViewUpdateReceiver;

public class ApplicationStatusChecker implements AsyncTaskResultReceiver{

    private ViewUpdateReceiver viewUpdateReceiver;

    public ApplicationStatusChecker(ViewUpdateReceiver viewUpdateReceiver) {
        this.viewUpdateReceiver = viewUpdateReceiver;
    }

    public void checkApplication(int applicationNumber, ApplicationNumberType applicationNumberType) {
        new SimpleCaseStatusParser.Worker(this).execute(new ApplicationNumber(applicationNumber, applicationNumberType));
    }

    public void checkApplication(Application application) {
        try {
            ApplicationNumber applicationNumber = application.getApplicationNumber();
            checkApplication(applicationNumber.getApplicationNumber(), applicationNumber.getApplicationNumberType());
        } catch (NoApplicationNumberException e) {
            e.printStackTrace();
            receiveResult(null);
        }
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
