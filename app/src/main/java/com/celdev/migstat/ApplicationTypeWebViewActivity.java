package com.celdev.migstat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.celdev.migstat.controller.Controller;
import com.celdev.migstat.controller.DataStorage;
import com.celdev.migstat.view.CustomAboutDialog;
import com.celdev.migstat.view.ViewInterface;


public class ApplicationTypeWebViewActivity extends AppCompatActivity implements ViewInterface {

    public static final String SWEDISH_PAGE_URL = "http://www.migrationsverket.se/Kontakta-oss/Tid-till-beslut.html";
    public static final String ENGLISH_PAGE_URL = "http://www.migrationsverket.se/English/Contact-us/Time-to-a-decision.html";

    private ProgressDialog progressDialog;
    private String url;

    private Controller controller = Controller.getInstance(this, this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_type_web_view);
        initProgressDialog();
        initWebView(swedishExtraInIntent());
    }

    private boolean swedishExtraInIntent() {
        return getIntent() != null && (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(MainActivity.WEBVIEWLANGUGAGE_INTENT, false));
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_website));
        progressDialog.show();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(boolean swedish) {
        WebView webView = (WebView) findViewById(R.id.type_web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new CustomWebResourceResponseWebViewClient());
        url = swedish ? SWEDISH_PAGE_URL : ENGLISH_PAGE_URL;
        webView.loadUrl(url);
    }

    private void dismissProgressDialogAndShowWhatToDoDialog() {
        boolean shouldShowWhatToDoDialog = progressDialog.isShowing();
        progressDialog.dismiss();
        if (shouldShowWhatToDoDialog) {
            showWhatToDoDialog();
        }
    }

    @Override
    public void modelChange(ModelChange modelChange) {
        progressDialog.dismiss();
        switch (modelChange) {
            case WAITING_TIME_OK:
                moveToNextActivity();
                return;
            case ERROR_UPDATE:
            case MULTIPLE_WAITING_TIME:
                Toast.makeText(this, R.string.not_implemented_yet, Toast.LENGTH_LONG).show();
                return;
        }
    }

    private void showWhatToDoDialog() {
        new AlertDialog.Builder(this).setMessage(R.string.what_to_do_message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }


    private class CustomWebResourceResponseWebViewClient extends WebViewClient {

        private static final String FINAL_REQUEST_KEYWORD = "history";

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (requestIsSendQuery("" + request.getUrl())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initProgressDialog();
                    }
                });
                saveQuery("" + request.getUrl());
            }
            return super.shouldInterceptRequest(view, request);
        }




        /*  Called when the page is finished
        *   removes the progress dialog.
        *
        *   if the url loaded isn't the same as the "check waitingtime"-page the
        *   webview will be redirected there so that the
        *   user can't access other page through this webview
        *   shows a dialog about why the users is redirected
        *
        *   shouldOverrideUrlLoading() isn't called for some types of Urls
        *   so this seems to be the current "best" workaround
        * */
        @Override
        public void onPageFinished(WebView view, String url) {
            dismissProgressDialogAndShowWhatToDoDialog();
            if (!url.equals(ApplicationTypeWebViewActivity.this.url)) {
                view.loadUrl(ApplicationTypeWebViewActivity.this.url);
                showWrongLinkDialog();
            }
            super.onPageFinished(view, url);
        }


        /*  Returns true if the request is the "send query"-request
                *   that is sent when the user answers the final question. */
        private boolean requestIsSendQuery(String request) {
            return request.contains(FINAL_REQUEST_KEYWORD);
        }
    }

    private void showWrongLinkDialog() {
        new AlertDialog.Builder(this).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setMessage(R.string.wrong_link_webview).create().show();
    }

    /*  Stores the url to the page containing the average waiting time
    * */
    private void saveQuery(String query) {
        controller.handleWaitingTimeQuery(query);
    }

    /*  Transfers the user to the next Activity (ShowStatus)
    * */
    private void moveToNextActivity() {
        Intent intent = new Intent(ApplicationTypeWebViewActivity.this, ShowStatus.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.only_about_menu, menu);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        controller = Controller.getInstance(this, this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                showAboutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAboutDialog() {
        new CustomAboutDialog(this).createAndShow();
    }

}

