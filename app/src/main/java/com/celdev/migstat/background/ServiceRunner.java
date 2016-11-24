package com.celdev.migstat.background;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.icu.text.LocaleDisplayNames;
import android.nfc.Tag;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.celdev.migstat.MainActivity;
import com.celdev.migstat.controller.Controller;
import com.celdev.migstat.view.ViewInterface;


public class ServiceRunner extends Service implements ViewInterface{


    private Controller controller;
    private ServiceThread serviceThread;

    private static final String TAG = MainActivity.LOG_KEY + ".Service";


    @Override
    public void modelChange(ModelChange modelChange) {
        if (modelChange != null) {
            switch (modelChange) {
                case FINISHED:
                    NotificationHelper.doFinishedApplicationStatusNotification(this);
                    Log.d(TAG, "Service received modelChange: Finished");
                    break;
                case APPLICATION:
                    Log.d(TAG, "Service received modelChange: Application");
                    break;
                case ERROR_UPDATE:
                case INVALID_APPLICATION:
                    if (serviceThread != null) {
                        serviceThread.kill();
                    }
                    Log.d(TAG, "Service Recieved error");
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

    @Override
    public void onCreate() {
        super.onCreate();
        controller = new Controller(this, this);
        serviceThread = new ServiceThread();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceThread.kill();
        serviceThread = null;
        controller = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (serviceThread == null) {
            serviceThread = new ServiceThread();
        }
        if (!serviceThread.alive) {
            serviceThread.start();
        }
        return Service.START_STICKY;
    }

    private class ServiceThread extends Thread {

        private boolean alive = false;

        @Override
        public void run() {
            int debug = 0;
            alive = true;
            while (alive) {
                Log.d(TAG, "running service update");
                controller.updateApplicationAndWaitingTime();
                try {
                    Log.d(TAG, "sleeping service thread");
                    Thread.sleep(300000);
                } catch (InterruptedException e) {
                    kill();
                }
                debug++;
                if (debug == 10) {
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
