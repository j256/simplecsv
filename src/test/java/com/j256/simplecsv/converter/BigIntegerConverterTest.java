package com.j256.simplecsv.converter;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;

import org.junit.Test;

public class BigIntegerConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		BigIntegerConverter converter = BigIntegerConverter.getSingleton();
		DecimalFormat configInfo = converter.configure(null, 0, null);
		testNumbers(converter, configInfo);
	}

	@Test
	public void testFormat() throws Exception {
		BigIntegerConverter converter = BigIntegerConverter.getSingleton();
		DecimalFormat configInfo = converter.configure("###,##0", 0, null);
		testNumbers(converter, configInfo);
	}

	@Test
	public void testConverage() {
		BigIntegerConverter converter = BigIntegerConverter.getSingleton();
		assertTrue(converter.isNeedsQuotes(null));
		assertTrue(converter.isAlwaysTrimInput());
	}

	private void testNumbers(BigIntegerConverter converter, DecimalFormat configInfo) throws ParseException {
		testConverter(converter, configInfo, new BigInteger("-1"));
		testConverter(converter, configInfo, new BigInteger("0"));
		testConverter(converter, configInfo, new BigInteger("1"));
		testConverter(converter, configInfo, null);

		BigInteger bigInt = new BigInteger(Long.toString(Long.MIN_VALUE));
		testConverter(converter, configInfo, bigInt);
		BigInteger smaller = bigInt.add(bigInt);
		testConverter(converter, configInfo, smaller);

		bigInt = new BigInteger(Long.toString(Long.MAX_VALUE));
		testConverter(converter, configInfo, bigInt);
		BigInteger larger = bigInt.add(bigInt);
		testConverter(converter, configInfo, larger);
	}
}
