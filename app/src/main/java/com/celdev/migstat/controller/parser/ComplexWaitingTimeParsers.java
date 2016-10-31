package com.celdev.migstat.controller.parser;

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

    public static WaitingTimeParser getCorrectWaitingTimeParser(String type, AsyncTaskResultReceiver asyncTaskResultReceiver) throws ParserException{
        switch (type) {
            case VISIT_SWEDEN:
                return new VisitSwedenWaitingTimeParser(asyncTaskResultReceiver);
            case CITIZENSHIP:
                return new CitizenshipWaitingTimeParser(asyncTaskResultReceiver);
            case TRAVEL_DOCUMENT:
                return new TravelDocumentWaitingTimeParser(asyncTaskResultReceiver);
            case UT_CARD_FOR_EU_FAMILY:
                return new UTCardForEUFamilyWaitingTimeParser(asyncTaskResultReceiver);
        }
        throw new ParserException();
    }

    private static class TravelDocumentWaitingTimeParser extends WaitingTimeParser {

        private TravelDocumentWaitingTimeParser(AsyncTaskResultReceiver asyncTaskResultReceiver) {
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

    private static class CitizenshipWaitingTimeParser extends WaitingTimeParser {
        private CitizenshipWaitingTimeParser(AsyncTaskResultReceiver asyncTaskResultReceiver) {
            super(asyncTaskResultReceiver);
        }

        @Override
        protected Object doInBackground(String... strings) {
            return null;
        }
    }

    private static class UTCardForEUFamilyWaitingTimeParser extends WaitingTimeParser {
        private UTCardForEUFamilyWaitingTimeParser(AsyncTaskResultReceiver asyncTaskResultReceiver) {
            super(asyncTaskResultReceiver);
        }

        @Override
        protected Object doInBackground(String... strings) {
            return null;
        }
    }

    private static class VisitSwedenWaitingTimeParser extends WaitingTimeParser {
        private VisitSwedenWaitingTimeParser(AsyncTaskResultReceiver asyncTaskResultReceiver) {
            super(asyncTaskResultReceiver);
        }

        @Override
        protected Object doInBackground(String... strings) {
            return null;
        }
    }


}
