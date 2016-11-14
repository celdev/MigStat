package com.celdev.migstat.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.celdev.migstat.MainActivity;

import com.celdev.migstat.model.Application;
import com.celdev.migstat.model.ApplicationNumberType;
import com.celdev.migstat.model.NoApplicationNumberException;
import com.celdev.migstat.model.StatusType;
import com.celdev.migstat.model.WaitingTime;

public class DataStorage {

    private final static String APPLICATION_NUMBER_KEY = "APPLICATION_NUMBER";
    private final static String APPLICATION_NUMBER_TYPE = "APPLICATION_NUMBER_TYPE";
    private final static String APPLICATION_DATE = "APPLICATION_DATE";
    private final static String APPLICATION_STATUS_TYPE = "APPLICATION_STATUS_TYPE";

    private final static String WAITING_TIME_TYPE_QUERY = "WAITING_TIME_TYPE_QUERY";
    private final static String WAITING_TIME_TYPE_QUERY_MODE = "WAITING_TIME_TYPE_QUERY_MODE";
    private final static String WAITING_TIME_QUERY_CUSTOM_MONTHS = "WAITING_TIME_QUERY_CUSTOM_MONTHS";
    private final static String WAITING_TIME_LOW_MONTH = "WAITING_TIME_LOW_MONTH";
    private final static String WAITING_TIME_HIGH_MONTH = "WAITING_TIME_HIGH_MONTH";
    private final static String WAITING_TIME_UPDATED_AT = "WAITING_TIME_UPDATED_AT";



    private final static String APP_BACKGROUND_INDEX = "APP_BACKGROUND_INDEX";

    private final static String VERSION_KEY = "VERSION_KEY";

    private static final long APP_VERSION = 2L;


    private static DataStorage dataStorage;

    public static DataStorage getInstance(){
        if (dataStorage == null) {
            dataStorage = new DataStorage();
        }
        return dataStorage;
    }

    private DataStorage() {
    }

    public int getBackgroundIndex(Context context) {
        SharedPreferences preferences = getSharedPreference(context);
        return preferences.getInt(APP_BACKGROUND_INDEX, 0);
    }

    public void saveBackgroundIndex(Context context, int index) {
        SharedPreferences preferences = getSharedPreference(context);
        preferences.edit().putInt(APP_BACKGROUND_INDEX, index).apply();
    }


