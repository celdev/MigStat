package com.celdev.migstat;

import com.celdev.migstat.controller.utils.DateUtils;
import com.celdev.migstat.model.ParserException;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;
public class DateUtilTests {

    @Test
    public void testStringCalendarConversion() throws Exception{
        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, 0, 1);
        Calendar actual = DateUtils.dateStringToCalendar("2016-01-01");
        assertEquals(true, assertFields(calendar, actual));
        calendar.set(2004, 2, 2);
        actual = DateUtils.dateStringToCalendar("2004-03-2");
        assertEquals(true, assertFields(calendar, actual));
        assertEquals(null, returnNullIfException("2015-13-31"));
        assertEquals(null, returnNullIfException("2013-02-31"));
        assertEquals(null, returnNullIfException("2014-02-29"));
        assertNotEquals(null, returnNullIfException("2016-02-29"));

        assertEquals(true, DateUtils.isValidDate(2012,11,30));
        assertEquals(false, DateUtils.isValidDate(2012,10,31));
    }

    @Test
    public void testUpdatedComparison() throws Exception {
        assertEquals(1, DateUtils.compareDateStrings("2016-01-01","2016-01-02"));
        assertEquals(1, DateUtils.compareDateStrings("2015-02-01","2016-01-02"));
        assertEquals(0, DateUtils.compareDateStrings("2016-01-01","2016-01-01"));
        assertEquals(-1, DateUtils.compareDateStrings("2018-01-01","2013-01-02"));
        assertEquals(null, returnNullIfException("2018-00-01", "2019-1-2"));
        assertEquals(0, DateUtils.compareDateStrings("2011-01-1","2011-1-01"));

        assertEquals(true, DateUtils.isNewerDate("2016-01-01", "2016-01-02"));
    }

    private Object returnNullIfException(String first, String second) {
        try {
            return DateUtils.compareDateStrings(first, second);
        } catch (ParserException e) {
            return null;
        }
    }

    private Calendar returnNullIfException(String dateString) {
        try {
            return DateUtils.dateStringToCalendar(dateString);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean assertFields(Calendar calendar, Calendar actual) {
        return calendar.get(Calendar.YEAR) == actual.get(Calendar.YEAR) &&
        calendar.get(Calendar.MONTH) == actual.get(Calendar.MONTH) &&
        calendar.get(Calendar.DATE) == actual.get(Calendar.DATE);
    }

}
