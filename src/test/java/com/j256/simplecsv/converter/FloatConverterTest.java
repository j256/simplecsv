package com.j256.simplecsv.converter;

import org.junit.Test;

public class FloatConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		FloatConverter converter = new FloatConverter();
		testConverter(converter, -1.0F);
		testConverter(converter, 0.11F);
		testConverter(converter, 1.2F);
		testConverter(converter, Float.MIN_VALUE);
		testConverter(converter, Float.MAX_VALUE);
	}

	@Test
	public void testFormat() throws Exception {
		FloatConverter converter = new FloatConverter();
		converter.configure("###,##0.0##", 0, null);
		testConverter(converter, -1.0F);
		testConverter(converter, 0.11F);
		testConverter(converter, 1.2F);
		testConverter(converter, Float.MIN_VALUE);
		testConverter(converter, Float.MAX_VALUE);
	}
}
