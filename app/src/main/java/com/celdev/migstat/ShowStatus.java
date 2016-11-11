package com.celdev.migstat;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.celdev.migstat.controller.ApplicationStatusChecker;
import com.celdev.migstat.controller.DataStorage;
import com.celdev.migstat.controller.NoApplicationException;
import com.celdev.migstat.controller.NoWaitingTimeException;
import com.celdev.migstat.controller.WaitingTimeDataStoragePacket;
import com.celdev.migstat.controller.WebViewResponseParser;
import com.celdev.migstat.controller.parser.AsyncTaskResultReceiver;
import com.celdev.migstat.controller.parser.SimpleCaseStatusParser;
import com.celdev.migstat.controller.utils.DateUtils;
import com.celdev.migstat.model.Application;
import com.celdev.migstat.model.ParserException;
import com.celdev.migstat.model.WaitingTime;
import com.celdev.migstat.view.CustomNewWaitingTimeDialog;
import com.celdev.migstat.view.ViewUpdateReceiver;

import java.text.DecimalFormat;

public class ShowStatus extends AppCompatActivity {

    private static final long ROTATE_ANIMATION_TIME_MS = 1000L;

    private Application application;
    private ProgressBar progressBar;
    private ImageButton refreshButton;
    private TextView applicationDateTextView, progressBarText;

    private ProgressBarUpdaterThread progressBarUpdaterThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_status);
        initViews();
        try {
            loadApplication();
        } catch (NoApplicationException | NoWaitingTimeException e) {
            returnToMainActivityBecauseError(R.string.alpha_incorrect_state,true);
        }
        initButtonFunctionality();
    }

    private void initButtonFunctionality() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateRefreshButton();
            }
        });
    }

    private void rotateRefreshButton() {
        final Animation ani = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ani.setDuration(1000);
        ani.setRepeatCount(Animation.INFINITE);
        ani.setRepeatMode(Animation.RESTART);
        refreshButton.startAnimation(ani);
        refreshButton.setEnabled(false);
        new ApplicationStatusChecker(refreshStatusReceiver).checkApplication(application);
    }

    private void initViews() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        refreshButton = (ImageButton) findViewById(R.id.progress_reload_button);
        applicationDateTextView = (TextView) findViewById(R.id.application_date_number);
        progressBarText = (TextView) findViewById(R.id.progress_bar_text);
    }

    private ViewUpdateReceiver refreshStatusReceiver = new ViewUpdateReceiver() {
        @Override
        public void receiveUpdate(Object object) {
            disableRefreshAnimation();
            if (object == null) {
                Toast.makeText(ShowStatus.this, R.string.error_reload, Toast.LENGTH_LONG).show();
            }
            if (object instanceof SimpleCaseStatusParser.StatusAndDate) {
                SimpleCaseStatusParser.StatusAndDate statusAndDate = (SimpleCaseStatusParser.StatusAndDate) object;
                Log.d(MainActivity.LOG_KEY, "received status update" + statusAndDate.toString());
                Toast.makeText(ShowStatus.this, R.string.refreshed_status, Toast.LENGTH_SHORT).show();
            }
        }
    };




    private void loadApplication() throws NoApplicationException, NoWaitingTimeException {
        application = DataStorage.getInstance().loadApplication(this);
        setApplicationViewInformation();
        WaitingTimeDataStoragePacket waitingTimeDataStoragePacket = DataStorage.getInstance().loadWaitingTime(this);
        try {
            new WebViewResponseParser(waitingTimeDataStoragePacket.getQuery(), waitingTimeReceiver);
        } catch (ParserException e) {
            e.printStackTrace();
        }
    }

    private void setApplicationViewInformation() {
        applicationDateTextView.setText(DateUtils.msToDateString(application.getApplicationDate()));
    }

    private void disableRefreshAnimation() {
        final Animation animation = refreshButton.getAnimation();
        if (animation != null) {
            animation.cancel();
        }
        refreshButton.setEnabled(true);
    }


    private AsyncTaskResultReceiver waitingTimeReceiver = new AsyncTaskResultReceiver() {
        @Override
        public void receiveResult(Object object) {
            System.out.println("result: " + object);
            if (object instanceof WaitingTime) {
                Log.d(MainActivity.LOG_KEY, "Setting application waitingtime");
                handleCheckOldNewWaitingTime(application.setWaitingTimeReturnBothIfNewer((WaitingTime) object));
                refreshProgressThread();
            }
            Log.d(MainActivity.LOG_KEY, "Application waiting time is: " + application.getWaitingTime().toString());
        }
    };

    private void handleCheckOldNewWaitingTime(Application.OldAndNewWaitingTimeWrapper oldNew) {
        if (oldNew != null) {
            new CustomNewWaitingTimeDialog(this, oldNew).create().show();
        }
    }

    private void refreshProgressThread() {
        if (progressBarUpdaterThread != null) {
            progressBarUpdaterThread.kill();
            progressBarUpdaterThread = null;
        }
        progressBarUpdaterThread = new ProgressBarUpdaterThread(application.getApplicationDate(),application.getWaitingTime().getAverage());
    }



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


    private class ProgressBarUpdaterThread extends Thread {

        private boolean alive = true;

        private long startDateMS, estimatedEndDateMS;

        private ProgressBarUpdaterThread(long startDateMS, double averageMonths) {
            this.startDateMS = startDateMS;
            this.estimatedEndDateMS = DateUtils.addAverageMonthsToStartDate(startDateMS, averageMonths);
            start();
        }

        @Override
        public void run() {

            final DecimalFormat decimalFormat = new DecimalFormat("0.########");
            while (alive) {
                final double percent = calculateProgress();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBarText.setText(decimalFormat.format(percent * 100) + "%");
                        progressBar.setProgress((int)(percent*10000));
                    }
                });
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    kill();
                }
            }
        }

        private double calculateProgress() {
            long now = System.currentTimeMillis();
            long waited = now - startDateMS;
            long toWait = estimatedEndDateMS - startDateMS;
            return (double)waited / toWait;
        }

        private void kill() {
            alive = false;
        }
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
