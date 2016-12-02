package com.celdev.migstat.background;

import android.app.Service;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.celdev.migstat.BuildConfig;
import com.celdev.migstat.MainActivity;
import com.celdev.migstat.controller.Controller;
import com.celdev.migstat.view.ViewInterface;

import java.util.Calendar;
import java.util.TimeZone;


public class ServiceRunner extends Service implements ViewInterface{


    private ServiceThread serviceThread;

    public static final long MS_BETWEEN_UPDATES = 30 * 60 * 1000;

    public static final String TAG = MainActivity.LOG_KEY + ".Service";


    /*  this method will be called when an async operation returns
    *   that was originally called from this object
    *
    *   if the application has gotten a decision the FINISHED-enum will be passed into this
    *   method and a notification will be created and the service will be turned off.
    *
    *   if the application doesn't use an application number this service will be turned off
    *
    * */
    @Override
    public void modelChange(ModelChange modelChange) {
        if (modelChange != null) {
            switch (modelChange) {
                case FINISHED:
                    NotificationHelper.doFinishedApplicationStatusNotification(this);
                    Log.d(TAG, "Service received modelChange: Finished");
                    if (serviceThread != null) {
                        serviceThread.kill();
                        stopSelf();
                    }
                    break;
                case APPLICATION:
                    Log.d(TAG, "Service received modelChange: Application");
                    break;
                case ERROR_UPDATE:
                case INVALID_APPLICATION:
                    if (serviceThread != null) {
                        serviceThread.kill();
                    }
                    Log.d(TAG, "Service Received error");
                    break;
                default:
                    Log.d(TAG, modelChange.name());
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*  this method is called when the service is created.
    *   creates the thread what will do the background work.
    * */
    @Override
    public void onCreate() {
        super.onCreate();
        serviceThread = new ServiceThread();
    }

    /*  called when the service is stopped.
    *   kills the background thread.
    * */
    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceThread.kill();
        serviceThread = null;
    }

    /*  this method is called when the service is started.
    *   if the thread isn't null and is't already alive the thread will be started
    *   returns START_STICKY
    * */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (serviceThread == null) {
            serviceThread = new ServiceThread();
            serviceThread.start();
        }
        if (!serviceThread.alive) {
            serviceThread.start();
        }
        return Service.START_STICKY;
    }

    /*  This thread will do the background work in this service.
    *   Every MS_BETWEEN_UPDATES (30 minutes) the application status will be updated
    *   as long as the time is in the migrationsverket opening hours (8-18)
    * */
    private class ServiceThread extends Thread {

        private boolean alive = false;

        @Override
        public void run() {
            alive = true;
            long sleep = 0;
            while (alive) {
                try {
                    sleep = ServiceTimeHelper.getMsToNextRequest();
                    Log.d(TAG, "sleeping service thread for " + sleep + " ms");
                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Stockholm"));
                    calendar.setTimeInMillis(System.currentTimeMillis() + sleep);
                    Log.d(TAG, "thread will stop sleep at " + calendar.toString());
                    Thread.sleep(sleep);
                    Log.d(TAG, "running service update");
                    new Controller(ServiceRunner.this,ServiceRunner.this).updateApplicationAndWaitingTime();
                } catch (Exception e) {
                    e.printStackTrace();
                    kill();
                }
            }
        }

        void kill() {
            ServiceRunner.this.stopSelf();
            Log.d(TAG, "killing service thread");
            alive = false;
        }
    }
}
