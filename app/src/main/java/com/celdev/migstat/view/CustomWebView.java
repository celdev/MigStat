package com.celdev.migstat.view;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.celdev.migstat.MainActivity;
import com.celdev.migstat.ShowStatus;

public class CustomWebView extends WebViewClient {

    private Activity activity;

    public CustomWebView(Activity activity) {
        this.activity = activity;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        Log.d(MainActivity.LOG_KEY, "" + request.getUrl());
        if (("" + request.getUrl()).contains("history")) {
            Intent intent = new Intent(activity, ShowStatus.class);
            intent.putExtra("abc", request.getUrl().toString());
            activity.startActivity(intent);
        }
        return super.shouldInterceptRequest(view, request);
    }
}
