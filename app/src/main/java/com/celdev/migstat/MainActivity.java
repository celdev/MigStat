package com.celdev.migstat;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.celdev.migstat.controller.Controller;
import com.celdev.migstat.controller.utils.DateUtils;
import com.celdev.migstat.model.Application;
import com.celdev.migstat.model.ApplicationNumberType;
import com.celdev.migstat.model.WaitingTime;
import com.celdev.migstat.view.CustomAboutDialog;
import com.celdev.migstat.view.CustomDatePickerDialog;
import com.celdev.migstat.view.CustomPrivacyPolicyDialog;
import com.celdev.migstat.view.CustomSetWaitingTimeDialog;
import com.celdev.migstat.view.IntegerInputListener;
import com.celdev.migstat.view.NumberPickerDialogReturn;
import com.celdev.migstat.view.ViewInterface;

import java.util.Calendar;

/*  This Activity contains the functionality for specifying which (migrationsverket)application
*   this application will show the information about
*
*   The user can specify a number or specify the application date
*   the user can then specify what kind of application this is
*   (in the ApplicationTypeWebViewActivity)
*   to get the estimated waiting time for similar applications.
* */

public class MainActivity extends AppCompatActivity implements ViewInterface {


    public static String LOG_KEY = "migstat";

    public static final String APPLICATION_KEY = "com.celdev.migstat";
    public static final String WEBVIEWLANGUGAGE_INTENT = APPLICATION_KEY + ".WEB_VIEW_INTENT";
    public static final String PRIVACY_POLICY_URL = "https://celdev.github.io/MigStat/";

    private RadioButton caseRadioButton, checkRadioButton;
    private EditText applicationNumberField;
    private Button checkStatusButton, setDateButton, afterSetDateButton, customWaitingTimeButton;
    private Button waitingTimeButtonEng, waitingTimeButtonSwe;

    private ViewSwitcher replaceView;
    private View useNumberView, useNoNumberView;
    private ProgressDialog progressDialog;

    private Switch useNumberSwitch;
    private Application application;

    private Controller controller = new Controller(this, this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* checks if this is the first time a new version of the app is launched
        *  which requires the reset of the stored data in the application
        *  shows a dialog if this is the case
        * */
        if (controller.resetBecauseNewVersion()) {
            new AlertDialog.Builder(this).setMessage(R.string.reset_because_new_version).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }
        if (!controller.privacyPolicyHaveBeenShown()) {
            showPrivacyPolicy();
        }


        initLayout();

        initSwitch();
        initRadioButtons();
        initButtons();
        setupApplicationOKButtonFunction();
        initNumberField();
        initProgressDialog();
        checkState();
    }

    private void showPrivacyPolicy() {
        new CustomPrivacyPolicyDialog(this,controller).createAndShow();
    }

