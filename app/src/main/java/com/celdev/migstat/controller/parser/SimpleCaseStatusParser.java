package com.celdev.migstat.controller.parser;

import android.os.AsyncTask;
import android.util.Log;

import com.celdev.migstat.MainActivity;
import com.celdev.migstat.controller.utils.DateUtils;
import com.celdev.migstat.model.ApplicationDate;
import com.celdev.migstat.model.ApplicationNumber;
import com.celdev.migstat.model.ParserException;
import com.celdev.migstat.model.StatusType;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


/*  This class is responsible for extracting the status and application date of an application using
*   the applications check/case number.
*
*   The extraction is done using an AsyncTask
*   When the AsyncTask is created the object that will handle the result is
*   passed as a parameter to the custom AsyncTask class constructor
* */

public class SimpleCaseStatusParser {

    public static final String MIGRATIONSVERKET_MY_PAGE_URL = "https://www.migrationsverket.se/Kontakta-oss/Kontrollera-din-ansokan/Utan-inloggning.html?typenr=";

    public static final String DIV_CLASS_APPLICATIONDATE = ".sv-column-4 > p.normal";
    public static final String LIST_CLASS_STATUS = ".statusbar li";
    public static final String LIST_ELEMENT_ACTIVE_CLASS = "active";

    /*  The status is extracted from the document by checking which li-element has the class active
    *   example
    *   <ul class="statusbar">
           <li><i class="fa fa-chevron-right"></i>Ansökan inkommen</li>
           <li class="active"><i class="fa fa-chevron-right"></i>I väntan på beslut</li>
           <li><i class="fa fa-chevron-right"></i>Beslut fattat</li>
        </ul>
    *   menas that the second (index = 1) element is active and
    *   the StatusType with that number (=1) is WAITING
    *
    *   Will throw an exception if a status can't be extracted.
    * */
    public static StatusType extractStatus(Document document) throws ParserException {
        Elements elements = document.select(LIST_CLASS_STATUS);
        if (elements.size() != 3) {
            throw new ParserException("Extracted list has wrong amount of elements inside");
        }
        StatusType statusType = null;
        Log.d(MainActivity.LOG_KEY, elements.outerHtml());
        for(int i = 0; i <= 2; i++) {
            Log.d(MainActivity.LOG_KEY, elements.get(i).outerHtml());
            if (elements.get(i).hasClass(LIST_ELEMENT_ACTIVE_CLASS)) {
                statusType = StatusType.statusNumberToStatusType(i);
            }
        }
        if (statusType == null) {
            throw new ParserException("Couldn't parse current status");
        }
        return statusType;
    }

    /*  Extracts the date of the application from the document and creates a
    *   ApplicationDate-object with that time. */
    private static ApplicationDate extractApplicationDate(Document document) throws ParserException{
        Elements elements = document.select(DIV_CLASS_APPLICATIONDATE);
        return new ApplicationDate(DateUtils.dateStringToCalendar(elements.get(0).html().trim()).getTimeInMillis());
    }


    /*  This AsyncTask will extract the status and application date of the application
    *   with the application number (and application number type) passed as a parameter
    *   in the execute-method.
    *   if something can't be parsed an exception will be thrown and null will be returned.
    *
    *   the AsyncTaskResultReceiver passed as a parameter in the Worker constructor
    *   will handle the object passed into onPostExecute
    * */
    public static class Worker extends AsyncTask<ApplicationNumber, Void, StatusAndDate> {

        private AsyncTaskResultReceiver asyncTaskResultReceiver;

        public Worker(AsyncTaskResultReceiver asyncTaskResultReceiver) {
            this.asyncTaskResultReceiver = asyncTaskResultReceiver;
        }

        @Override
        protected StatusAndDate doInBackground(ApplicationNumber... applicationNumbers) {
            int applicationNumber = applicationNumbers[0].getApplicationNumber();
            int applicationNumberType = applicationNumbers[0].getApplicationNumberType().getMigrationsverketQueryNumber();
            try {
                Document document = Jsoup.connect(MIGRATIONSVERKET_MY_PAGE_URL + applicationNumberType + "&q=" + applicationNumber).get();
                StatusType applicationStatus = extractStatus(document);
                ApplicationDate applicationDate = extractApplicationDate(document);
                return new StatusAndDate(applicationStatus, applicationDate, applicationNumbers[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(StatusAndDate statusAndDate) {
            Log.d(MainActivity.LOG_KEY, "Parsed status and date: " + statusAndDate);
            asyncTaskResultReceiver.receiveResult(statusAndDate);
        }
    }


    /*  Wrapper class for wrapping the status type, application date
    *   and the application number.
    *
    *   This will be used to update/create the Application-object
    * */
    public static class StatusAndDate{
        private StatusType statusType;
        private ApplicationDate applicationDate;
        private ApplicationNumber applicationNumber;

        public StatusAndDate(StatusType statusType, ApplicationDate applicationDate, ApplicationNumber applicationNumber) {
            this.statusType = statusType;
            this.applicationDate = applicationDate;
            this.applicationNumber = applicationNumber;
        }

        public ApplicationNumber getApplicationNumber() {
            return applicationNumber;
        }

        public StatusType getStatusType() {
            return statusType;
        }

        public ApplicationDate getApplicationDate() {
            return applicationDate;
        }

        @Override
        public String toString() {
            return "StatusAndDate{" +
                    "statusType=" + statusType +
                    ", applicationDate=" + applicationDate +
                    ", applicationNumber=" + applicationNumber +
                    '}';
        }
    }


}