    boolean saveWaitingTimeDataStoragePacket(Context context, WaitingTime waitingTime) {
        if (waitingTime == null) {
            return false;
        }
        SharedPreferences preferences = getSharedPreference(context);
        try {
            preferences.edit().
                    putBoolean(WAITING_TIME_TYPE_QUERY_MODE, waitingTime.isUseCustomMonths()).
                    putInt(WAITING_TIME_QUERY_CUSTOM_MONTHS, waitingTime.getCustomMonths()).
                    putString(WAITING_TIME_TYPE_QUERY, waitingTime.getQuery()).apply();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    boolean saveWaitingTime(Context context, WaitingTime waitingTime) {
        SharedPreferences preferences = getSharedPreference(context);
        if (waitingTime == null) {
            return false;
        }
        try {
            preferences.edit().
                    putBoolean(WAITING_TIME_TYPE_QUERY_MODE, waitingTime.isUseCustomMonths()).
                    putInt(WAITING_TIME_QUERY_CUSTOM_MONTHS, waitingTime.getCustomMonths()).
                    putInt(WAITING_TIME_LOW_MONTH, waitingTime.getLowMonth()).
                    putInt(WAITING_TIME_HIGH_MONTH, waitingTime.getHighMonth()).
                    putString(WAITING_TIME_TYPE_QUERY, waitingTime.getQuery()).
                    putString(WAITING_TIME_UPDATED_AT,waitingTime.getUpdatedAtDate()).apply();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    WaitingTime loadWaitingTime(Context context) throws NoWaitingTimeException{
        SharedPreferences preferences = getSharedPreference(context);
        String waitingTimeQuery = preferences.getString(WAITING_TIME_TYPE_QUERY, "");
        Log.d(MainActivity.LOG_KEY, "Waiting time query = " + waitingTimeQuery);
        int customMonths = preferences.getInt(WAITING_TIME_QUERY_CUSTOM_MONTHS, 0);
        Log.d(MainActivity.LOG_KEY, "custom months = " + customMonths);
        int highMonth = preferences.getInt(WAITING_TIME_HIGH_MONTH, -1);
        Log.d(MainActivity.LOG_KEY,"high month = " + highMonth);
        int lowMonth = preferences.getInt(WAITING_TIME_LOW_MONTH, -1);
        Log.d(MainActivity.LOG_KEY,"low month = " + lowMonth);
        String updatedAt = preferences.getString(WAITING_TIME_UPDATED_AT, "");
        Log.d(MainActivity.LOG_KEY,"updated at = " + updatedAt);
        boolean useCustomMode = preferences.getBoolean(WAITING_TIME_TYPE_QUERY_MODE, false);
        Log.d(MainActivity.LOG_KEY,"use custom mode = " + useCustomMode);
        if (isInvalidWaitingTime(waitingTimeQuery,customMonths,useCustomMode)) {
            throw new NoWaitingTimeException();
        }
        if (waitingTimeQuery.isEmpty()) {
            return new WaitingTime(customMonths);
        }
        Log.d(MainActivity.LOG_KEY, "query is = " + waitingTimeQuery);
        return new WaitingTime(lowMonth, highMonth, updatedAt, waitingTimeQuery, useCustomMode, customMonths);
    }

    private boolean isInvalidWaitingTime(String query, int customMonth, boolean useCustomMonth) {
        return (query.isEmpty() && !useCustomMonth) || (customMonth == 0 && useCustomMonth);
    }

    boolean saveApplication(Context context, Application application) {
        SharedPreferences preferences = getSharedPreference(context);
        if (application == null) {
            return false;
        }
        try {
            if (application.isHasApplicationNumber()) {
                preferences.edit().
                        putInt(APPLICATION_NUMBER_KEY, application.getApplicationNumber().getApplicationNumber()).
                        putInt(APPLICATION_NUMBER_TYPE, application.getApplicationNumber().getApplicationNumberType().getMigrationsverketQueryNumber()).
                        putLong(APPLICATION_DATE, application.getApplicationDate()).
                        putInt(APPLICATION_STATUS_TYPE, application.getApplicationStatus().getStatusType().getNumber()).
                        apply();
            } else {
                preferences.edit().
                        putLong(APPLICATION_DATE, application.getApplicationDate()).
                        apply();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    Application loadApplication(Context context) throws NoApplicationException{
        SharedPreferences preferences = getSharedPreference(context);
        try {
            long applicationDate = preferences.getLong(APPLICATION_DATE, -1);
            Log.d(MainActivity.LOG_KEY, "application date: " + applicationDate);
            if (applicationDate != -1) {
                int applicationNumber = preferences.getInt(APPLICATION_NUMBER_KEY, -1);
                Log.d(MainActivity.LOG_KEY, "application number = " + applicationNumber);
                if (applicationNumber == -1) {
                    return new Application(applicationDate);
                }
                int applicationNumberType = preferences.getInt(APPLICATION_NUMBER_TYPE, -1);
                Log.d(MainActivity.LOG_KEY, "number type = " + applicationNumberType);
                int applicationStatusNumber = preferences.getInt(APPLICATION_STATUS_TYPE, -1);
                Log.d(MainActivity.LOG_KEY, "status number = " + applicationStatusNumber);
                if (applicationNumberType != -1 && applicationStatusNumber != -1) {
                    return new Application(
                            StatusType.statusNumberToStatusType(applicationStatusNumber),
                            applicationDate, applicationNumber,
                            ApplicationNumberType.numberToNumberType(applicationNumberType));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new NoApplicationException();
    }


    private SharedPreferences getSharedPreference(Context context) {
        return context.getSharedPreferences(MainActivity.APPLICATION_KEY, Context.MODE_PRIVATE);
    }

    private boolean firstTimeNewVersion(Context context) {
        boolean returnValue = true;
        try {
            SharedPreferences preferences = getSharedPreference(context);
            if (preferences.contains(VERSION_KEY)) {
                long version = preferences.getLong(VERSION_KEY, -1);
                returnValue = version < APP_VERSION;
            }
            saveVersion(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    public void checkVersionResetIfNeeded(Context context) {
        if (firstTimeNewVersion(context)) {
            deleteAllData(context);
        }
    }

    private void saveVersion(Context context) {
        getSharedPreference(context).edit().putLong(VERSION_KEY, APP_VERSION).apply();
    }

    void deleteAllData(Context context) {
        getSharedPreference(context).edit().clear().apply();
    }

    void deleteWaitingTime(Context context) {
        SharedPreferences preferences = getSharedPreference(context);
        preferences.edit().
                remove(WAITING_TIME_TYPE_QUERY_MODE).
                remove(WAITING_TIME_QUERY_CUSTOM_MONTHS).
                remove(WAITING_TIME_LOW_MONTH).
                remove(WAITING_TIME_UPDATED_AT).
                remove(WAITING_TIME_HIGH_MONTH).
                remove(WAITING_TIME_TYPE_QUERY).apply();
    }

}
