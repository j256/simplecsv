package com.j256.simplecsv.converter;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.junit.Test;

public class ByteConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		ByteConverter converter = ByteConverter.getSingleton();
		DecimalFormat configInfo = converter.configure(null, 0, null);
		testNumbers(converter, configInfo);
	}

	@Test
	public void testFormat() throws Exception {
		ByteConverter converter = ByteConverter.getSingleton();
		DecimalFormat configInfo = converter.configure("###,##0", 0, null);
		testNumbers(converter, configInfo);
	}

	private void testNumbers(ByteConverter converter, DecimalFormat configInfo) throws ParseException {
		testConverter(converter, configInfo, (byte) -1);
		testConverter(converter, configInfo, (byte) 0);
		testConverter(converter, configInfo, (byte) 1);
		testConverter(converter, configInfo, Byte.MIN_VALUE);
		testConverter(converter, configInfo, Byte.MAX_VALUE);
		testConverter(converter, configInfo, null);
	}
}
