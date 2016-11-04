package com.celdev.migstat.controller;

public class NoWaitingTimeException extends Exception {

    public NoWaitingTimeException() {
    }

    public NoWaitingTimeException(String message) {
        super(message);
    }
}
