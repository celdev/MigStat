package com.celdev.migstat.controller.parser;

import android.os.AsyncTask;

import com.celdev.migstat.model.WaitingTime;
import com.celdev.migstat.model.query.Query;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public abstract class WaitingTimeParser extends AsyncTask<String, Void, Object> {

    /*  The id the swedish response uses */
    public static final String SWEDISH_ID = "h-Tidtillbeslut";

    private AsyncTaskResultReceiver asyncTaskResultReceiver;

    public WaitingTimeParser(AsyncTaskResultReceiver asyncTaskResultReceiver) {
        this.asyncTaskResultReceiver = asyncTaskResultReceiver;
    }

    public Document getDocumentForURL(String url) throws IOException{
        return Jsoup.connect(url).get();
    }

    /*  To determine if the document is in english or swedish I'm using the fact that
    *   the two different responses use different names on the id of some elements
    *   <h2 class="subheading" id="h-Tidtillbeslut">Tid till beslut</h2>        = swedish
    *   <h2 class="subheading" id="h-Timetoadecision">Time to a decision</h2>   = english
    *
    *   if there is an element with the id h-Tidtillbeslut then that element won't be null and
    *   Swedish vill be returned
    * */
    public Query.SwedishOrEnglishQuery swedishOrEnglishQuery(Document document){
        return document.getElementById(SWEDISH_ID) != null ? Query.SwedishOrEnglishQuery.SWEDISH : Query.SwedishOrEnglishQuery.ENGLISH;
    }

    /*  Sends the response back to the registered listener */
    @Override
    protected void onPostExecute(Object waitingTime) {
        asyncTaskResultReceiver.receiveResult(waitingTime);
    }

}

