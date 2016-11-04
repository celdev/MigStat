package com.celdev.migstat.controller;

import android.content.Context;
import android.content.SharedPreferences;

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
    private final static String APPLICATION_TYPE_QUERY = "APPLICATION_TYPE_QUERY";
    private final static String APPLICATION_TYPE_QUERY_MODE = "APPLICATION_TYPE_QUERY_MODE";
    private final static String APPLICATION_TYPE_QUERY_CUSTOM_MONTHS = "APPLICATION_TYPE_QUERY_CUSTOM_MONTHS";
    private final static String PREFERENCE_KEY = MainActivity.APPLICATION_KEY + ".";

    private final static String VERSION_KEY = "VERSION_KEY";

    private static final long APP_VERSION = 1L;


    private static DataStorage dataStorage;

    public static DataStorage getInstance(){
        if (dataStorage == null) {
            dataStorage = new DataStorage();
        }
        return dataStorage;
    }

    private DataStorage() {
    }


    public Application loadApplication(Context context) throws NoApplicationException{
        SharedPreferences preferences = getSharedPreference(context);
        try {
            int applicationNumber = preferences.getInt(APPLICATION_NUMBER_KEY, -1);
            long applicationDateMS = preferences.getLong(APPLICATION_DATE, 0);
            if (applicationDateMS == 0) {
                throw new NoApplicationException();
            }
            if(applicationNumber == -1){
                return new Application(applicationDateMS);
            }
            int applicationTypeNumber = preferences.getInt(APPLICATION_NUMBER_TYPE, -1);
            if (applicationTypeNumber != 1 && applicationTypeNumber != 2) {
                throw new NoApplicationException();
            }
            int status = preferences.getInt(APPLICATION_STATUS_TYPE, -1);
            if (status != 0 && status != 1 && status != 2) {
                throw new NoApplicationException();
            }
            return new Application(context, StatusType.statusNumberToStatusType(status), applicationDateMS,
                    applicationNumber, ApplicationNumberType.numberToNumberType(applicationTypeNumber));

        } catch (Exception e) {
            throw new NoApplicationException();
        }
    }

    public boolean saveWaitingTimeQuery(Context context, String query) {
        SharedPreferences preferences = getSharedPreference(context);
        try {
            preferences.edit().
                    putBoolean(APPLICATION_TYPE_QUERY_MODE, false).
                    putString(APPLICATION_TYPE_QUERY, query).apply();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean saveApplicationNotUseApplicationNumber(Context context,long applicationDate) {
        return saveApplication(context, new Application(applicationDate));
    }

    public boolean saveWaitingTimeDataStoragePacket(Context context, WaitingTime waitingTime) {
        if (waitingTime == null) {
            return false;
        }
        SharedPreferences preferences = getSharedPreference(context);
        try {
            preferences.edit().
                    putBoolean(APPLICATION_TYPE_QUERY_MODE, waitingTime.isUseCustomMonths()).
                    putInt(APPLICATION_TYPE_QUERY_CUSTOM_MONTHS, waitingTime.getCustomMonths()).
                    putString(APPLICATION_TYPE_QUERY, waitingTime.getQuery()).apply();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean saveApplication(Context context, Application application) {
        if (application == null) {
            return false;
        }
        try {
            SharedPreferences preferences = getSharedPreference(context);
            int applicationNumber, applicationNumberType, applicationStatus;
            long applicationDate = application.getApplicationDate();
            try {
                applicationNumber = application.getApplicationNumber().getApplicationNumber();
                applicationNumberType = application.getApplicationNumber().getApplicationNumberType().getMigrationsverketQueryNumber();
                applicationStatus = application.getApplicationStatus().getStatusType().getNumber();
            } catch (NoApplicationNumberException e) {
                applicationNumber = -1;
                applicationNumberType = -1;
                applicationStatus = StatusType.PRIVACY_MODE.getNumber();
            }
            preferences.edit().
                    putInt(APPLICATION_NUMBER_KEY, applicationNumber).
                    putInt(APPLICATION_NUMBER_TYPE, applicationNumberType).
                    putInt(APPLICATION_STATUS_TYPE, applicationStatus).
                    putLong(APPLICATION_DATE, applicationDate).
                    apply();
            if (application.getWaitingTime() != null) {
                saveWaitingTimeDataStoragePacket(context, application.getWaitingTime());
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public WaitingTimeDataStoragePacket loadWaitingTime(Context context) throws NoWaitingTimeException{
        SharedPreferences preferences = getSharedPreference(context);
        String applicationTypeQuery = preferences.getString(APPLICATION_TYPE_QUERY, "");
        int applicationTypeCustomMonths = preferences.getInt(APPLICATION_TYPE_QUERY_CUSTOM_MONTHS, 0);
        boolean applicationTypeCustomMonthsUseCustomMode = preferences.getBoolean(APPLICATION_TYPE_QUERY_MODE, false);
        if (isInvalidWaitingTime(applicationTypeQuery,applicationTypeCustomMonths,applicationTypeCustomMonthsUseCustomMode)) {
            throw new NoWaitingTimeException();
        }
        return new WaitingTimeDataStoragePacket(applicationTypeQuery, applicationTypeCustomMonthsUseCustomMode, applicationTypeCustomMonths);
    }

    private boolean isInvalidWaitingTime(String query, int customMonth, boolean useCustomMonth) {
        return (query.isEmpty() && !useCustomMonth) || (customMonth == 0 && useCustomMonth);
    }


    private SharedPreferences getSharedPreference(Context context) {
        return context.getSharedPreferences(MainActivity.APPLICATION_KEY, Context.MODE_PRIVATE);
    }

    public boolean firstTimeNewVersion(Context context) {
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

    private void saveVersion(Context context) {
        getSharedPreference(context).edit().putLong(VERSION_KEY, APP_VERSION).apply();
    }

    public void deleteAllData(Context context) {
        getSharedPreference(context).edit().clear().apply();
    }

    public void DEBUG_deleteWaitingTime(Context context) {
        SharedPreferences preferences = getSharedPreference(context);
        preferences.edit().
                remove(APPLICATION_TYPE_QUERY_MODE).
                remove(APPLICATION_TYPE_QUERY_CUSTOM_MONTHS).
                remove(APPLICATION_TYPE_QUERY).apply();
    }

}
