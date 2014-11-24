package com.j256.simplecsv.converter;

import java.text.ParseException;

import org.junit.Test;

public class ShortConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		ShortConverter converter = new ShortConverter();
		converter.configure(null, 0, null);
		testNumbers(converter);
	}

	@Test
	public void testFormat() throws Exception {
		ShortConverter converter = new ShortConverter();
		converter.configure("###,##0", 0, null);
		testNumbers(converter);
	}

	private void testNumbers(ShortConverter converter) throws ParseException {
		testConverter(converter, (short) -1);
		testConverter(converter, (short) 0);
		testConverter(converter, (short) 1);
		testConverter(converter, Short.MIN_VALUE);
		testConverter(converter, Short.MAX_VALUE);
		testConverter(converter, null);
	}
}
