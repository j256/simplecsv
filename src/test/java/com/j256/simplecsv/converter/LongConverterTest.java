package com.j256.simplecsv.converter;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.junit.Test;

public class LongConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		LongConverter converter = LongConverter.getSingleton();
		DecimalFormat configInfo = converter.configure(null, 0, null);
		testNumbers(converter, configInfo);
	}

	@Test
	public void testFormat() throws Exception {
		LongConverter converter = LongConverter.getSingleton();
		DecimalFormat configInfo = converter.configure("###,##0", 0, null);
		testNumbers(converter, configInfo);
	}

	private void testNumbers(LongConverter converter, DecimalFormat configInfo) throws ParseException {
		testConverter(converter, configInfo, -1L);
		testConverter(converter, configInfo, 0L);
		testConverter(converter, configInfo, 1L);
		testConverter(converter, configInfo, Long.MIN_VALUE);
		testConverter(converter, configInfo, Long.MAX_VALUE);
		testConverter(converter, configInfo, null);
	}
}
