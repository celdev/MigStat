package com.celdev.migstat;

import com.celdev.migstat.model.StatusType;

import org.jsoup.Jsoup;
import org.junit.Test;

import static com.celdev.migstat.controller.parser.SimpleCaseStatusParser.MIGRATIONSVERKET_MY_PAGE_URL;
import static com.celdev.migstat.controller.parser.SimpleCaseStatusParser.extractStatus;
import static org.junit.Assert.*;

public class StatusParserTest {

    public int[] waitingTimeNumbers = {};

    @Test
    public void testStatusParser() throws Exception {
        assertEquals(StatusType.WAITING,extractStatus(Jsoup.connect(MIGRATIONSVERKET_MY_PAGE_URL + "2&q=" + waitingTimeNumbers[0]).get()));
    }
}
