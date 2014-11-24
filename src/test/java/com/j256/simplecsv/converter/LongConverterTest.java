package com.j256.simplecsv.converter;

import java.text.ParseException;

import org.junit.Test;

public class LongConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		LongConverter converter = new LongConverter();
		converter.configure(null, 0, null);

		testNumbers(converter);
	}

	@Test
	public void testFormat() throws Exception {
		LongConverter converter = new LongConverter();
		converter.configure("###,##0", 0, null);
		testNumbers(converter);
	}

	private void testNumbers(LongConverter converter) throws ParseException {
		testConverter(converter, -1L);
		testConverter(converter, 0L);
		testConverter(converter, 1L);
		testConverter(converter, Long.MIN_VALUE);
		testConverter(converter, Long.MAX_VALUE);
		testConverter(converter, null);
	}
}
