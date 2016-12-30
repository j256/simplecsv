package com.j256.simplecsv.converter;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.text.ParseException;

import org.junit.Test;

public class BigIntegerConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		BigIntegerConverter converter = BigIntegerConverter.getSingleton();
		testNumbers(converter, null);
	}

	@Test
	public void testFormat() throws Exception {
		BigIntegerConverter converter = BigIntegerConverter.getSingleton();
		testNumbers(converter, "###,##0");
	}

	@Test
	public void testConverage() {
		BigIntegerConverter converter = BigIntegerConverter.getSingleton();
		assertTrue(converter.isNeedsQuotes(null));
		assertTrue(converter.isAlwaysTrimInput());
	}

	private void testNumbers(BigIntegerConverter converter, String format) throws ParseException {
		testConverter(converter, BigInteger.class, format, 0, new BigInteger("-1"));
		testConverter(converter, BigInteger.class, format, 0, new BigInteger("0"));
		testConverter(converter, BigInteger.class, format, 0, new BigInteger("1"));
		testConverter(converter, BigInteger.class, format, 0, null);

		BigInteger bigInt = new BigInteger(Long.toString(Long.MIN_VALUE));
		testConverter(converter, BigInteger.class, format, 0, bigInt);
		BigInteger smaller = bigInt.add(bigInt);
		testConverter(converter, BigInteger.class, format, 0, smaller);

		bigInt = new BigInteger(Long.toString(Long.MAX_VALUE));
		testConverter(converter, BigInteger.class, format, 0, bigInt);
		BigInteger larger = bigInt.add(bigInt);
		testConverter(converter, BigInteger.class, format, 0, larger);
	}
}
