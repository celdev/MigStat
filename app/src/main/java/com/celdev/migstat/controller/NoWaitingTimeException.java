package com.celdev.migstat.controller;

public class NoWaitingTimeException extends DataStorageLoadException {

    public NoWaitingTimeException() {
    }

    public NoWaitingTimeException(String message) {
        super(message);
    }
}
