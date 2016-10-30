package com.celdev.migstat.controller;

import com.celdev.migstat.controller.parser.AsyncTaskResultReceiver;
import com.celdev.migstat.controller.parser.ComplexWaitingTimeParsers;
import com.celdev.migstat.controller.parser.MonthsWaitingTimeParser;
import com.celdev.migstat.model.MultipleWaitingTimeWrapper;
import com.celdev.migstat.model.WaitingTime;

public class WebViewResponseParser implements AsyncTaskResultReceiver{

    private static final String[] COMPLEX_QUERIES = {
            ComplexWaitingTimeParsers.VISIT_SWEDEN,
            ComplexWaitingTimeParsers.CITIZENSHIP,
            ComplexWaitingTimeParsers.TRAVEL_DOCUMENT,
            ComplexWaitingTimeParsers.UT_CARD_FOR_EU_FAMILY
    };

    public WebViewResponseParser(String query) {
        if (isComplexResponse(query)) {

        } else {
            new MonthsWaitingTimeParser(this);
        }
    }

    public boolean isComplexResponse(String query) {
        for (String s : COMPLEX_QUERIES) {
            if (query.contains(s)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void receiveResult(Object waitingTime) {

    }

}
