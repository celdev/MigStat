package com.celdev.migstat.controller.parser;

/*  Classes implementing this interface are capable of handling
*   the result of an AsyncTask
* */
public interface AsyncTaskResultReceiver {

    void receiveResult(Object object);

}
