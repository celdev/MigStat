package com.celdev.migstat.controller;

import android.util.Log;

import com.celdev.migstat.MainActivity;
import com.celdev.migstat.controller.parser.AsyncTaskResultReceiver;
import com.celdev.migstat.controller.parser.ComplexWaitingTimeParsers;
import com.celdev.migstat.controller.parser.MonthsWaitingTimeParser;
import com.celdev.migstat.controller.parser.WaitingTimeParser;
import com.celdev.migstat.model.MultipleWaitingTimeWrapper;
import com.celdev.migstat.model.ParserException;
import com.celdev.migstat.model.WaitingTime;

public class WebViewResponseParser {

    public static final String VISIT_SWEDEN = "q0:4";
    public static final String CITIZENSHIP = "q0:5";
    public static final String TRAVEL_DOCUMENT = "q0:6";
    public static final String UT_CARD_FOR_EU_FAMILY = "q0:7";

    private static final String[] COMPLEX_QUERIES = {
            VISIT_SWEDEN,
            CITIZENSHIP,
            TRAVEL_DOCUMENT,
            UT_CARD_FOR_EU_FAMILY
    };

    private static final String NOT_COMPLEX = "not_complex";

    public WebViewResponseParser(String query, AsyncTaskResultReceiver asyncTaskResultReceiver) {
        String parserType = returnComplexString(query);
        if (query.trim().isEmpty()) {
            asyncTaskResultReceiver.receiveResult(null);
        }
        switch (parserType) {
            case NOT_COMPLEX:
                Log.d(MainActivity.LOG_KEY, "Using not complex parser");
                new MonthsWaitingTimeParser(asyncTaskResultReceiver).execute(query);
                break;
            default:
                Log.d(MainActivity.LOG_KEY, "Using complex parser");
                WaitingTimeParser waitingTimeParser = ComplexWaitingTimeParsers.getCorrectWaitingTimeParser(parserType, asyncTaskResultReceiver);
                if (waitingTimeParser != null) {
                    waitingTimeParser.execute(query);
                } else {
                    asyncTaskResultReceiver.receiveResult(null);
                }
        }
    }

    public String returnComplexString(String query) {
        for (String s : COMPLEX_QUERIES) {
            if (query.contains(s)) {
                return s;
            }
        }
        return NOT_COMPLEX;
    }
}
