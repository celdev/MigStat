package com.celdev.migstat.view;

import android.app.DatePickerDialog;
import android.content.Context;
import java.util.Calendar;

/*  Creates a DatePickerDialog with the max date = todays date.
* */
public class CustomDatePickerDialog extends DatePickerDialog{

    public CustomDatePickerDialog(Context context, Calendar calendar, OnDateSetListener onDateSetListener) {
        super(context, onDateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        getDatePicker().setMaxDate(calendar.getTimeInMillis());
    }



}
