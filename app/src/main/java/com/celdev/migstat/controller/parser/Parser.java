package com.celdev.migstat.controller.parser;

import com.celdev.migstat.model.WaitingTime;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Parser {

    public static final String DIV_CLASS_CONTENT = ".sv-script-portlet.sv-portlet";

    public static final String SWEDISH_BEFORE_MONTHS = "Tid till beslut: ";
    public static final String ENGLISH_BEFORE_MONTHS = "Time to a decision: ";
    public static final String SWEDISH_BEFORE_UPDATED_AT = "Statistik uppdaterades: ";
    public static final String ENGLISH_BEFORE_UPDATED_AT = "Updated: ";
    public static final String SWEDISH_AFTER_MONTHS = "månader.";
    public static final String ENGLISH_AFTER_MONTHS = "months.";


    public static WaitingTime parseSimpleEnglish(Document document) {
        return parseDocumentGetDatesPart(document, ENGLISH_BEFORE_MONTHS, ENGLISH_BEFORE_UPDATED_AT, ENGLISH_AFTER_MONTHS);
    }

    public static WaitingTime parseSimpleSwedish(Document document) {
        return parseDocumentGetDatesPart(document, SWEDISH_BEFORE_MONTHS, SWEDISH_BEFORE_UPDATED_AT, SWEDISH_AFTER_MONTHS);
    }


    public static WaitingTime parseDocumentGetDatesPart(Document document, String beforeMonths, String beforeUpdatedAt, String afterMonths) {
        Element element = document.select(DIV_CLASS_CONTENT).first();
        element.child(0).remove();
        Element updatedElement = element.child(0);
        element.child(0).remove();
        String monthsLine = element.html().replace(beforeMonths, "").replace(afterMonths, "").trim();
        String updatedLine = updatedElement.html().replace(beforeUpdatedAt, "").trim();
        int[] months = extractMonths(monthsLine);
        return new WaitingTime(months[0], months[1], updatedLine);
    }

    public static int[] extractMonths(String monthsLine) {
        String[] monthsSTR = monthsLine.split("-");
        return new int[]{Integer.valueOf(monthsSTR[0]), Integer.valueOf(monthsSTR[1])};
    }

}
