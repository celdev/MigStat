package com.celdev.migstat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ShowStatus extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_status);

        TextView textView = (TextView) findViewById(R.id.textTest);
        textView.setText(getIntent().getExtras().getString("abc"));

    }
}
