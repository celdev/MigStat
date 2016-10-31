package com.celdev.migstat.controller.parser;

import com.celdev.migstat.controller.utils.DateUtils;
import com.celdev.migstat.model.ApplicationDate;
import com.celdev.migstat.model.ParserException;
import com.celdev.migstat.model.StatusType;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class SimpleCaseStatusParser {

    public static final String MIGRATIONSVERKET_MY_PAGE_URL = "http://www.migrationsverket.se/Kontakta-oss/Kontrollera-din-ansokan/Utan-inloggning.html?typenr=";

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
        for(int i = 0; i < 2; i++) {
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





}