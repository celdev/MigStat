package com.celdev.migstat.controller.utils;

import com.celdev.migstat.model.ParserException;
import java.util.Calendar;

/*  This class contains static helper methods for date-manipulation and conversion
* */
public class DateUtils {


    /*  Converts a YYYY-MM-DD string into Calendar object
    *   Throws an exception is the date isn't valid
    * */
    public static Calendar dateStringToCalendar(String date) throws ParserException {
        String[] split = date.split("-");
        int year = Integer.valueOf(split[0]);
        int month = Integer.valueOf(split[1]) - 1;
        int day = Integer.valueOf(split[2]);
        if (isValidDate(year, month, day)) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, 0, 0, 0);
            return calendar;
        }
        throw new ParserException("Invalid date");
    }

    /*  returns true if the date is valid
    *   month is zero-based so January = 0... December = 11
    * */
    public static boolean isValidDate(int year, int month, int day) {
        return !(month < 0 || month > 11 || day < 0 || !isValidDay(year, month, day));
    }

    /*  returns true if the day is valid
    *   1. day is greater than 0
    *   and
    *   2. day is less than or equal to 31 (for 31-day months) or
    *   2. day is less than or equal to 30 (for 30-day months) or
    *   2. day is less than or equal to 28 (for february and not leap year) or
    *   2. day is less than or equal to 29 (for february and leap year)
    * */
    private static boolean isValidDay(int year, int month, int day) {
        if (is31DayMonth(month)) {
            return day > 0 && day <= 31;
        } else if (month == 1){
            return day > 0 && (isLeapYear(year) ? day <= 29 : day <= 28);
        } else {
            return day > 0 && day <= 30;
        }
    }

    /*  Returns true if it's a leap year (may return false value if year is before 1583
    *   however the use case for this application is for years "current year"-2 to "current year" */
    private static boolean isLeapYear(int year) {
        return ((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0);
    }

    /*  return true if the zero-based month-number passed in is a 31-day month
    *   january (0), march, may, july, august, october and december(11)*/
    private static boolean is31DayMonth(int month) {
        return month == 0 || month == 2 || month == 4 || month == 6 || month == 7 || month == 9 || month == 11;
    }

    /* returns  1 if the first string is the older date
     * returns -1 if the second string is the older date
     * returns  0 if the first == second
     */
    public static int compareDateStrings(String first, String second) throws ParserException {
        if(first.equals(second)) return 0;
        Calendar firstC = dateStringToCalendar(first);
        Calendar secondC = dateStringToCalendar(second);
        return secondC.compareTo(firstC);
    }

    //returns true if first is an older date than second.
    public static boolean isNewerDate(String first, String second) throws ParserException {
        return 1 == compareDateStrings(first, second);
    }

}
