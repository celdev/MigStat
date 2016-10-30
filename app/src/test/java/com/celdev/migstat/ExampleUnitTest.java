package com.celdev.migstat;

import com.celdev.migstat.controller.parser.Parser;

import org.jsoup.Jsoup;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testSimpleParser() throws Exception {

        String[] urls = {
                "http://www.migrationsverket.se/4.2d998ffc151ac3871595cd4/10.2d998ffc151ac3871595cdf.js?history=q0:1,q1:1,q5:1,q6:1,q3:1TH,q2:1,q4:2,q8:2&_=1477846796545",
                "http://www.migrationsverket.se/4.2d998ffc151ac3871595cd4/10.2d998ffc151ac3871595cdf.js?history=q0:3,q1:2,q20:2,q2:1&_=1477846796547",
                "http://www.migrationsverket.se/4.2d998ffc151ac3871595b01/10.2d998ffc151ac3871595b0c.js?history=q0:1,q1:1,q5:1,q6:1,q3:1TH,q2:1,q4:2,q8:2&_=1477846214365"
        };

        double delta = 0.01;

        assertEquals(14.5d, Parser.parseSimpleEnglish(Jsoup.connect(urls[0]).get()).getAverage(), delta);
        assertEquals(1.5d, Parser.parseSimpleEnglish(Jsoup.connect(urls[1]).get()).getAverage(), delta);
        assertEquals(14.5d, Parser.parseSimpleSwedish(Jsoup.connect(urls[2]).get()).getAverage(), delta);
    }
}