package com.celdev.migstat;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.celdev.migstat.controller.Controller;
import com.celdev.migstat.controller.DataStorage;
import com.celdev.migstat.controller.NoApplicationException;
import com.celdev.migstat.controller.NoWaitingTimeException;
import com.celdev.migstat.controller.WaitingTimeDataStoragePacket;
import com.celdev.migstat.controller.parser.SimpleCaseStatusParser;
import com.celdev.migstat.controller.utils.DateUtils;
import com.celdev.migstat.model.Application;
import com.celdev.migstat.model.ApplicationNumberType;
import com.celdev.migstat.view.CustomDatePickerDialog;
import com.celdev.migstat.view.IntegerInputListener;
import com.celdev.migstat.view.ViewUpdateReceiver;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {


    public static String LOG_KEY = "migstat";

    public static final String APPLICATION_KEY = "com.celdev.migstat";
    public static final String WEBVIEWLANGUGAGE_INTENT = APPLICATION_KEY + ".WEB_VIEW_INTENT";

    private RadioButton caseRadioButton, checkRadioButton;
    private EditText applicationNumberField;
    private Button checkStatusButton, setDateButton, afterSetDateButton;
    private ImageButton waitingTimeButtonEng, waitingTimeButtonSwe;

    private ViewSwitcher replaceView;
    private View useNumberView, useNoNumberView;
    private ProgressDialog progressDialog;

    private Switch useNumberSwitch;
    private Application application;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //resetAllData();
        DataStorage.getInstance().DEBUG_deleteWaitingTime(this);



        initLayout();

        initSwitch();
        initRadioButtons();
        initButtons();
        initNumberField();
        initProgressDialog();
        checkState();

    }

    private void checkState() {
        try {
            application = DataStorage.getInstance().loadApplication(this);
            try {
                WaitingTimeDataStoragePacket waitingTimeDataStoragePacket = DataStorage.getInstance().loadWaitingTime(this);
                startActivity(new Intent(this, ShowStatus.class));
            } catch (NoWaitingTimeException e) {
                e.printStackTrace();
                setLockMode(ViewLockMode.APPLICATION_LOCK);
            }
        } catch (NoApplicationException e) {
            e.printStackTrace();
        }
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_application_information));
    }

    private void initNumberField() {
        applicationNumberField = (EditText) findViewById(R.id.number_field);
        applicationNumberField.addTextChangedListener(new IntegerInputListener(this,applicationNumberField));
    }

    private void initButtons() {
        checkStatusButton = (Button) findViewById(R.id.check_status_button);
        checkStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doStatusCheck();
            }
        });

        setDateButton = (Button) findViewById(R.id.set_application_date_button);
        setDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });



        afterSetDateButton = (Button) findViewById(R.id.after_set_date_button);
        afterSetDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (application != null) {
                    DataStorage.getInstance().saveApplication(MainActivity.this, application);
                    setLockMode(ViewLockMode.APPLICATION_LOCK);
                }
            }
        });

        initWaitingTimeButtons();
    }

    private void disableWaitingTimeButtons() {
        waitingTimeButtonSwe.setEnabled(false);
        waitingTimeButtonEng.setEnabled(false);
        Drawable grayEngFlag = getDrawable(R.drawable.ic_flag_of_the_united_kingdom);
        Drawable graySweFlag = getDrawable(R.drawable.ic_flag_of_sweden);
        if (grayEngFlag != null && graySweFlag != null) {
            grayEngFlag.mutate();
            graySweFlag.mutate();
            grayEngFlag.setColorFilter(Color.GRAY, PorterDuff.Mode.ADD);
            graySweFlag.setColorFilter(Color.GRAY, PorterDuff.Mode.ADD);
            waitingTimeButtonEng.setImageDrawable(grayEngFlag);
            waitingTimeButtonSwe.setImageDrawable(graySweFlag);

        }
    }

    private void enableWaitingTimeButtons() {
        waitingTimeButtonSwe.setEnabled(true);
        waitingTimeButtonEng.setEnabled(true);
        Drawable grayEngFlag = getDrawable(R.drawable.ic_flag_of_the_united_kingdom);
        Drawable graySweFlag = getDrawable(R.drawable.ic_flag_of_sweden);
        if (grayEngFlag != null && graySweFlag != null) {
            waitingTimeButtonEng.setImageDrawable(grayEngFlag);
            waitingTimeButtonSwe.setImageDrawable(graySweFlag);

        }

    }

    private void initWaitingTimeButtons() {
        waitingTimeButtonEng = (ImageButton) findViewById(R.id.waitingtime_launcher_eng);
        waitingTimeButtonEng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ApplicationTypeWebViewActivity.class);
                startActivity(intent);
            }
        });

        waitingTimeButtonSwe = (ImageButton) findViewById(R.id.waitingtime_launcher_swe);
        waitingTimeButtonSwe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ApplicationTypeWebViewActivity.class);
                intent.putExtra(WEBVIEWLANGUGAGE_INTENT, true);
                startActivity(intent);
            }
        });

        disableWaitingTimeButtons();

    }

    private void initLayout() {
        replaceView = (ViewSwitcher) findViewById(R.id.replaceable_application_number_view);
        useNumberView = findViewById(R.id.use_number_view);
        useNoNumberView = findViewById(R.id.no_number_view);
    }

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


    private void initRadioButtons() {
        caseRadioButton = (RadioButton) findViewById(R.id.case_radio_button);
        checkRadioButton = (RadioButton) findViewById(R.id.check_radio_button);
    }

    private void initSwitch() {
        useNumberSwitch = (Switch) findViewById(R.id.use_number_switch);
        useNumberSwitch.setOnCheckedChangeListener(switchListener);
    }

    private CompoundButton.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            setUseNumberStateEnabled(isChecked);
            if (!isChecked) {
                showNoNumberDialog();
            }
        }
    };

    private void setUseNumberStateEnabled(boolean enabled) {
        if (enabled && !replaceView.getCurrentView().equals(useNumberView)) {
            replaceView.showNext();
            nullApplication();
        } else if (!enabled && !replaceView.getCurrentView().equals(useNoNumberView)) {
            replaceView.showPrevious();
            nullApplication();
        }
    }

    private void nullApplication() {
        application = null;
        resetViews();
    }

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
    }

    private void lockApplicationAfterDownload() {
        useNumberSwitch.setEnabled(false);
        checkRadioButton.setEnabled(false);
        caseRadioButton.setEnabled(false);
        applicationNumberField.setEnabled(false);
        checkStatusButton.setEnabled(false);
    }

    private void lockApplicationAfterSetDate() {
        useNumberSwitch.setEnabled(false);
        setDateButton.setEnabled(false);
        afterSetDateButton.setEnabled(false);
    }

    private ApplicationNumberType getApplicationNumberType() {
        if (caseRadioButton.isChecked()) {
            return ApplicationNumberType.CASE_NUMBER;
        }
        return ApplicationNumberType.CHECK_NUMBER;
    }

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

    private ViewUpdateReceiver applicationAsyncReceiver = new ViewUpdateReceiver() {
        @Override
        public void receiveUpdate(Object object) {
            progressDialog.dismiss();
            if (object == null) {
                checkStatusButton.setEnabled(true);
                setOkIconAfterStatusCheck(false);
                Toast.makeText(MainActivity.this, R.string.error_parsing_case, Toast.LENGTH_LONG).show();
            } else if (object instanceof SimpleCaseStatusParser.StatusAndDate) {
                SimpleCaseStatusParser.StatusAndDate s = (SimpleCaseStatusParser.StatusAndDate) object;
                application = new Application(getApplication(),
                        s.getStatusType(),
                        s.getApplicationDate().getApplicationDate(),
                        s.getApplicationNumber().getApplicationNumber(),
                        s.getApplicationNumber().getApplicationNumberType());
                DataStorage.getInstance().saveApplication(getApplication(), application);
                setOkIconAfterStatusCheck(true);
            }
        }
    };

    private void setOkIconAfterStatusCheck(boolean success) {
        if (success) {
            applicationNumberField.setCompoundDrawablesWithIntrinsicBounds(0,0,android.R.drawable.checkbox_on_background,0);
            setLockMode(ViewLockMode.APPLICATION_LOCK);
        } else {
            applicationNumberField.setCompoundDrawablesWithIntrinsicBounds(0,0,android.R.drawable.ic_delete,0);
        }
    }


    private void doStatusCheck() {
        checkStatusButton.setEnabled(false);
        application = null;
        progressDialog.show();
        new Controller(applicationAsyncReceiver).checkApplication(Integer.valueOf(applicationNumberField.getText().toString()), getApplicationNumberType());
    }


    private void resetAllData() {
        DataStorage.getInstance().deleteAllData(this);
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


    private void setLockMode(ViewLockMode viewLockMode) {
        switch (viewLockMode) {
            case APPLICATION_LOCK:
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


    private enum ViewLockMode {
        APPLICATION_LOCK,
        WAITING_TIME_LOCK,
        UNLOCK_APPLICATION;
    }

}
