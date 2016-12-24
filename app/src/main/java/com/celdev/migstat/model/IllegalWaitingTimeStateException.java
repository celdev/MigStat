package com.celdev.migstat.model;

/*  this may be thrown if the controller tries to change the waiting time
*   to a mode that isn't set (i.e. setting to custom months mode
*   when custom months vale isn't set)
* */
public class IllegalWaitingTimeStateException extends Exception {
}
