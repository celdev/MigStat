package com.celdev.migstat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import com.celdev.migstat.view.CustomWebView;

public class MainActivity extends AppCompatActivity {

    public static String LOG_KEY = "migstat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
}
