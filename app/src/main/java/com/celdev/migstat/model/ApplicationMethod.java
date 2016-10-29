package com.celdev.migstat.model;

public interface ApplicationMethod {

    long getApplicationDate();

    ApplicationStatus getApplicationStatus() throws NoApplicationNumberException;

    ApplicationNumber getApplicationNumer() throws NoApplicationNumberException;

    


}
