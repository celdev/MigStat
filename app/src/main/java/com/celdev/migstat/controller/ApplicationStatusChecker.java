package com.celdev.migstat.controller;

import android.util.Log;

import com.celdev.migstat.MainActivity;
import com.celdev.migstat.controller.parser.AsyncTaskResultReceiver;
import com.celdev.migstat.controller.parser.SimpleCaseStatusParser;
import com.celdev.migstat.model.Application;
import com.celdev.migstat.model.ApplicationNumber;
import com.celdev.migstat.model.ApplicationNumberType;
import com.celdev.migstat.model.NoApplicationNumberException;

/*  This class checks the status of an application
*   the result of the check will be passed to the AsyncCallback-object passed
*   in the constructor
* */
public class ApplicationStatusChecker implements AsyncTaskResultReceiver{

    private AsyncCallback asyncCallback;

    public ApplicationStatusChecker(AsyncCallback asyncCallback) {
        this.asyncCallback = asyncCallback;
    }

    /*  Checks the application (checks if it is valid and if so extracts the application date and status
    * */
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

    /*  The results of the check will be received in this method and if the check was
    *   successful the StatusAndDate-object will be passed to the AsyncCallback-object
    *   passed as a parameter in this objects constructor
    *
    *   if something goes wrong during the check PARSER_EXCEPTION will be returned to the
    *   AsyncCallback-object
    * */
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
