package com.celdev.migstat;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.celdev.migstat.controller.DataStorage;
import com.celdev.migstat.controller.NoApplicationException;
import com.celdev.migstat.controller.NoWaitingTimeException;
import com.celdev.migstat.controller.WaitingTimeDataStoragePacket;
import com.celdev.migstat.controller.WebViewResponseParser;
import com.celdev.migstat.controller.parser.AsyncTaskResultReceiver;
import com.celdev.migstat.controller.parser.WaitingTimeParser;
import com.celdev.migstat.model.Application;
import com.celdev.migstat.model.ParserException;
import com.celdev.migstat.model.WaitingTime;

import java.util.Observable;
import java.util.Observer;

public class ShowStatus extends AppCompatActivity {

    private Application application;
    private WaitingTimeDataStoragePacket waitingTimeDataStoragePacket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_status);
        try {
            loadApplication();
            ((TextView) findViewById(R.id.avg_months)).setText(application.toString());
        } catch (NoApplicationException | NoWaitingTimeException e) {
            e.printStackTrace();
            returnToMainActivityBecauseError(R.string.alpha_incorrect_state,true);
        }
    }

    private void loadApplication() throws NoApplicationException, NoWaitingTimeException {
        application = DataStorage.getInstance().loadApplication(this);
        waitingTimeDataStoragePacket = DataStorage.getInstance().loadWaitingTime(this);
        try {
            new WebViewResponseParser(waitingTimeDataStoragePacket.getQuery(), waitingTimeReceiver);
        } catch (ParserException e) {
            e.printStackTrace();
        }
    }


    private AsyncTaskResultReceiver waitingTimeReceiver = new AsyncTaskResultReceiver() {
        @Override
        public void receiveResult(Object object) {
            System.out.println("result: " + object);
            if (object instanceof WaitingTime) {
                Log.d(MainActivity.LOG_KEY, "Setting application waitingtime");
                application.setWaitingTimeReturnIsNewer((WaitingTime) object);
            }
            Log.d(MainActivity.LOG_KEY, "Application waiting time is: " + application.getWaitingTime().toString());
            ((TextView) findViewById(R.id.avg_months)).setText(application.toString());
        }
    };

    private void returnToMainActivityBecauseError(@StringRes int message, boolean shouldResetAllData) {
        if (shouldResetAllData) {
            DataStorage.getInstance().deleteAllData(this);
        }
        new AlertDialog.Builder(this).setMessage(message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(ShowStatus.this, MainActivity.class));
            }
        }).create().show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN).
                    addCategory(Intent.CATEGORY_HOME).
                    setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }
}
