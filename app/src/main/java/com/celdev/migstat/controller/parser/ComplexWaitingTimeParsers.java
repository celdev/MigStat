package com.celdev.migstat.controller.parser;

import com.celdev.migstat.controller.AsyncCallback;
import com.celdev.migstat.model.MultipleWaitingTimeWrapper;
import com.celdev.migstat.model.ParserException;
import com.celdev.migstat.model.query.Query;

import org.jsoup.nodes.Document;

import java.io.IOException;

import static com.celdev.migstat.controller.WebViewResponseParser.CITIZENSHIP;
import static com.celdev.migstat.controller.WebViewResponseParser.TRAVEL_DOCUMENT;
import static com.celdev.migstat.controller.WebViewResponseParser.UT_CARD_FOR_EU_FAMILY;
import static com.celdev.migstat.controller.WebViewResponseParser.VISIT_SWEDEN;

public class ComplexWaitingTimeParsers {

    public static WaitingTimeParser getCorrectWaitingTimeParser(String type, AsyncCallback asyncCallback){
        switch (type) {
            case VISIT_SWEDEN:
                return new VisitSwedenWaitingTimeParser(asyncCallback);
            case CITIZENSHIP:
                return new CitizenshipWaitingTimeParser(asyncCallback);
            case TRAVEL_DOCUMENT:
                return new TravelDocumentWaitingTimeParser(asyncCallback);
            case UT_CARD_FOR_EU_FAMILY:
                return new UTCardForEUFamilyWaitingTimeParser(asyncCallback);
        }
        return null;
    }

    private static class TravelDocumentWaitingTimeParser extends WaitingTimeParser {

        private TravelDocumentWaitingTimeParser(AsyncCallback asyncCallback) {
            super(asyncCallback);
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

    private static class CitizenshipWaitingTimeParser extends WaitingTimeParser {
        private CitizenshipWaitingTimeParser(AsyncCallback asyncCallback) {
            super(asyncCallback);
        }

        @Override
        protected Object doInBackground(String... strings) {
            return null;
        }
    }

    private static class UTCardForEUFamilyWaitingTimeParser extends WaitingTimeParser {
        private UTCardForEUFamilyWaitingTimeParser(AsyncCallback asyncCallback) {
            super(asyncCallback);
        }

        @Override
        protected Object doInBackground(String... strings) {
            return null;
        }
    }

    private static class VisitSwedenWaitingTimeParser extends WaitingTimeParser {
        private VisitSwedenWaitingTimeParser(AsyncCallback asyncCallback) {
            super(asyncCallback);
        }

        @Override
        protected Object doInBackground(String... strings) {
            return null;
        }
    }


}
