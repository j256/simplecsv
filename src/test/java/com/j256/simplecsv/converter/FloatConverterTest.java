package com.j256.simplecsv.converter;

import java.text.ParseException;

import org.junit.Test;

public class FloatConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		FloatConverter converter = FloatConverter.getSingleton();
		testNumbers(converter, null);
		testConverter(converter, Float.class, null, 0, Float.MIN_VALUE);
		testConverter(converter, Float.class, null, 0, Float.MAX_VALUE);
	}

	@Test
	public void testFormat() throws Exception {
		FloatConverter converter = FloatConverter.getSingleton();
		testNumbers(converter, "###,##0.0################");
	}

	private void testNumbers(FloatConverter converter, String format) throws ParseException {
		testConverter(converter, Float.class, format, 0, -1.0F);
		testConverter(converter, Float.class, format, 0, 0.11F);
		testConverter(converter, Float.class, format, 0, 1.2F);
		testConverter(converter, Float.class, format, 0, 1000.222F);
		testConverter(converter, Float.class, format, 0, -1000.222F);
	}
}
