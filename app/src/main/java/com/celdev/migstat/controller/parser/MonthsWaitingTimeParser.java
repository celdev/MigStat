package com.celdev.migstat.controller.parser;

import com.celdev.migstat.model.WaitingTime;
import com.celdev.migstat.model.query.Query;

import org.jsoup.nodes.Document;

import java.io.IOException;

public class MonthsWaitingTimeParser extends WaitingTimeParser{

    public MonthsWaitingTimeParser(AsyncTaskResultReceiver asyncTaskResultReceiver) {
        super(asyncTaskResultReceiver);
    }

    @Override
    protected Object doInBackground(String... strings) {
        try {
            Document document = getDocumentForURL(strings[0]);
            Query.SwedishOrEnglishQuery swedishOrEnglishQuery = swedishOrEnglishQuery(document);
            WaitingTime waitingTime;
            if (swedishOrEnglishQuery.equals(Query.SwedishOrEnglishQuery.SWEDISH)) {

            } else {
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }


}
