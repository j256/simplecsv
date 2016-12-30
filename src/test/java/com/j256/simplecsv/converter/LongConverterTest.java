package com.j256.simplecsv.converter;

import java.text.ParseException;

import org.junit.Test;

public class LongConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		LongConverter converter = LongConverter.getSingleton();
		testNumbers(converter, null);
	}

	@Test
	public void testFormat() throws Exception {
		LongConverter converter = LongConverter.getSingleton();
		testNumbers(converter, "###,##0");
	}

	private void testNumbers(LongConverter converter, String format) throws ParseException {
		testConverter(converter, Long.class, format, 0, -1L);
		testConverter(converter, Long.class, format, 0, 0L);
		testConverter(converter, Long.class, format, 0, 1L);
		testConverter(converter, Long.class, format, 0, Long.MIN_VALUE);
		testConverter(converter, Long.class, format, 0, Long.MAX_VALUE);
		testConverter(converter, Long.class, format, 0, null);
	}
}
