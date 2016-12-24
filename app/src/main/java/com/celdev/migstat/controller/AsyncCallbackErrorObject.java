package com.celdev.migstat.controller;

//used when a parser throws an exception to the async result receiver or if it returns null
public enum  AsyncCallbackErrorObject {

    NO_APPLICATION_NUMBER,
    NO_WAITINGTIME_QUERY,
    PARSER_EXCEPTION;
}
