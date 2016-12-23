package com.celdev.migstat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
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
import com.celdev.migstat.view.CustomAboutDialog;
import com.celdev.migstat.view.ViewInterface;

/*  This Acitivy contains a WebView that has a number of special funcations
*       * it can only be used for answering the questions of the web page (only the specified url
*           may be accessed)
*       * when the user answers all questions the url that is called is intercepted and used to
*           parse and save the average waiting time.
*
*   Depending on if the extra swedish boolean is present in the intent that creates this Activity
*   the WebView will show the swedish or english web page
*
*   when the waiting time has been extracted the user will be moved to the ShowStatus Activity
* */
public class ApplicationTypeWebViewActivity extends AppCompatActivity implements ViewInterface {

    public static final String SWEDISH_PAGE_URL = "http://www.migrationsverket.se/Kontakta-oss/Tid-till-beslut.html";
    public static final String ENGLISH_PAGE_URL = "http://www.migrationsverket.se/English/Contact-us/Time-to-a-decision.html";

    private ProgressDialog progressDialog;
    private String url;

    private Controller controller = new Controller(this, this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_type_web_view);
        initProgressDialog();
        initWebView(swedishExtraInIntent());
    }

    /* returns true if the intent contains the extra swedish boolean*/
    private boolean swedishExtraInIntent() {
        return getIntent() != null && (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(MainActivity.WEBVIEWLANGUGAGE_INTENT, false));
    }

    /*  Creates and shows a progress dialog)
    * */
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_website));
        progressDialog.show();
    }

    /*  initializes the WebView
    *   enables javascript and sets the WebViewClient to the custom made
    *   client.
    *   loads the migrationsverkets url (swedish or engish depending on the boolean
    *   passed as a parameter)
    * */
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(boolean swedish) {
        WebView webView = (WebView) findViewById(R.id.type_web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new CustomWebResourceResponseWebViewClient());
        url = swedish ? SWEDISH_PAGE_URL : ENGLISH_PAGE_URL;
        webView.loadUrl(url);
    }

    /*  Dismisses the progress dialog (which is shown while the web page is loading)
    *   and shows a dialog explaining what the user should do
    * */
    private void dismissProgressDialogAndShowWhatToDoDialog() {
        boolean shouldShowWhatToDoDialog = progressDialog.isShowing();
        progressDialog.dismiss();
        if (shouldShowWhatToDoDialog) {
            showWhatToDoDialog();
        }
    }

    /*  This method is called when the check of the waiting time url have been done
    *   if the waiting time is ok the user is moved to the ShowStatus-activity
    *   otherwise an error dialog is shown
    * */
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

    /*  creates and shows a dialog which explains what the user should do*/
    private void showWhatToDoDialog() {
        new AlertDialog.Builder(this).setMessage(R.string.what_to_do_message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }


    /*  This class extends the WebViewClient class and overrides a couple of methods which
    *   enables the interception of the url that is called when the user finishes answering
    *   all questions
    *   also disables the ability for the user to use the webview to go to other web pages
    * */
    private class CustomWebResourceResponseWebViewClient extends WebViewClient {

        private static final String FINAL_REQUEST_KEYWORD = "history";

        /*  if the request is a send query (= when all questions have been answered)
        *   then the url of the request will be saved and if the waiting time can be parsed
        *   the user is moved to the ShowStatus activity
        * */
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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

        /*  does the same as the method above but for low API*/
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (requestIsSendQuery("" + url)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initProgressDialog();
                    }
                });
                saveQuery("" + url);
            }
            return super.shouldInterceptRequest(view, url);
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

    /*  shows a dialog if the user tries to press an incorrect link or button */
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

    /* specifies which xml-file contains the menu-xml */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.only_about_menu, menu);
        return true;
    }


    /*  this is called when the application is started or resume
    *   makes sure the controller isn't null */
    @Override
    protected void onResume() {
        super.onResume();
        controller = new Controller(this, this);
    }


    /*  this method is called when an item in the menu is pressed*/
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

    /*  shows the about dialog */
    private void showAboutDialog() {
        new CustomAboutDialog(this).createAndShow();
    }

}

