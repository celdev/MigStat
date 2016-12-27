package com.celdev.migstat.controller.parser;

import com.celdev.migstat.controller.AsyncCallback;
import com.celdev.migstat.model.ParserException;
import com.celdev.migstat.model.WaitingTime;
import com.celdev.migstat.model.query.Query;

import org.jsoup.nodes.Document;

import java.io.IOException;

/*  This class extends WaitingTimeParser and defines the
*   doInBackground-method which is executed when passing
*   the waiting time query url into the execute method of the AsyncTask-class
* */
public class MonthsWaitingTimeParser extends WaitingTimeParser{

    public MonthsWaitingTimeParser(AsyncCallback asyncCallback) {
        super(asyncCallback);
    }

    /*  Gets the document for the url and checks if it's the swedish or english url
    *   and uses the different parseSimple-methods of the SimpleWaitingTimeParser-class
    *   depending on the language
    *
    *   if successful it will return a WaitingTime-object, otherwise it will return null
    * */
    @Override
    protected Object doInBackground(String... strings) {
        try {
            Document document = getDocumentForURL(strings[0]);
            Query.SwedishOrEnglishQuery swedishOrEnglishQuery = swedishOrEnglishQuery(document);
            WaitingTime waitingTime;
            if (swedishOrEnglishQuery.equals(Query.SwedishOrEnglishQuery.SWEDISH)) {
                waitingTime = SimpleWaitingTimeParser.parseSimpleSwedish(strings[0], document);
            } else {
                waitingTime = SimpleWaitingTimeParser.parseSimpleEnglish(strings[0], document);
            }
            return waitingTime;
        } catch (IOException | ParserException e) {
            e.printStackTrace();
            return null;
        }
    }


}
