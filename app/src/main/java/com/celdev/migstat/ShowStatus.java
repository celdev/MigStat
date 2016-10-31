package com.celdev.migstat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

public class ShowStatus extends AppCompatActivity implements Observer{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_status);
    }

    @Override
    public void update(Observable observable, Object o) {

    }
}
