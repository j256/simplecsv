package com.j256.simplecsv.converter;

import java.math.BigDecimal;
import java.text.ParseException;

import org.junit.Test;

public class BigDecimalConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws ParseException {
		BigDecimalConverter converter = new BigDecimalConverter();
		converter.configure(null, 0, null);
		testNumbers(converter);
	}

	@Test
	public void testFormat() throws Exception {
		BigDecimalConverter converter = new BigDecimalConverter();
		converter.configure(
				"###,##0.0##########################################################################################",
				0, null);
		testNumbers(converter);
	}

	private void testNumbers(BigDecimalConverter converter) throws ParseException {
		testConverter(converter, new BigDecimal("-1.0"));
		testConverter(converter, new BigDecimal("0.0"));
		testConverter(converter, new BigDecimal("1.0"));
		testConverter(converter, null);

		BigDecimal bigDec = new BigDecimal(Long.toString(Long.MIN_VALUE) + ".0");
		testConverter(converter, bigDec);
		BigDecimal smaller = bigDec.add(bigDec);
		testConverter(converter, smaller);
		String decimalStr = "0.7029384290482390483482308230489230482390482304823048230482304823048230482034";
		BigDecimal decimal = new BigDecimal(decimalStr);
		smaller = bigDec.subtract(decimal);
		testConverter(converter, smaller);

		bigDec = new BigDecimal(Long.toString(Long.MAX_VALUE) + ".0");
		testConverter(converter, bigDec);
		BigDecimal larger = bigDec.add(bigDec);
		testConverter(converter, larger);
		larger = bigDec.add(decimal);
		testConverter(converter, larger);
	}
}
