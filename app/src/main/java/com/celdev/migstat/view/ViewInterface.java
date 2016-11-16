package com.celdev.migstat.view;

import com.celdev.migstat.model.Application;

public interface ViewInterface {

    enum ModelChange {
        APPLICATION,
        APPLICATION_OK,
        WAITING_TIME,
        ERROR_UPDATE,
        NEW_WAITING_TIME(),
        SAVE_SUCCESS,
        SAVE_FAILED,
        FINISHED,
        INVALID_APPLICATION,
        WAITING_TIME_OK,
        MULTIPLE_WAITING_TIME
    }

    void modelChange(ModelChange modelChange);


}
