package com.j256.simplecsv.converter;

import java.text.ParseException;

import org.junit.Test;

public class ByteConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		ByteConverter converter = ByteConverter.getSingleton();
		testNumbers(converter, null);
	}

	@Test
	public void testFormat() throws Exception {
		ByteConverter converter = ByteConverter.getSingleton();
		testNumbers(converter, "###,##0");
	}

	private void testNumbers(ByteConverter converter, String format) throws ParseException {
		testConverter(converter, Byte.class, format, 0, (byte) -1);
		testConverter(converter, Byte.class, format, 0, (byte) 0);
		testConverter(converter, Byte.class, format, 0, (byte) 1);
		testConverter(converter, Byte.class, format, 0, Byte.MIN_VALUE);
		testConverter(converter, Byte.class, format, 0, Byte.MAX_VALUE);
		testConverter(converter, Byte.class, format, 0, null);
	}
}
