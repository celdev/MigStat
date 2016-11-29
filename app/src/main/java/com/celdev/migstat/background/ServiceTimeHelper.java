package com.celdev.migstat.background;

import android.icu.util.TimeZone;
import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class ServiceTimeHelper {

    private static long timeAtLastRequest = 0L;
    public static final int OPENING_TIME = 8;
    public static final int CLOSING_TIME = 18;

    public static long getMsToNextRequest() {
        if (timeAtLastRequest == 0) {
            Log.d(ServiceRunner.TAG, "first request, return 1 ms");
            timeAtLastRequest = System.currentTimeMillis();
            return 1;
        }
        timeAtLastRequest = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("Europe/Stockholm"));
        if (isWorkingHours(calendar)) {
            Log.d(ServiceRunner.TAG, "is working hours, returning default MS between updates");
            return ServiceRunner.MS_BETWEEN_UPDATES;
        } else {
            Log.d(ServiceRunner.TAG, "not working hours, returning time until working hours");
            return getMsToMigrationsverketOpeningTime(calendar);
        }
    }

    private static boolean isWorkingHours(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour >= OPENING_TIME && hour <= CLOSING_TIME;
    }

    private static long getMsToMigrationsverketOpeningTime(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int hoursToOpeningHour;
        if (hour > OPENING_TIME) {
            hoursToOpeningHour = (24 - hour) + OPENING_TIME;
        } else {
            hoursToOpeningHour = OPENING_TIME - hour;
        }
        int minutesToOpeningHour = hoursToOpeningHour * 60 - minute;
        return TimeUnit.MINUTES.toMillis(minutesToOpeningHour);
    }


}
