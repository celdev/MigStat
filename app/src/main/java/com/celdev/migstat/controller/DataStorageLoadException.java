package com.celdev.migstat.controller;

/*  Used as a super class to both NoApplicationException and NoWaitingTimeException
* */
public class DataStorageLoadException extends Exception {
    public DataStorageLoadException() {
        super();
    }

    public DataStorageLoadException(String message) {
        super(message);
    }
}