    /*  sets up the OK-button functionality
    *   and other view related to the "no application set"-state of this activity
    * */
    private void setupApplicationOKButtonFunction() {
        checkStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doStatusCheck();
            }
        });
        afterSetDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (application != null) {
                    controller.saveApplication(application);
                    setLockMode(ViewLockMode.APPLICATION_LOCK);
                }
            }
        });
        checkStatusButton.setText(R.string.ok);
        afterSetDateButton.setText(R.string.ok);
        findViewById(R.id.setup_application_ok_text).setVisibility(View.INVISIBLE);
        findViewById(R.id.set_date_application_ok_text).setVisibility(View.INVISIBLE);
    }

    /*  This method is called when the case check returns from checking if the
    *   application number was correct
    *
    *   shows a Toast if the application number was wrong
    *
    *   sets the mode of the application depending on if the check was successful or not.
    * */
    @Override
    public void modelChange(ModelChange modelChange) {
        progressDialog.dismiss();
        switch (modelChange) {
            case APPLICATION_OK:
                setOkIconAfterStatusCheck(true);
                return;
            case INVALID_APPLICATION:
                checkStatusButton.setEnabled(true);
                setOkIconAfterStatusCheck(false);
                Toast.makeText(MainActivity.this, R.string.error_parsing_case, Toast.LENGTH_LONG).show();
                return;
        }
    }

    /*  Checks the state of the saved data in the application
        *   There's 3 states
        *   1: Have all information (waiting time and application information)
        *   2: No application
        *   3: No waiting time
        *
        *   Depending on the state of the application the enabled GUI-elements should be altered
        *   if the application is set the possibility to change application number should be removed
        *   if the waiting time is set (and application) the user will be brought to the
        *   ShowStatus-activity.
        * */
    private void checkState() {
        switch (controller.getApplicationState()) {
            case NO_APPLICATION:
                controller.deleteAll();
                return;
            case NO_WAITING_TIME:
                setLockMode(ViewLockMode.APPLICATION_LOCK);
                return;
            case HAVE_BOTH:
                startActivity(new Intent(this, ShowStatus.class));
        }
    }

    /* creates the progress dialog and sets it's text
    * */
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_application_information));
    }

    /* adds a TextChangeListener to the application number EditText which checks so the information
     * entered is integers */
    private void initNumberField() {
        applicationNumberField = (EditText) findViewById(R.id.number_field);
        applicationNumberField.addTextChangedListener(new IntegerInputListener(this,applicationNumberField));
    }

    /*  fetches the buttons from the view and init some of the listeners */
    private void initButtons() {
        checkStatusButton = (Button) findViewById(R.id.check_status_button);
        setDateButton = (Button) findViewById(R.id.set_application_date_button);
        setDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        afterSetDateButton = (Button) findViewById(R.id.after_set_date_button);
        initWaitingTimeButtons();
    }

    /*  this method is called when the application is started or resumed
    *   checks the state of the application
    * */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_KEY, "Main activity onResume called");
        Log.d(LOG_KEY, "controller is null = " + (controller == null));
        checkState();
    }



    private void disableWaitingTimeButtons() {
        waitingTimeButtonSwe.setEnabled(false);
        waitingTimeButtonEng.setEnabled(false);
        customWaitingTimeButton.setEnabled(false);
    }

    private void enableWaitingTimeButtons() {
        waitingTimeButtonSwe.setEnabled(true);
        waitingTimeButtonEng.setEnabled(true);
        customWaitingTimeButton.setEnabled(true);
    }

    /*  initializes the waiting time buttons
    *   english button:
    *       starts the ApplicationTypeWebViewActivity
    *   swedish button:
    *       starts the ApplicationTypeWebViewActivity with an extra boolean in the intent
    *   custom waiting time:
    *       shows a dialog which enables the user to choose a custom waiting time (in months)
    *       transfers the user to the ShowStatus-activity after the waiting time has been set
    * */
    private void initWaitingTimeButtons() {
        waitingTimeButtonEng = (Button) findViewById(R.id.waitingtime_launcher_eng);
        waitingTimeButtonEng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ApplicationTypeWebViewActivity.class);
                startActivity(intent);
            }
        });

        waitingTimeButtonSwe = (Button) findViewById(R.id.waitingtime_launcher_swe);
        waitingTimeButtonSwe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ApplicationTypeWebViewActivity.class);
                intent.putExtra(WEBVIEWLANGUGAGE_INTENT, true);
                startActivity(intent);
            }
        });

        customWaitingTimeButton = (Button) findViewById(R.id.custom_waitingtime_button);
        customWaitingTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CustomSetWaitingTimeDialog(MainActivity.this, new NumberPickerDialogReturn() {
                    @Override
                    public void returnOnOk(int months) {
                        try {
                            controller.saveWaitingTime(new WaitingTime(months));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        startActivity(new Intent(MainActivity.this, ShowStatus.class));
                    }
                }).createAndShow();
            }
        });
        disableWaitingTimeButtons();

    }

    /*  fetches the views the activity need to alter
    * */
    private void initLayout() {
        replaceView = (ViewSwitcher) findViewById(R.id.replaceable_application_number_view);
        useNumberView = findViewById(R.id.use_number_view);
        useNoNumberView = findViewById(R.id.no_number_view);
    }

    /*  Shows a date-picker dialog, if the user specifies a date the ok button will be enabled
    *   and the date the user picked will be shown
    * */
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        CustomDatePickerDialog customDatePickerDialog = new CustomDatePickerDialog(this, calendar, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                application = new Application(DateUtils.dateToCalendar(year, month, dayOfMonth).getTimeInMillis());
                ((TextView) findViewById(R.id.set_application_date_text)).setText(
                        getString(R.string.date_of_application_placeholder, year, month + 1, dayOfMonth));
                afterSetDateButton.setEnabled(true);
            }
        });
        customDatePickerDialog.show();
    }


    /* fetches the radio buttons */
    private void initRadioButtons() {
        caseRadioButton = (RadioButton) findViewById(R.id.case_radio_button);
        checkRadioButton = (RadioButton) findViewById(R.id.check_radio_button);
    }

    /* fetches the switch and sets it's listener */
    private void initSwitch() {
        useNumberSwitch = (Switch) findViewById(R.id.use_number_switch);
        useNumberSwitch.setOnCheckedChangeListener(switchListener);
    }

    /*  the listener for the use application number switch
    *   shows a dialog about how the application uses personal information
    *   if the user switch the switch off
    * */
    private CompoundButton.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            setUseNumberStateEnabled(isChecked);
            if (!isChecked) {
                showNoNumberDialog();
            }
        }
    };

    /*  Shows the correct view depending on if the use-application-number switch is on or off */
    private void setUseNumberStateEnabled(boolean enabled) {
        if (enabled && !replaceView.getCurrentView().equals(useNumberView)) {
            replaceView.showNext();
            nullApplication();
        } else if (!enabled && !replaceView.getCurrentView().equals(useNoNumberView)) {
            replaceView.showPrevious();
            nullApplication();
        }
    }

    /* removes the application object and resets the views */
    private void nullApplication() {
        application = null;
        resetViews();
    }

    /* reset all the views */
    private void resetViews() {
        ((TextView) findViewById(R.id.set_application_date_text)).setText(R.string.set_your_application_date);
        applicationNumberField.setText("");
        applicationNumberField.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        checkStatusButton.setEnabled(true);
        applicationNumberField.setEnabled(true);
        caseRadioButton.setEnabled(true);
        checkRadioButton.setEnabled(true);
        useNumberSwitch.setEnabled(true);
        checkStatusButton.setEnabled(true);
        afterSetDateButton.setEnabled(false);
        setDateButton.setEnabled(true);
        setupApplicationOKButtonFunction();
    }

    /*  locks the "enter application number"-part of the view
    *   after a successful application number check
    * */
    private void lockApplicationAfterDownload() {
        useNumberSwitch.setEnabled(false);
        checkRadioButton.setEnabled(false);
        caseRadioButton.setEnabled(false);
        applicationNumberField.setEnabled(false);
    }

    /*  locks the set-date button after the user presses ok
    * */
    private void lockApplicationAfterSetDate() {
        useNumberSwitch.setEnabled(false);
        setDateButton.setEnabled(false);
    }

    /*  returns the ApplicationNumberType used (depending on which
    *   radio button is pressed
    * */
    private ApplicationNumberType getApplicationNumberType() {
        if (caseRadioButton.isChecked()) {
            return ApplicationNumberType.CASE_NUMBER;
        }
        return ApplicationNumberType.CHECK_NUMBER;
    }

    /*  shows a dialog which explains how this application
    *   uses the personal information the user may provide if they choose
    *   to use their applications number.
    * */
    private void showNoNumberDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).
                setMessage(R.string.no_number_info_dialog).
                setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();
        alertDialog.show();
    }

    /*  Sets the icon of the EditText-view depending on if the
    *   application number was correct
    * */
    private void setOkIconAfterStatusCheck(boolean success) {
        if (success) {
            applicationNumberField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_ok, 0);
            setLockMode(ViewLockMode.APPLICATION_LOCK);
        } else {
            applicationNumberField.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_delete, 0);
        }
    }

    /*  Sets the ok button-mode to cancel mode
    *   so that the user can reset the provided application information
    * */
    private void setOKButtonCancelMode() {
        checkStatusButton.setText(R.string.cancel);
        afterSetDateButton.setText(R.string.cancel);
        checkStatusButton.setEnabled(true);
        afterSetDateButton.setEnabled(true);
        findViewById(R.id.setup_application_ok_text).setVisibility(View.VISIBLE);
        findViewById(R.id.set_date_application_ok_text).setVisibility(View.VISIBLE);
        checkStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.deleteAll();
                setLockMode(ViewLockMode.UNLOCK_APPLICATION);
            }
        });
        afterSetDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.deleteAll();
                setLockMode(ViewLockMode.UNLOCK_APPLICATION);
            }
        });
    }

    /*  Specifies the file that contains the menu-xml
    * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.only_about_menu, menu);
        return true;
    }


    /*  This method is called when an item in the menu is pressed
    * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                showAboutDialog();
                return true;
            case R.id.menu_privacy:
                showPrivacyDocument(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void showPrivacyDocument(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(PRIVACY_POLICY_URL));
        activity.startActivity(intent);
    }

    /*  creates and shows the about dialog */
    private void showAboutDialog() {
        new CustomAboutDialog(this).createAndShow();
    }

    /*  Preforms a check of the entered application number
    *   locks the check button and shows a progress dialog
    * */
    private void doStatusCheck() {
        if (applicationNumberField.getText().toString().length() == 8) {
            checkStatusButton.setEnabled(false);
            application = null;
            progressDialog.show();
            controller.checkApplicationNumberReturnApplication(Integer.parseInt(applicationNumberField.getText().toString()), getApplicationNumberType());
        } else {
            Toast.makeText(this,R.string.enter_number,Toast.LENGTH_LONG).show();
        }
    }






    /*  overrides the functionality of the back button to make sure that the
    *   user can't "back into" a part of the application which shouldn't be
    *   accessible (i.e. backing to the ShowStatus-activity after reseting
    *   the application)
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


    /*  Sets the views and states in the activity to the correct mode
    * */
    private void setLockMode(ViewLockMode viewLockMode) {
        switch (viewLockMode) {
            case APPLICATION_LOCK:
                setOKButtonCancelMode();
                enableWaitingTimeButtons();
                lockApplicationAfterDownload();
                lockApplicationAfterSetDate();
                enableWaitingTimeButtons();
                break;
            case WAITING_TIME_LOCK:
                resetViews();
                break;
            case UNLOCK_APPLICATION:
                nullApplication();

                break;
        }
    }


    /*  enum used to represent the different modes this activity can have:
    *       APPLICATION_LOCK    the application information is set (waiting time button is unlocked)
    *       WAITING_TIME_LOCK   the application information isn't set (waiting time button locked)
    *       UNLOCK_APPLICATION  the application information was reset
    * */
    private enum ViewLockMode {
        APPLICATION_LOCK,
        WAITING_TIME_LOCK,
        UNLOCK_APPLICATION
    }

}
