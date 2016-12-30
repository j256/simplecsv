package com.j256.simplecsv.converter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class DateConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws ParseException {
		DateConverter converter = DateConverter.getSingleton();
		testConverter(converter, Date.class, null, 0, makeDate(2014, 11, 23));
		testConverter(converter, Date.class, null, 0, makeDate(2014, 1, 1));
		testConverter(converter, Date.class, null, 0, makeDate(2014, 1, 30));
		testConverter(converter, Date.class, null, 0, makeDate(2014, 12, 31));
		testConverter(converter, Date.class, null, 0, null);

		converter = DateConverter.getSingleton();
		testConverter(converter, Date.class, "yyyyMMdd", 0, makeDate(2014, 11, 23));
		testConverter(converter, Date.class, "yyyyMMdd", 0, makeDate(2014, 1, 1));
		testConverter(converter, Date.class, "yyyyMMdd", 0, makeDate(2014, 1, 30));
		testConverter(converter, Date.class, "yyyyMMdd", 0, makeDate(2014, 12, 31));
		testConverter(converter, Date.class, "yyyyMMdd", 0, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidPattern() {
		DateConverter converter = DateConverter.getSingleton();
		converter.configure("notagoodpattern", 0, null);
	}

	@Test
	public void testConverage() {
		DateConverter converter = DateConverter.getSingleton();
		assertTrue(converter.isNeedsQuotes(null));
		assertFalse(converter.isAlwaysTrimInput());
	}

	private Date makeDate(int year, int month, int day) {
		Calendar calender = Calendar.getInstance();
		calender.set(Calendar.YEAR, year);
		calender.set(Calendar.MONTH, month - 1);
		calender.set(Calendar.DAY_OF_MONTH, day);
		calender.set(Calendar.HOUR, 0);
		calender.set(Calendar.MINUTE, 0);
		calender.set(Calendar.SECOND, 0);
		calender.set(Calendar.MILLISECOND, 0);
		calender.set(Calendar.AM_PM, 0);
		return calender.getTime();
	}
}
