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

        WebView webView = (WebView) findViewById(R.id.webviewtest);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new CustomWebView(this));

        webView.loadUrl("http://www.migrationsverket.se/Kontakta-oss/Tid-till-beslut.html");

    }
}
