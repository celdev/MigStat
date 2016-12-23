package com.celdev.migstat.view;

/* this method provides a way for the NumberPickerDialog to return the number picked to
*  the Activity that created it */
public interface NumberPickerDialogReturn {

    void returnOnOk(int months);

}
