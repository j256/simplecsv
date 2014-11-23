package com.j256.simplecsv.converter;

import org.junit.Test;

public class IntegerConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		IntegerConverter converter = new IntegerConverter();

		testConverter(converter, -1);
		testConverter(converter, 0);
		testConverter(converter, 1);
		testConverter(converter, Integer.MIN_VALUE);
		testConverter(converter, Integer.MAX_VALUE);
	}
	
	@Test
	public void testFormat() throws Exception {
		IntegerConverter converter = new IntegerConverter();
		converter.configure("###,##0", 0, null);
		testConverter(converter, -1);
		testConverter(converter, 0);
		testConverter(converter, 1);
		testConverter(converter, Integer.MIN_VALUE);
		testConverter(converter, Integer.MAX_VALUE);
	}
}
