package com.celdev.migstat.controller;

import android.util.Log;

import com.celdev.migstat.MainActivity;
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
            Log.d(MainActivity.LOG_KEY, "check appplication result object class = StatusAndDate");
            asyncCallback.receiveAsyncResult(result);
        } else {
            Log.d(MainActivity.LOG_KEY, "check application parser exception");
            asyncCallback.receiveAsyncResult(AsyncCallbackErrorObject.PARSER_EXCEPTION);
        }
    }
}
