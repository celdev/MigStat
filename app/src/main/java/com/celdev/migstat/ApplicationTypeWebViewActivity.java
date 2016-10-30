package com.celdev.migstat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.celdev.migstat.view.CustomWebView;


public class ApplicationTypeWebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_type_web_view);
        initWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        WebView webView = (WebView) findViewById(R.id.type_web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new CustomWebResourceResponseWebViewClient());
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

    }

    private void moveToNextActivity() {
        Intent intent = new Intent(ApplicationTypeWebViewActivity.this, ShowStatus.class);
        startActivity(intent);
    }

}

