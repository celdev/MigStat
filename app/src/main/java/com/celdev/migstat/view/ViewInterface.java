package com.celdev.migstat.view;


/*  This interface provides a way for the controller to
*   inform the view of an update and specify what that update was
* */
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
