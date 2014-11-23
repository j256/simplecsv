package com.j256.simplecsv.converter;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class DateConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws ParseException {
		DateConverter converter = new DateConverter();
		testConverter(converter, makeDate(2014, 11, 23));
		testConverter(converter, makeDate(2014, 1, 1));
		testConverter(converter, makeDate(2014, 1, 30));
		testConverter(converter, makeDate(2014, 12, 31));
		testConverter(converter, null);

		converter = new DateConverter();
		converter.configure("yyyyMMdd", 0, null);
		testConverter(converter, makeDate(2014, 11, 23));
		testConverter(converter, makeDate(2014, 1, 1));
		testConverter(converter, makeDate(2014, 1, 30));
		testConverter(converter, makeDate(2014, 12, 31));
		testConverter(converter, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidPattern() {
		DateConverter converter = new DateConverter();
		converter.configure("notagoodpattern", 0, null);
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
