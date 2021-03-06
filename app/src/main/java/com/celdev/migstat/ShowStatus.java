package com.celdev.migstat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.celdev.migstat.background.ServiceRunner;
import com.celdev.migstat.controller.Controller;
import com.celdev.migstat.controller.DataStorageLoadException;
import com.celdev.migstat.controller.IncorrectStateException;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/*  This class contains the Activity that shows the information and status about
*   an application with the swedish migration agency.
*
*   it will show:
*       * when the application was made
*       * average waiting time (custom or migrationsverkets estimate)
*       * days waited
*       * days until decision (average)
*       * progress (in percent)
*     if using application number it will also show
*       * application status
*
*  it is also possible for the user to customize this Activity in terms of changing the
*  background-picture.
* */
public class ShowStatus extends AppCompatActivity implements ViewInterface {

    private Application application;
    private ProgressBar progressBar;
    private ImageButton refreshButton;
    private TextView applicationDateTextView, progressBarText, estimatedMonthsText, applicationStatusText;
    private TextView daysSinceApplicationText, daysToDecisionText;

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

        //used so that we can determind if onResume was called directly after this method
        fromOnCreate = true;

        //sets the layout to be used in this activity
        setContentView(R.layout.activity_show_status);

        //fetches references to the views in the layout
        initViews();
        //sets the correct background
        changeBackground();
        try {
            //try to load the application information from the DataStorage (through the controller)
            loadApplication();
        } catch (DataStorageLoadException | IncorrectStateException e) {
            //an exception will be thrown if there's a problem in loading the application information
            //the user will then be forced back to the MainActivity and have to provide the
            //correct information (again)
            e.printStackTrace();
            returnToMainActivityBecauseError(R.string.alpha_incorrect_state,true);
        }

        //initializes the button click functionality
        initButtonFunctionality();

        //starts the background "check application status"-service
        startService(new Intent(this, ServiceRunner.class));

