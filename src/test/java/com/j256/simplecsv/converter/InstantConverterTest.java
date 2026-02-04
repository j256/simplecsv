package com.j256.simplecsv.converter;

import org.junit.Test;

import java.text.ParseException;
import java.time.Instant;
import java.util.Calendar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InstantConverterTest extends AbstractConverterTest {

    @Test
    public void testStuff() throws ParseException {
        InstantConverter converter = InstantConverter.getSingleton();
        testConverter(converter, Instant.class, null, 0, makeDate(2014, 11, 23));
        testConverter(converter, Instant.class, null, 0, makeDate(2014, 1, 1));
        testConverter(converter, Instant.class, null, 0, makeDate(2014, 1, 30));
        testConverter(converter, Instant.class, null, 0, makeDate(2014, 12, 31));
        testConverter(converter, Instant.class, null, 0, null);

        converter = InstantConverter.getSingleton();
        testConverter(converter, Instant.class, "yyyyMMdd", 0, makeDate(2014, 11, 23));
        testConverter(converter, Instant.class, "yyyyMMdd", 0, makeDate(2014, 1, 1));
        testConverter(converter, Instant.class, "yyyyMMdd", 0, makeDate(2014, 1, 30));
        testConverter(converter, Instant.class, "yyyyMMdd", 0, makeDate(2014, 12, 31));
        testConverter(converter, Instant.class, "yyyyMMdd", 0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPattern() {
        InstantConverter converter = InstantConverter.getSingleton();
        converter.configure("notagoodpattern", 0, null);
    }

    @Test
    public void testConverage() {
        InstantConverter converter = InstantConverter.getSingleton();
        assertTrue(converter.isNeedsQuotes(null));
        assertFalse(converter.isAlwaysTrimInput());
    }

    private Instant makeDate(int year, int month, int day) {
        Calendar calender = Calendar.getInstance();
        calender.set(Calendar.YEAR, year);
        calender.set(Calendar.MONTH, month - 1);
        calender.set(Calendar.DAY_OF_MONTH, day);
        calender.set(Calendar.HOUR, 0);
        calender.set(Calendar.MINUTE, 0);
        calender.set(Calendar.SECOND, 0);
        calender.set(Calendar.MILLISECOND, 0);
        calender.set(Calendar.AM_PM, 0);
        return calender.getTime().toInstant();
    }
}
