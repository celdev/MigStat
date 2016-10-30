package com.celdev.migstat.controller.parser;

import android.os.AsyncTask;

import com.celdev.migstat.model.MultipleWaitingTimeWrapper;
import com.celdev.migstat.model.WaitingTime;
import com.celdev.migstat.model.query.Query;

import org.jsoup.nodes.Document;

import java.io.IOException;

public class ComplexWaitingTimeParsers {

    public static final String VISIT_SWEDEN = "q0:4";
    public static final String CITIZENSHIP = "q0:5";
    public static final String TRAVEL_DOCUMENT = "q0:6";
    public static final String UT_CARD_FOR_EU_FAMILY = "q0:7";

    public ComplexWaitingTimeParsers(String query, AsyncTaskResultReceiver asyncTaskResultReceiver){
        int parser = getParserFromQueryType(query);
        switch (parser) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                new TravelDocumentWaitingTimeParser(asyncTaskResultReceiver).execute(query);
                break;
        }
    }

    private int getParserFromQueryType(String query) {
        if (query.contains(VISIT_SWEDEN)) {
            return 0;
        }
        if (query.contains(CITIZENSHIP)) {
            return 1;
        }
        if (query.contains(TRAVEL_DOCUMENT)) {
            return 2;
        }
        return -1;
    }

    public static class TravelDocumentWaitingTimeParser extends WaitingTimeParser {

        public TravelDocumentWaitingTimeParser(AsyncTaskResultReceiver asyncTaskResultReceiver) {
            super(asyncTaskResultReceiver);
        }

        @Override
        protected MultipleWaitingTimeWrapper doInBackground(String... strings) {
            try {
                Document document = getDocumentForURL(strings[0]);
                Query.SwedishOrEnglishQuery swedishOrEnglishQuery = swedishOrEnglishQuery(document);
                MultipleWaitingTimeWrapper multipleWaitingTimeWrapper;
                if (swedishOrEnglishQuery.equals(Query.SwedishOrEnglishQuery.SWEDISH)) {

                } else {

                }
            } catch (IOException e) {

            }
            return null;
        }

    }


}
