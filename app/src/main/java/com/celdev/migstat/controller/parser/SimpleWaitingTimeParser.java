package com.celdev.migstat.controller.parser;

import com.celdev.migstat.model.ParserException;
import com.celdev.migstat.model.WaitingTime;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/*  This waiting time parser works on the simple waiting times
*   which will produce a waiting time containing two months (i.e. 13-15 months)
*   and also contains an "updated at" value
* */
public class SimpleWaitingTimeParser {

    public static final String DIV_CLASS_CONTENT = ".sv-script-portlet.sv-portlet";

    public static final String SWEDISH_BEFORE_MONTHS = "Tid till beslut: ";
    public static final String ENGLISH_BEFORE_MONTHS = "Time to a decision: ";
    public static final String SWEDISH_BEFORE_UPDATED_AT = "Statistik uppdaterades: ";
    public static final String ENGLISH_BEFORE_UPDATED_AT = "Updated: ";
    public static final String SWEDISH_AFTER_MONTHS = "månader.";
    public static final String ENGLISH_AFTER_MONTHS = "months.";

    /*  The waiting time url is english so use the english words to extract the values
    *   returns a waiting time object if successful
    * */
    public static WaitingTime parseSimpleEnglish(String query, Document document) throws ParserException{
        return parseDocumentGetDatesPart(query, document, ENGLISH_BEFORE_MONTHS, ENGLISH_BEFORE_UPDATED_AT, ENGLISH_AFTER_MONTHS);
    }


    /*  The waiting time url is swedish so use the swedish words to extract the values
    *   returns a waiting time object if successful
    * */
    public static WaitingTime parseSimpleSwedish(String query, Document document) throws ParserException{
        return parseDocumentGetDatesPart(query, document, SWEDISH_BEFORE_MONTHS, SWEDISH_BEFORE_UPDATED_AT, SWEDISH_AFTER_MONTHS);
    }


    /*  Extracts the element containing the waiting time and updated at time
    *   example:
    *   <div class="sv-script-portlet sv-portlet" id="svid12_2d998ffc151ac3871595ce1">
    *       <div id="ModulHandlaggningstid"><!-- Modul - Handläggningstid -->
    *       </div>
    *       Time to a decision: 14-15 months.
    *       <span class="resultUpdated">Updated: 2016-10-25</span>
    *   </div>
    *   1. removes the first inner div from the outer div
    *   2. saves the span-element and removes it from the outer div
    *   now the outer div will only contain "Time to a decision: 14-15 months."
    *   3. extract the months-numbers
    *   4. use these to create a WaitingTime
    * */
    private static WaitingTime parseDocumentGetDatesPart(String query, Document document, String beforeMonths, String beforeUpdatedAt, String afterMonths) throws ParserException {
        try {
            Element element = document.select(DIV_CLASS_CONTENT).first();
            element.child(0).remove();
            Element updatedElement = element.child(0);
            element.child(0).remove();
            String monthsLine = element.html().replace(beforeMonths, "").replace(afterMonths, "").trim();
            String updatedLine = updatedElement.html().replace(beforeUpdatedAt, "").trim();
            int[] months = extractMonths(monthsLine);
            return new WaitingTime(months[0], months[1], updatedLine, query);
        } catch (NumberFormatException e) {
            throw new ParserException("Error: Couldn't convert parsed string to number");
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ParserException("Error: ArrayOutOfBoundsException after split the parsed months-string");
        } catch (Exception e) {
            throw new ParserException(e.getMessage());
        }
    }

    /*  returns an integer array containing the low month and high month of the string passed in
    *   (i.e.) "13-15" becomes [13,15]
    * */
    public static int[] extractMonths(String monthsLine) throws NumberFormatException, ArrayIndexOutOfBoundsException{
        String[] monthsSTR = monthsLine.split("-");
        return new int[]{Integer.valueOf(monthsSTR[0]), Integer.valueOf(monthsSTR[1])};
    }

}
