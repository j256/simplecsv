package com.j256.simplecsv.converter;

import org.junit.Test;

public class ByteConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		ByteConverter converter = new ByteConverter();
		converter.configure(null, 0, null);

		testConverter(converter, (byte) -1);
		testConverter(converter, (byte) 0);
		testConverter(converter, (byte) 1);
		testConverter(converter, Byte.MIN_VALUE);
		testConverter(converter, Byte.MAX_VALUE);
		testConverter(converter, null);
	}

	@Test
	public void testFormat() throws Exception {
		ByteConverter converter = new ByteConverter();
		converter.configure("###,##0", 0, null);
		testConverter(converter, (byte) -1);
		testConverter(converter, (byte) 0);
		testConverter(converter, (byte) 1);
		testConverter(converter, Byte.MIN_VALUE);
		testConverter(converter, Byte.MAX_VALUE);
		testConverter(converter, null);
	}
}