        //initializes the ads
        initAd();
    }


    /*  Makes sure the ActionBar menu's items are in the correct state
    *   Depending on the state of the WaitingTime-object and
    *
    *   if the change background-item should be unlocked.
    * */
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
        } else {
            Log.d(MainActivity.LOG_KEY, "Waiting time is not loaded");
        }

        return super.onPrepareOptionsMenu(menu);
    }

    /*  This method is called when the controller finishes an Async operation
    *   different operations will send different ModelChange-objects to this method
    * */
    @Override
    public void modelChange(ModelChange modelChange) {
        disableRefreshAnimation();
        switch (modelChange) {
            case APPLICATION:
                processUpdateApplication();
                break;
            case WAITING_TIME:
                processUpdateWaitingTime();
                invalidateOptionsMenu();
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
                invalidateOptionsMenu();
                Log.d(MainActivity.LOG_KEY, "got other model change enum" + modelChange.name());
        }
    }

    /*  creates and shows a dialog containing the information
    *   that the application number used have gotten a decision
    * */
    private void processGotDecision() {
        new CustomGotDecisionDialog(this).create().show();
    }

    /*  Sets the application status text
    * */
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
                findViewById(R.id.application_status_box).setVisibility(View.INVISIBLE);
                applicationStatusText.setText("");
            }
        } catch (IncorrectStateException e) {
            e.printStackTrace();
        }
    }

    /*  sets the text of the waiting time GUI elements
    *   to the information contained in the WaitingTime-object
    * */
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
        } catch (IncorrectStateException e) {
            e.printStackTrace();
            returnToMainActivityBecauseError(R.string.alpha_incorrect_state, true);
        }
    }

    /*  sets the functionality of the refresh button
    * */
    private void initButtonFunctionality() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateRefreshButton();
            }
        });
    }

    /*  rotates the refresh button when it's pressed
    *   and calls the controller to update the application status and waiting time
    * */
    private void rotateRefreshButton() {
        final Animation ani = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ani.setDuration(500);
        ani.setRepeatCount(Animation.INFINITE);
        ani.setRepeatMode(Animation.RESTART);
        refreshButton.startAnimation(ani);
        refreshButton.setEnabled(false);
        controller.updateApplicationAndWaitingTime();
    }

    /*  Fetches the view elements
    * */
    private void initViews() {
        root = (RelativeLayout) findViewById(R.id.activity_show_status);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        refreshButton = (ImageButton) findViewById(R.id.progress_reload_button);
        applicationDateTextView = (TextView) findViewById(R.id.application_date_number);
        progressBarText = (TextView) findViewById(R.id.progress_bar_text);
        estimatedMonthsText = (TextView) findViewById(R.id.estimated_months_number);
        applicationStatusText = (TextView) findViewById(R.id.application_status_text);
        changeBgView = (LinearLayout)findViewById(R.id.change_bg_view);
        daysSinceApplicationText = (TextView) findViewById(R.id.days_since_application_number);
        daysToDecisionText = (TextView) findViewById(R.id.avg_days_to_decision_number);
    }

    /*  Loads the application and forces an update of the application status and
    *   the waiting time.
    *
    *   throws an error if the application or waiting time isn't in a correct state
    *   (which should mean that the user shouldn't be in this activity)
    * */
    private void loadApplication() throws DataStorageLoadException, IncorrectStateException {
        controller.loadAll();
        application = controller.getApplication();
        controller.updateWaitingTime();
        controller.updateApplication();
    }

    /*  sets the text of some of the application GUI elements to the
    *   information contained in the Application-object
    * */
    private void setApplicationViewInformation() {
        Log.d(MainActivity.LOG_KEY, "updating progress from application");
        applicationDateTextView.setText(DateUtils.msToDateString(application.getApplicationDate()));
        daysSinceApplicationText.setText(getString(R.string.integer_placeholder, DateUtils.daysWaited(application.getApplicationDate())));
    }

    /*  Disables the animation of the refresh button
    * */
    private void disableRefreshAnimation() {
        final Animation animation = refreshButton.getAnimation();
        if (animation != null) {
            animation.cancel();
        }
        refreshButton.setEnabled(true);
    }


    /*  shows a dialog with the information that the average waiting time
    *   from migrationsverket has been updated
    * */
    private void handleCheckOldNewWaitingTime() {
        new CustomNewWaitingTimeDialog(this).create().show();
    }

    /*  kills the progressbar updating thread and starts a new one
    * */
    private void refreshProgressThread() {
        if (progressBarUpdaterThread != null) {
            progressBarUpdaterThread.kill();
            progressBarUpdaterThread = null;
        }
        try {
            WaitingTime waitingTime = controller.getWaitingTime();
            progressBarUpdaterThread = new ProgressBarUpdaterThread(application.getApplicationDate(),waitingTime.getAverage());
        } catch (IncorrectStateException e) {
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
            } catch (IncorrectStateException | DataStorageLoadException e) {
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
        stopService(new Intent(this, ServiceRunner.class));
        startActivity(new Intent(ShowStatus.this, MainActivity.class));
    }

    /*  This thread will handle the updating of the progressbar
    *   and the progress text percent.
    * */
    private class ProgressBarUpdaterThread extends Thread {

        private boolean alive = true;

        private long startDateMS, estimatedEndDateMS;

        /*  passes the information needed to calculate the progress and
        *   starts the thread
        * */
        private ProgressBarUpdaterThread(long startDateMS, double averageMonths) {
            this.startDateMS = startDateMS;
            this.estimatedEndDateMS = DateUtils.addAverageMonthsToDate(startDateMS, averageMonths);
            start();
        }

        /*  updates the progressbar and progressbar text every 250ms
        * */
        @Override
        public void run() {
            final DecimalFormat decimalFormat = new DecimalFormat("0.######", DecimalFormatSymbols.getInstance(Locale.getDefault()));
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
                    e.printStackTrace();
                    kill();
                }
            }
        }

        /*  calculates he progress (in percent)
        *   by dividing the time waited with the estimated
        *   average waiting time
        * */
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

    /*  this method is called when this activity is shut down
    *   calls for the controller to save all data
    * */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.saveAll();
    }


    /*  This method is called when the user presses an hardware button
    *   overrides the back-key functionality so that the app is
    *   moved to the background instead of taking the user to the
    *   activity before this.
    * */
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


    /*  This method is called when the ActionBar menu is to be created.
    *   creates the menu using the show_menu.xml file in the menu resource folder.
    * */
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

    /*  This method is called when a menu item is clicked */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:               //shows the about dialog
                showAboutDialog();
                return true;
            case R.id.menu_set_case_type:       //starts the webview-activity for getting
                                                //migrationsverket average time to decision
                Intent intent = new Intent(this, ApplicationTypeWebViewActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_set_custom_waiting_time:     //sets the custom waiting time
                new CustomSetWaitingTimeDialog(this, new NumberPickerDialogReturn() {
                    @Override
                    public void returnOnOk(int months) {
                        controller.setAndUseCustomWaitingTime(months);
                        refreshAfterWaitingTimeChange();
                    }
                }).createAndShow();
                return true;
            case R.id.menu_use_custom_waiting_time:     //sets the waiting time mode to
                controller.setWaitingTimeMode(true);    //custom waiting time
                refreshAfterWaitingTimeChange();
                return true;
            case R.id.menu_use_migrationverket_waiting_time:    //sets the waiting time mode to
                controller.setWaitingTimeMode(false);           //migrationsverket waiting time
                processUpdateWaitingTime();
                return true;
            case R.id.menu_unlock_theme:                        //shows an ad and unlocks the
                Log.d(MainActivity.LOG_KEY, "Loading ad");      //change background functionality
                customInterstitialAd.show();
                return true;
            case R.id.menu_change_bg:                           //starts the change background mode
                setChangeBackgroundMode();
                return true;
            case R.id.menu_reset_application:                   //resets the application and starts
                controller.deleteAll();                         //the MainActivity
                stopService(new Intent(this, ServiceRunner.class));
                startActivity(new Intent(this, MainActivity.class));
                return true;
            case R.id.menu_privacy:
                MainActivity.showPrivacyDocument(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*  Initializes the banner ad and interstitial ad
    *   checks if the device screen can fit a AdSize.Banner sized ad
    *   if not it sets the ad size to little bit less than the available screen width
    * */
    private void initAd() {
        LinearLayout linearAdView = (LinearLayout) findViewById(R.id.adView);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = (displayMetrics.widthPixels / displayMetrics.density) - 36;
        Log.d(MainActivity.LOG_KEY, "dp width is" + dpWidth);
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-1707701136705466~8632139435");
        AdView adView = new AdView(this);
        if (dpWidth < 320) {
            adView.setAdSize(new AdSize((int) dpWidth, 50));
        } else {
            adView.setAdSize(AdSize.BANNER);
        }
        adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
        linearAdView.addView(adView);
        if (customInterstitialAd == null) {
            customInterstitialAd = new CustomInterstitialAd(controller, this);
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }


    /*  Updates the GUI (including the ActionBar menu)
    *   after the waiting time have change
    *   for example if the user change to custom months or
    *   change to use migrationsverket average
    * */
    private void refreshAfterWaitingTimeChange() {
        processUpdateWaitingTime();
        invalidateOptionsMenu();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            refreshButton.callOnClick();
        } else {
            refreshButton.performClick();
        }
    }

    /*  initializes the background changer-helper object */
    private void setChangeBackgroundMode() {
        new BackgroundChanger(controller,root, changeBgView, this);
    }

    /*  creates and shows the about dialog. */
    private void showAboutDialog() {
        new CustomAboutDialog(this).createAndShow();
    }

    /* removes the change background view elements */
    public void removeSetBG() {
        changeBgView.removeAllViewsInLayout();
    }

    /*  Changes the background on startup
    *   uses different methods depending on the API level
    * */
    @SuppressLint("NewApi")
    private void changeBackground() {
        int backgroundIndex = controller.loadBackground();
        int[] backgrounds = BackgroundChanger.drawables;
        if (backgroundIndex > 0 && backgroundIndex < backgrounds.length) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    root.setBackground(getDrawable(backgrounds[backgroundIndex]));
                } else {
                    root.setBackground(getResources().getDrawable(backgrounds[backgroundIndex]));
                }
            } else {
                root.setBackgroundDrawable(getResources().getDrawable(backgrounds[backgroundIndex]));
            }
        }
    }
}
