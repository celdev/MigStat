package com.celdev.migstat.controller;

/* is thrown if an application doesn't have a waiting time set
 * when restoring the data from the persistence layer
 * */
public class NoWaitingTimeException extends DataStorageLoadException {

    public NoWaitingTimeException() {
    }

    public NoWaitingTimeException(String message) {
        super(message);
    }
}
