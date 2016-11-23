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

import static com.celdev.migstat.view.ViewInterface.ModelChange.*;


public class ServiceRunner extends Service implements ViewInterface{


    private Controller controller;
    private ServiceThread serviceThread;


    @Override
    public void modelChange(ModelChange modelChange) {
        if (modelChange != null) {
            switch (modelChange) {
                case FINISHED:
                    NotificationHelper.doFinishedApplicationStatusNotification(this);
                    Log.d(MainActivity.LOG_KEY, "Service received modelChange: Finished");
                    break;
                case APPLICATION:
                    NotificationHelper.doFinishedApplicationStatusNotification(this);
                    Log.d(MainActivity.LOG_KEY, "Service received modelChange: Application");
                    break;
                case ERROR_UPDATE:
                case INVALID_APPLICATION:
                    Log.d(MainActivity.LOG_KEY, "Service Recieved error");
                    break;
                default:
                    Log.d(MainActivity.LOG_KEY, modelChange.name());
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
        controller = Controller.getInstance(getApplicationContext(), this);
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
        serviceThread = new ServiceThread();
        return super.onStartCommand(intent, flags, startId);
    }

    private class ServiceThread extends Thread {

        private boolean alive = true;

        @Override
        public void run() {
            int debug = 0;
            while (alive) {
                Log.d(MainActivity.LOG_KEY, "running service update");
                controller.updateApplicationAndWaitingTime();
                try {
                    Log.d(MainActivity.LOG_KEY, "sleeping service thread");
                    Thread.sleep(30000);
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
            Log.d(MainActivity.LOG_KEY, "killing service thread");
            alive = false;
        }
    }
}
