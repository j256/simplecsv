package com.j256.simplecsv.converter;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.ParseException;

import org.junit.Test;

public class BigDecimalConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws ParseException {
		BigDecimalConverter converter = BigDecimalConverter.getSingleton();
		testNumbers(converter, null);
	}

	@Test
	public void testFormat() throws Exception {
		BigDecimalConverter converter = BigDecimalConverter.getSingleton();
		testNumbers(converter,
				"###,##0.0##########################################################################################");
	}

	@Test
	public void testConverage() {
		BigDecimalConverter converter = BigDecimalConverter.getSingleton();
		assertTrue(converter.isNeedsQuotes(null));
		assertTrue(converter.isAlwaysTrimInput());
	}

	private void testNumbers(BigDecimalConverter converter, String format) throws ParseException {
		testConverter(converter, BigDecimal.class, format, 0, new BigDecimal("-1.0"));
		testConverter(converter, BigDecimal.class, format, 0, new BigDecimal("0.0"));
		testConverter(converter, BigDecimal.class, format, 0, new BigDecimal("1.0"));
		testConverter(converter, BigDecimal.class, format, 0, null);

		BigDecimal bigDec = new BigDecimal(Long.toString(Long.MIN_VALUE) + ".0");
		testConverter(converter, BigDecimal.class, format, 0, bigDec);
		BigDecimal smaller = bigDec.add(bigDec);
		testConverter(converter, BigDecimal.class, format, 0, smaller);
		String decimalStr = "0.7029384290482390483482308230489230482390482304823048230482304823048230482034";
		BigDecimal decimal = new BigDecimal(decimalStr);
		smaller = bigDec.subtract(decimal);
		testConverter(converter, BigDecimal.class, format, 0, smaller);

		bigDec = new BigDecimal(Long.toString(Long.MAX_VALUE) + ".0");
		testConverter(converter, BigDecimal.class, format, 0, bigDec);
		BigDecimal larger = bigDec.add(bigDec);
		testConverter(converter, BigDecimal.class, format, 0, larger);
		larger = bigDec.add(decimal);
		testConverter(converter, BigDecimal.class, format, 0, larger);
	}
}
