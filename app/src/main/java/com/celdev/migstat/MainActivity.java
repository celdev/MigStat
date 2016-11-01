package com.celdev.migstat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.celdev.migstat.controller.Controller;
import com.celdev.migstat.controller.DataStorage;
import com.celdev.migstat.controller.parser.SimpleCaseStatusParser;
import com.celdev.migstat.model.Application;
import com.celdev.migstat.model.ApplicationNumberType;
import com.celdev.migstat.view.CustomWebView;
import com.celdev.migstat.view.ViewUpdateReceiver;

import java.io.Serializable;

public class MainActivity extends AppCompatActivity implements ViewUpdateReceiver {


    public static String LOG_KEY = "migstat";

    public static final String APPLICATION_KEY = "com.celdev.migstat";

    private RadioButton caseRadioButton;
    private EditText applicationNumberField;

    private Application application;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Switch useNumberSwitch = (Switch) findViewById(R.id.use_number_switch);
        applicationNumberField = (EditText) findViewById(R.id.number_field);
        final Button checkStatusButton = (Button) findViewById(R.id.check_status_button);
        caseRadioButton = (RadioButton) findViewById(R.id.case_radio_button);

        ImageButton waitingTimeButton = (ImageButton) findViewById(R.id.waitingtime_launcher);
        waitingTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ApplicationTypeWebViewActivity.class);
                startActivity(intent);
            }
        });

        checkStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doStatusCheck();
            }
        });
    }

    private ApplicationNumberType getApplicationNumberType() {
        if (caseRadioButton.isChecked()) {
            return ApplicationNumberType.CASE_NUMBER;
        }
        return ApplicationNumberType.CHECK_NUMBER;
    }

    @Override
    public void receiveUpdate(Object object) {
        if (object == null) {
            //error
        } else if (object instanceof SimpleCaseStatusParser.StatusAndDate) {
            SimpleCaseStatusParser.StatusAndDate s = (SimpleCaseStatusParser.StatusAndDate) object;
            application = new Application(this,
                    s.getStatusType(),
                    s.getApplicationDate().getApplicationDate(),
                    s.getApplicationNumber().getApplicationNumber(),
                    s.getApplicationNumber().getApplicationNumberType());
            DataStorage.getInstance().saveApplication(this, application);
        }
        if (application != null) {
            Log.d(LOG_KEY, application.toString());
        }
    }

    private void doStatusCheck() {
        new Controller(MainActivity.this).checkApplication(Integer.valueOf(applicationNumberField.getText().toString()), getApplicationNumberType());
    }

    private void resetAllData() {
        DataStorage.getInstance().deleteAllData(this);
    }

}
