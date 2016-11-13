package com.celdev.migstat;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.celdev.migstat.model.WaitingTime;
import com.celdev.migstat.view.BackgroundChanger;
import com.celdev.migstat.view.CustomAboutDialog;
import com.celdev.migstat.view.CustomNewWaitingTimeDialog;
import com.celdev.migstat.view.ViewUpdateReceiver;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;

public class ShowStatus extends AppCompatActivity {

    private Application application;
    private ProgressBar progressBar;
    private ImageButton refreshButton;
    private TextView applicationDateTextView, progressBarText, estimatedMonthsText, applicationStatusText;
    private TextView daysSinceApplicationText, daysToDecisionText;
    private FrameLayout progressBarFrame;

    private LinearLayout changeBgView;
    private ProgressBarUpdaterThread progressBarUpdaterThread;

    private RelativeLayout root;

    private boolean fromOnCreate = false;

    private long timeWhenLastUpdatedStatus = 0;
    private long timeWhenLastUpdatedWaitingTime = 0;

    private static final long UPDATE_MS_TRESHHOLD = 1800000; //30 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(MainActivity.LOG_KEY, "onCreate called");
        fromOnCreate = true;
        setContentView(R.layout.activity_show_status);
        initViews();
        changeBackground();
        try {
            loadApplication();
        } catch (NoApplicationException | NoWaitingTimeException e) {
            e.printStackTrace();
            returnToMainActivityBecauseError(R.string.alpha_incorrect_state,true);
        }
        initButtonFunctionality();
        removeRefreshButtonIfNoApplicationNumberAndNoWaitingTimeQuery();
    }

    /*  If the application doesn't have an application number
    *   and doesn't have a "application type" query (created in the webview)
    *   updating the waiting time and status does nothing
    * */
    private void removeRefreshButtonIfNoApplicationNumberAndNoWaitingTimeQuery() {
        if (!application.isHasApplicationNumber() &&
                application.getWaitingTime() != null &&
                application.getWaitingTime().getQuery().isEmpty()) {
            progressBarFrame.removeView(refreshButton);
        }
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
        ani.setDuration(500);
        ani.setRepeatCount(Animation.INFINITE);
        ani.setRepeatMode(Animation.RESTART);
        refreshButton.startAnimation(ani);
        refreshButton.setEnabled(false);
        new ApplicationStatusChecker(refreshStatusReceiver).checkApplication(application);
    }

    private void initViews() {
        root = (RelativeLayout) findViewById(R.id.activity_show_status);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        refreshButton = (ImageButton) findViewById(R.id.progress_reload_button);
        applicationDateTextView = (TextView) findViewById(R.id.application_date_number);
        progressBarText = (TextView) findViewById(R.id.progress_bar_text);
        estimatedMonthsText = (TextView) findViewById(R.id.estimated_months_number);
        progressBarFrame = (FrameLayout) findViewById(R.id.progress_bar_frame);
        applicationStatusText = (TextView) findViewById(R.id.application_status_text);
        changeBgView = (LinearLayout)findViewById(R.id.change_bg_view);
        daysSinceApplicationText = (TextView) findViewById(R.id.days_since_application_number);
        daysToDecisionText = (TextView) findViewById(R.id.avg_days_to_decision_number);
    }

    private ViewUpdateReceiver refreshStatusReceiver = new ViewUpdateReceiver() {
        @Override
        public void receiveUpdate(Object object) {
            disableRefreshAnimation();
            if (object == null) {
                Toast.makeText(ShowStatus.this, R.string.error_reload, Toast.LENGTH_LONG).show();
            }
            if (object instanceof SimpleCaseStatusParser.StatusAndDate) {
                timeWhenLastUpdatedStatus = System.currentTimeMillis();
                SimpleCaseStatusParser.StatusAndDate statusAndDate = (SimpleCaseStatusParser.StatusAndDate) object;
                Log.d(MainActivity.LOG_KEY, "received status update" + statusAndDate.toString());
                Toast.makeText(ShowStatus.this, R.string.refreshed_status, Toast.LENGTH_SHORT).show();
                int index = statusAndDate.getStatusType().getNumber();
                if (index >= 0 && index < 3) {
                    applicationStatusText.setText(getResources().getStringArray(R.array.case_status_types)[index]);
                } else {
                    applicationStatusText.setText("");
                }
            }
        }
    };

    private void loadApplication() throws NoApplicationException, NoWaitingTimeException {
        application = DataStorage.getInstance().loadApplication(this);
        setApplicationViewInformation();
        WaitingTimeDataStoragePacket waitingTimeDataStoragePacket = DataStorage.getInstance().loadWaitingTime(this);
        if (waitingTimeDataStoragePacket.isCustomMode()) {
            application.setWaitingTimeReturnBothIfNewer(
                    new WaitingTime(waitingTimeDataStoragePacket.getCustomMonths(),
                            waitingTimeDataStoragePacket.getCustomMonths(),
                            DateUtils.msToDateString(System.currentTimeMillis()), ""));
        } else {
            new WebViewResponseParser(waitingTimeDataStoragePacket.getQuery(), waitingTimeReceiver);
        }
    }

    private void setApplicationViewInformation() {
        applicationDateTextView.setText(DateUtils.msToDateString(application.getApplicationDate()));
        daysSinceApplicationText.setText("" + DateUtils.daysWaited(application.getApplicationDate()));
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
            if (object instanceof WaitingTime) {
                timeWhenLastUpdatedWaitingTime = System.currentTimeMillis();
                WaitingTime waitingTime = (WaitingTime) object;
                handleCheckOldNewWaitingTime(application.setWaitingTimeReturnBothIfNewer(waitingTime));
                estimatedMonthsText.setText(waitingTime.lowMonthAndHighMonthIsEqual()?
                        getString(R.string.estimated_months_placeholder_single_month,waitingTime.getAverage()) :
                        getString(R.string.estimated_months_placeholder, waitingTime.getLowMonth(), waitingTime.getHighMonth()));
                refreshProgressThread();
                daysToDecisionText.setText("" + DateUtils.daysUntilDecision(
                        DateUtils.addAverageMonthsToDate(
                                application.getApplicationDate(),
                                waitingTime.getAverage())));
            }
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

    /*  This method is called before the application enters the onPaused state
    *   kills the progressupdater thread (which updates the progressbar and progress text)
    *   so that it doesn't steal resources when the application isn't visible
    * */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(MainActivity.LOG_KEY, "onPause called");
        if (progressBarUpdaterThread != null) {
            progressBarUpdaterThread.kill();
            progressBarUpdaterThread = null;
        }
        fromOnCreate = false;
    }

    /*  This method is called after the Activity is created (onCreate) or when
    *   the application returns from the onPaused-state or goes from the onStart-state
    *   https://developer.android.com/images/activity_lifecycle.png
    *
    *   if this method is called from (after) the onCreate-state we shouldn't do anything
    *   since the onCreate-method will take care of the initializing of the application
    *
    *   if the method is called when returning from the onPaused state or from (after)
    *   the onStart-state the progress thread should be updated (after the application
    *   and waiting time has been fetched from the DataStorage if needed)
    *
    *   if there's something wrong with receiving the application or waitingtime from the
    *   DataStorage an exception will be trown and the application will be reset
    * */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(MainActivity.LOG_KEY, "onResume called");
        if (fromOnCreate) {
            fromOnCreate = false;
            return;
        }
        if (application != null && application.getWaitingTime() != null) {
            refreshProgressThread();
        } else {
            try {
                loadApplication();
                refreshProgressThread();
            } catch (NoApplicationException | NoWaitingTimeException e) {
                Log.d(MainActivity.LOG_KEY, "Incorrect state, application or waiting time is null");
                returnToMainActivityBecauseError(R.string.alpha_incorrect_state, true);
            }
        }
    }

    /*  Returns the user to the MainActivity and resets all stored data if shouldResetAllData is true
    *   Shows a dialog with a message specified by the resource int provided as a parameter
    * */
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

    /*  This thread will handle the updating of the progressbar
    *   and the progress text percent.
    * */
    private class ProgressBarUpdaterThread extends Thread {

        private boolean alive = true;

        private long startDateMS, estimatedEndDateMS;

        private ProgressBarUpdaterThread(long startDateMS, double averageMonths) {
            this.startDateMS = startDateMS;
            this.estimatedEndDateMS = DateUtils.addAverageMonthsToDate(startDateMS, averageMonths);
            start();
        }

        @Override
        public void run() {

            final DecimalFormat decimalFormat = new DecimalFormat("0.######", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("se")));
            decimalFormat.setMinimumFractionDigits(6);
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
    protected void onDestroy() {
        super.onDestroy();
        if (application != null) {
            DataStorage.getInstance().saveApplication(this, application);
            if (application.getWaitingTime() != null) {
                DataStorage.getInstance().saveWaitingTimeDataStoragePacket(this, application.getWaitingTime());
            }
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

    private class UpdateStatusAndWaitingTimeReceiver implements ViewUpdateReceiver, AsyncTaskResultReceiver {

        private ViewUpdateReceiver viewUpdateReceiver;
        private AsyncTaskResultReceiver asyncTaskResultReceiver;
        private boolean receivedWaitingTime = false, receivedStatus = false;

        private Object waitingTimeResult, statusResult;

        public UpdateStatusAndWaitingTimeReceiver(ViewUpdateReceiver viewUpdateReceiver, AsyncTaskResultReceiver asyncTaskResultReceiver) {
            this.viewUpdateReceiver = viewUpdateReceiver;
            this.asyncTaskResultReceiver = asyncTaskResultReceiver;
        }

        private synchronized void pushToOuterClassIfReceivedBoth() {
            if (receivedWaitingTime && receivedStatus) {
                viewUpdateReceiver.receiveUpdate(statusResult);
                asyncTaskResultReceiver.receiveResult(waitingTimeResult);
            }
        }

        private synchronized void setReceived(boolean setReceivedStatus) {
            if (setReceivedStatus) {
                receivedStatus = true;
            } else {
                receivedWaitingTime = true;
            }
        }

        @Override
        public void receiveResult(Object object) {
            //waitingtime
            setReceived(false);
            waitingTimeResult = object;
        }

        @Override
        public void receiveUpdate(Object object) {
            //status
            setReceived(true);
            statusResult = object;
        }

        public void check() {
            new ApplicationStatusChecker(this).checkApplication(application);
            new WebViewResponseParser(application.getWaitingTime().getQuery(), this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        try {
            menuInflater.inflate(R.menu.show_menu, menu);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                showAboutDialog();
                return true;
            case R.id.menu_unlock_theme:
                return true;
            case R.id.menu_change_bg:
                setChangeBackgroundMode();
                return true;
            case R.id.menu_reset_application:
                DataStorage.getInstance().deleteAllData(this);
                startActivity(new Intent(this, MainActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setChangeBackgroundMode() {
        new BackgroundChanger(root, changeBgView, this);
    }

    private void showAboutDialog() {
        new CustomAboutDialog(this).createAndShow();
    }

    public void removeSetBG() {
        changeBgView.removeAllViewsInLayout();
    }

    private void changeBackground() {
        int backgroundIndex = DataStorage.getInstance().getBackgroundIndex(this);
        int[] backgrounds = BackgroundChanger.drawables;
        if (backgroundIndex > 0 && backgroundIndex < backgrounds.length) {
            root.setBackground(getDrawable(backgrounds[backgroundIndex]));
        }
    }
}
