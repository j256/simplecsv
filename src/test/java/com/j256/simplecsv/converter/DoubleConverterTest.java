package com.j256.simplecsv.converter;

import java.text.ParseException;

import org.junit.Test;

public class DoubleConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		DoubleConverter converter = new DoubleConverter();
		testNumbers(converter);
		testConverter(converter, Double.MIN_VALUE);
		testConverter(converter, Double.MAX_VALUE);
	}

	@Test
	public void testFormat() throws Exception {
		DoubleConverter converter = new DoubleConverter();
		converter.configure("###,##0.0################", 0, null);
		testNumbers(converter);
	}

	private void testNumbers(DoubleConverter converter) throws ParseException {
		testConverter(converter, -1.0);
		testConverter(converter, 0.11);
		testConverter(converter, 1.2);
		testConverter(converter, 1000.22245678);
		testConverter(converter, -1000.22245678);
	}
}
