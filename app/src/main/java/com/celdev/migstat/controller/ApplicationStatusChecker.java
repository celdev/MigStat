package com.celdev.migstat.controller;

import com.celdev.migstat.controller.parser.AsyncTaskResultReceiver;
import com.celdev.migstat.controller.parser.SimpleCaseStatusParser;
import com.celdev.migstat.model.Application;
import com.celdev.migstat.model.ApplicationNumber;
import com.celdev.migstat.model.ApplicationNumberType;
import com.celdev.migstat.model.NoApplicationNumberException;

public class ApplicationStatusChecker implements AsyncTaskResultReceiver{

    private AsyncCallback asyncCallback;

    public ApplicationStatusChecker(AsyncCallback asyncCallback) {
        this.asyncCallback = asyncCallback;
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
            receiveResult(AsyncCallbackErrorObject.NO_APPLICATION_NUMBER);
        }
    }

    @Override
    public void receiveResult(Object result) {
        if (result instanceof SimpleCaseStatusParser.StatusAndDate) {
            asyncCallback.receiveAsyncResult(result);
        } else {
            asyncCallback.receiveAsyncResult(AsyncCallbackErrorObject.PARSER_EXCEPTION);
        }
    }
}
