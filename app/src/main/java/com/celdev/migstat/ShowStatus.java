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

import com.celdev.migstat.background.ServiceRunner;
import com.celdev.migstat.controller.Controller;
import com.celdev.migstat.controller.DataStorageLoadException;
import com.celdev.migstat.controller.utils.DateUtils;
import com.celdev.migstat.model.Application;
import com.celdev.migstat.model.NoApplicationNumberException;
import com.celdev.migstat.model.WaitingTime;
import com.celdev.migstat.view.BackgroundChanger;
import com.celdev.migstat.view.CustomAboutDialog;
import com.celdev.migstat.view.CustomGotDecisionDialog;
import com.celdev.migstat.view.CustomInterstitialAd;
import com.celdev.migstat.view.CustomNewWaitingTimeDialog;
import com.celdev.migstat.view.CustomSetWaitingTimeDialog;
import com.celdev.migstat.view.NumberPickerDialogReturn;
import com.celdev.migstat.view.ViewInterface;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ShowStatus extends AppCompatActivity implements ViewInterface {

    private Application application;
    private ProgressBar progressBar;
    private ImageButton refreshButton;
    private TextView applicationDateTextView, progressBarText, estimatedMonthsText, applicationStatusText;
    private TextView daysSinceApplicationText, daysToDecisionText;
    private FrameLayout progressBarFrame;

    private LinearLayout changeBgView;
    private ProgressBarUpdaterThread progressBarUpdaterThread;

    private RelativeLayout root;

    private CustomInterstitialAd customInterstitialAd;

    private boolean fromOnCreate = false;


    private Controller controller = new Controller(this, this);

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
        } catch (DataStorageLoadException e) {
            e.printStackTrace();
            returnToMainActivityBecauseError(R.string.alpha_incorrect_state,true);
        }
        initButtonFunctionality();
        startService(new Intent(this, ServiceRunner.class));
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(MainActivity.LOG_KEY, "onPrepareOptionsMenu called");
        if (controller.themesIsEnabled()) {
            Log.d(MainActivity.LOG_KEY, "Themes are unlocked");
            menu.findItem(R.id.menu_change_bg).setEnabled(true);
            menu.findItem(R.id.menu_unlock_theme).setTitle(R.string.support_watch_ad);
        }
        if (controller.waitingTimeIsLoaded()) {
            if (controller.isUsingCustomWaitingTime()) {
                menu.findItem(R.id.menu_use_custom_waiting_time).setVisible(false);
            } else {
                menu.findItem(R.id.menu_use_migrationverket_waiting_time).setVisible(false);
            }
            if (!controller.hasWaitingTimeQuery()) {
                menu.findItem(R.id.menu_use_migrationverket_waiting_time).setVisible(false);
            }
            if (!controller.hasCustomWaitingTime()) {
                menu.findItem(R.id.menu_use_custom_waiting_time).setVisible(false);
            }
        }
        if (customInterstitialAd == null) {
            customInterstitialAd = new CustomInterstitialAd(controller, this);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void modelChange(ModelChange modelChange) {
        disableRefreshAnimation();
        switch (modelChange) {
            case APPLICATION:
                processUpdateApplication();
                break;
            case WAITING_TIME:
                processUpdateWaitingTime();
                break;
            case ERROR_UPDATE:
                /* returned when something went wrong with parsing, shouldn't happen since
                 * since the application should return to the MainActivity if the information
                 * is incorrect.
                 * Could happen if Migrationsverket alter their website
                 */
                break;
            case NEW_WAITING_TIME:
                //returned when the new waiting time have a newer "updated at" then before
                processUpdateWaitingTime();
                handleCheckOldNewWaitingTime();
                break;
            case FINISHED:
                processUpdateApplication();
                processGotDecision();
                break;
            default:
                processUpdateApplication();
                processUpdateWaitingTime();
                Log.d(MainActivity.LOG_KEY, "got other model change enum" + modelChange.name());
        }
    }

    private void processGotDecision() {
        new CustomGotDecisionDialog(this).create().show();

    }

    private void processUpdateApplication() {
        Log.d(MainActivity.LOG_KEY, "updating progress from application progressUpdateApplication");
        try {
            if (application == null) {
                application = controller.getApplication();
            }
            setApplicationViewInformation();
            if (application.isHasApplicationNumber()) {
                try {
                    applicationStatusText.setText(getResources().getStringArray(R.array.case_status_types)[application.getApplicationStatus().getStatusType().getNumber()]);
                } catch (NoApplicationNumberException e) {
                    e.printStackTrace();
                }
            } else {
                applicationStatusText.setText("");
            }
        } catch (DataStorageLoadException e) {
            e.printStackTrace();
        }
    }

    private void processUpdateWaitingTime() {

        try {
            WaitingTime waitingTime = controller.getWaitingTime();
            estimatedMonthsText.setText(waitingTime.lowMonthAndHighMonthIsEqual()?
                    getString(R.string.estimated_months_placeholder_single_month,(int)waitingTime.getAverage()) :
                    getString(R.string.estimated_months_placeholder, waitingTime.getLowMonth(), waitingTime.getHighMonth()));
            refreshProgressThread();
            daysToDecisionText.setText(getString(R.string.integer_placeholder, DateUtils.daysUntilDecision(
                    DateUtils.addAverageMonthsToDate(
                            application.getApplicationDate(),
                            waitingTime.getAverage()))));
            invalidateOptionsMenu();
        } catch (DataStorageLoadException e) {
            e.printStackTrace();
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
        controller.updateApplicationAndWaitingTime();
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

    private void loadApplication() throws DataStorageLoadException {
        controller.loadAll();
        application = controller.getApplication();
        controller.updateWaitingTime();
        controller.updateApplication();
    }

    private void setApplicationViewInformation() {
        Log.d(MainActivity.LOG_KEY, "updating progress from application");
        applicationDateTextView.setText(DateUtils.msToDateString(application.getApplicationDate()));
        daysSinceApplicationText.setText(getString(R.string.integer_placeholder, DateUtils.daysWaited(application.getApplicationDate())));
    }

    private void disableRefreshAnimation() {
        final Animation animation = refreshButton.getAnimation();
        if (animation != null) {
            animation.cancel();
        }
        refreshButton.setEnabled(true);
    }



    private void handleCheckOldNewWaitingTime() {
        new CustomNewWaitingTimeDialog(this).create().show();
    }

    private void refreshProgressThread() {
        if (progressBarUpdaterThread != null) {
            progressBarUpdaterThread.kill();
            progressBarUpdaterThread = null;
        }
        try {
            WaitingTime waitingTime = controller.getWaitingTime();
            progressBarUpdaterThread = new ProgressBarUpdaterThread(application.getApplicationDate(),waitingTime.getAverage());
        } catch (DataStorageLoadException e) {
            e.printStackTrace();
        }
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
        controller = new Controller(this, this);
        if (application != null && application.getWaitingTime() != null) {
            refreshProgressThread();
        } else {
            try {
                loadApplication();
                refreshProgressThread();
            } catch (DataStorageLoadException e) {
                e.printStackTrace();
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
            controller.deleteAll();
        }
        new AlertDialog.Builder(this).setMessage(message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
        startActivity(new Intent(ShowStatus.this, MainActivity.class));
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
                        progressBarText.setText(getString(R.string.percent_placeholder, decimalFormat.format(percent * 100)));
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
        controller.saveAll();
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
            case R.id.menu_set_case_type:
                Intent intent = new Intent(this, ApplicationTypeWebViewActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_set_custom_waiting_time:
                new CustomSetWaitingTimeDialog(this, new NumberPickerDialogReturn() {
                    @Override
                    public void returnOnOk(int months) {
                        controller.setAndUseCustomWaitingTime(months);
                    }
                }).createAndShow();
                processUpdateWaitingTime();
                refreshButton.performClick();
                return true;
            case R.id.menu_use_custom_waiting_time:
                controller.setWaitingTimeMode(true);
                processUpdateWaitingTime();
                refreshButton.performClick();
                return true;
            case R.id.menu_use_migrationverket_waiting_time:
                controller.setWaitingTimeMode(false);
                processUpdateWaitingTime();
                refreshButton.performClick();
                return true;
            case R.id.menu_unlock_theme:
                customInterstitialAd.show();
                return true;
            case R.id.menu_change_bg:
                setChangeBackgroundMode();
                return true;
            case R.id.menu_reset_application:
                controller.deleteAll();
                startActivity(new Intent(this, MainActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void setChangeBackgroundMode() {
        new BackgroundChanger(controller,root, changeBgView, this);
    }

    private void showAboutDialog() {
        new CustomAboutDialog(this).createAndShow();
    }

    public void removeSetBG() {
        changeBgView.removeAllViewsInLayout();
    }

    private void changeBackground() {
        int backgroundIndex = controller.loadBackground();
        int[] backgrounds = BackgroundChanger.drawables;
        if (backgroundIndex > 0 && backgroundIndex < backgrounds.length) {
            root.setBackground(getDrawable(backgrounds[backgroundIndex]));
        }
    }
}
