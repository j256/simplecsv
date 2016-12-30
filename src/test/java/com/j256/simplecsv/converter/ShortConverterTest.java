package com.j256.simplecsv.converter;

import java.text.ParseException;

import org.junit.Test;

public class ShortConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		ShortConverter converter = ShortConverter.getSingleton();
		testNumbers(converter, null);
	}

	@Test
	public void testFormat() throws Exception {
		ShortConverter converter = ShortConverter.getSingleton();
		testNumbers(converter, "###,##0");
	}

	private void testNumbers(ShortConverter converter, String format) throws ParseException {
		testConverter(converter, Short.class, format, 0, (short) -1);
		testConverter(converter, Short.class, format, 0, (short) 0);
		testConverter(converter, Short.class, format, 0, (short) 1);
		testConverter(converter, Short.class, format, 0, Short.MIN_VALUE);
		testConverter(converter, Short.class, format, 0, Short.MAX_VALUE);
		testConverter(converter, Short.class, format, 0, null);
	}
}
