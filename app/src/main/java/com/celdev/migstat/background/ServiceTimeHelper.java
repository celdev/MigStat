package com.celdev.migstat.background;

import android.util.Log;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/*  This class contains static methods for calculating the amount of MS until the next
*   service application status update should be done.
* */
class ServiceTimeHelper {

    private static long timeAtLastRequest = 0L;
    private static final int OPENING_TIME = 8;
    private static final int CLOSING_TIME = 18;

    /*  returns 1 (ms) if this is the first time this method is called
    *
    *   returns 30 (minutes in ms) if it's between 8 and 19 (18-19 is considered open)
    *   otherwise it will return the amount of ms until migrationsverket open again (8 am swedish time)
    * */
    static long getMsToNextRequest() {
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

    /*  returns true if the current hour is later or equal to 8 and less or equal to 18
    *
    *   returns true if hour is between 8:00 and 18.59 swedish time.
    * */
    private static boolean isWorkingHours(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour >= OPENING_TIME && hour <= CLOSING_TIME;
    }

    /*  returns the amount of ms until migrationsverket open again. (8 am swedish time)
    * */
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
