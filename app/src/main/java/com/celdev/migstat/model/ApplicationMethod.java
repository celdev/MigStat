package com.celdev.migstat.model;

interface ApplicationMethod {

    long getApplicationDate();

    ApplicationStatus getApplicationStatus() throws NoApplicationNumberException;

    ApplicationNumber getApplicationNumber() throws NoApplicationNumberException;




}
