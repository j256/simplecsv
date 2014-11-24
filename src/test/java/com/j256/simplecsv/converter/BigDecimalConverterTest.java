package com.j256.simplecsv.converter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

import org.junit.Test;

public class BigDecimalConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws ParseException {
		BigDecimalConverter converter = new BigDecimalConverter();
		DecimalFormat configInfo = converter.configure(null, 0, null);
		testNumbers(converter, configInfo);
	}

	@Test
	public void testFormat() throws Exception {
		BigDecimalConverter converter = new BigDecimalConverter();
		DecimalFormat configInfo =
				converter.configure(
						"###,##0.0##########################################################################################",
						0, null);
		testNumbers(converter, configInfo);
	}

	private void testNumbers(BigDecimalConverter converter, DecimalFormat configInfo) throws ParseException {
		testConverter(converter, configInfo, new BigDecimal("-1.0"));
		testConverter(converter, configInfo, new BigDecimal("0.0"));
		testConverter(converter, configInfo, new BigDecimal("1.0"));
		testConverter(converter, configInfo, null);

		BigDecimal bigDec = new BigDecimal(Long.toString(Long.MIN_VALUE) + ".0");
		testConverter(converter, configInfo, bigDec);
		BigDecimal smaller = bigDec.add(bigDec);
		testConverter(converter, configInfo, smaller);
		String decimalStr = "0.7029384290482390483482308230489230482390482304823048230482304823048230482034";
		BigDecimal decimal = new BigDecimal(decimalStr);
		smaller = bigDec.subtract(decimal);
		testConverter(converter, configInfo, smaller);

		bigDec = new BigDecimal(Long.toString(Long.MAX_VALUE) + ".0");
		testConverter(converter, configInfo, bigDec);
		BigDecimal larger = bigDec.add(bigDec);
		testConverter(converter, configInfo, larger);
		larger = bigDec.add(decimal);
		testConverter(converter, configInfo, larger);
	}
}
