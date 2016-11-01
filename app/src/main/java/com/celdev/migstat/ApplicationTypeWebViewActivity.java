package com.celdev.migstat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.celdev.migstat.controller.DataStorage;
import com.celdev.migstat.controller.WaitingTimeDataStoragePacket;
import com.celdev.migstat.model.WaitingTime;
import com.celdev.migstat.view.CustomWebView;

import java.io.Serializable;
import java.util.Locale;


public class ApplicationTypeWebViewActivity extends AppCompatActivity {

    public static final String SWEDISH_PAGE_URL = "http://www.migrationsverket.se/Kontakta-oss/Tid-till-beslut.html";
    public static final String ENGLISH_PAGE_URL = "http://www.migrationsverket.se/English/Contact-us/Time-to-a-decision.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_type_web_view);
        initWebView(Locale.getDefault().getLanguage().equals("sv"));
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(boolean swedish) {
        WebView webView = (WebView) findViewById(R.id.type_web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new CustomWebResourceResponseWebViewClient());
        webView.loadUrl(swedish ? SWEDISH_PAGE_URL : ENGLISH_PAGE_URL);
    }

    private class CustomWebResourceResponseWebViewClient extends WebViewClient {

        private static final String FINAL_REQUEST_KEYWORD = "history";

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (requestIsSendQuery("" + request.getUrl())) {
                saveQuery("" + request.getUrl());
                moveToNextActivity();
            }
            return super.shouldInterceptRequest(view, request);
        }

        /*  Returns true if the request is the "send query"-request
        *   that is sent when the user answers the final question. */
        private boolean requestIsSendQuery(String request) {
            return request.contains(FINAL_REQUEST_KEYWORD);
        }
    }

    private void saveQuery(String query) {
        DataStorage.getInstance().saveWaitingTimeQuery(this, query);
    }

    private void moveToNextActivity() {
        Intent intent = new Intent(ApplicationTypeWebViewActivity.this, ShowStatus.class);
        startActivity(intent);
    }

}

