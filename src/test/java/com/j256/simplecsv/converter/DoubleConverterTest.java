package com.j256.simplecsv.converter;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.junit.Test;

public class DoubleConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		DoubleConverter converter = DoubleConverter.getSingleton();
		DecimalFormat configInfo = converter.configure(null, 0, null);
		testNumbers(converter, configInfo);
		testConverter(converter, configInfo, Double.MIN_VALUE);
		testConverter(converter, configInfo, Double.MAX_VALUE);
	}

	@Test
	public void testFormat() throws Exception {
		DoubleConverter converter = DoubleConverter.getSingleton();
		DecimalFormat configInfo = converter.configure("###,##0.0################", 0, null);
		testNumbers(converter, configInfo);
	}

	private void testNumbers(DoubleConverter converter, DecimalFormat configInfo) throws ParseException {
		testConverter(converter, configInfo, -1.0);
		testConverter(converter, configInfo, 0.11);
		testConverter(converter, configInfo, 1.2);
		testConverter(converter, configInfo, 1000.22245678);
		testConverter(converter, configInfo, -1000.22245678);
	}
}
