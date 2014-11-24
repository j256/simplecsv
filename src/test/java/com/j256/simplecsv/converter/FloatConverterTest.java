package com.j256.simplecsv.converter;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.junit.Test;

public class FloatConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		FloatConverter converter = new FloatConverter();
		DecimalFormat configInfo = converter.configure(null, 0, null);
		testNumbers(converter, configInfo);
		testConverter(converter, configInfo, Float.MIN_VALUE);
		testConverter(converter, configInfo, Float.MAX_VALUE);
	}

	@Test
	public void testFormat() throws Exception {
		FloatConverter converter = new FloatConverter();
		DecimalFormat configInfo = converter.configure("###,##0.0################", 0, null);
		testNumbers(converter, configInfo);
	}

	private void testNumbers(FloatConverter converter, DecimalFormat configInfo) throws ParseException {
		testConverter(converter, configInfo, -1.0F);
		testConverter(converter, configInfo, 0.11F);
		testConverter(converter, configInfo, 1.2F);
		testConverter(converter, configInfo, 1000.222F);
		testConverter(converter, configInfo, -1000.222F);
	}
}
