package com.celdev.migstat.controller.parser;

import com.celdev.migstat.model.WaitingTime;

public interface AsyncTaskResultReceiver {

    void receiveResult(WaitingTime waitingTime);

}
