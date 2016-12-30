package com.j256.simplecsv.converter;

import java.text.ParseException;

import org.junit.Test;

public class DoubleConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		DoubleConverter converter = DoubleConverter.getSingleton();
		testNumbers(converter, null);
		testConverter(converter, Double.class, null, 0, Double.MIN_VALUE);
		testConverter(converter, Double.class, null, 0, Double.MAX_VALUE);
	}

	@Test
	public void testFormat() throws Exception {
		DoubleConverter converter = DoubleConverter.getSingleton();
		testNumbers(converter, "###,##0.0################");
	}

	private void testNumbers(DoubleConverter converter, String format) throws ParseException {
		testConverter(converter, Double.class, null, 0, -1.0);
		testConverter(converter, Double.class, null, 0, 0.11);
		testConverter(converter, Double.class, null, 0, 1.2);
		testConverter(converter, Double.class, null, 0, 1000.22245678);
		testConverter(converter, Double.class, null, 0, -1000.22245678);
	}
}
