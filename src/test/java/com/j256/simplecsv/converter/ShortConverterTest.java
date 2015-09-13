package com.j256.simplecsv.converter;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.junit.Test;

public class ShortConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		ShortConverter converter = ShortConverter.getSingleton();
		DecimalFormat configInfo = converter.configure(null, 0, null);
		testNumbers(converter, configInfo);
	}

	@Test
	public void testFormat() throws Exception {
		ShortConverter converter = ShortConverter.getSingleton();
		DecimalFormat configInfo = converter.configure("###,##0", 0, null);
		testNumbers(converter, configInfo);
	}

	private void testNumbers(ShortConverter converter, DecimalFormat configInfo) throws ParseException {
		testConverter(converter, configInfo, (short) -1);
		testConverter(converter, configInfo, (short) 0);
		testConverter(converter, configInfo, (short) 1);
		testConverter(converter, configInfo, Short.MIN_VALUE);
		testConverter(converter, configInfo, Short.MAX_VALUE);
		testConverter(converter, configInfo, null);
	}
}
