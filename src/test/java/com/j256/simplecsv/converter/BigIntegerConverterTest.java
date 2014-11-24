package com.j256.simplecsv.converter;

import java.math.BigInteger;
import java.text.ParseException;

import org.junit.Test;

public class BigIntegerConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		BigIntegerConverter converter = new BigIntegerConverter();
		converter.configure(null, 0, null);
		testNumbers(converter);
	}

	@Test
	public void testFormat() throws Exception {
		BigIntegerConverter converter = new BigIntegerConverter();
		converter.configure("###,##0", 0, null);
		testNumbers(converter);
	}

	private void testNumbers(BigIntegerConverter converter) throws ParseException {
		testConverter(converter, new BigInteger("-1"));
		testConverter(converter, new BigInteger("0"));
		testConverter(converter, new BigInteger("1"));
		testConverter(converter, null);

		BigInteger bigInt = new BigInteger(Long.toString(Long.MIN_VALUE));
		testConverter(converter, bigInt);
		BigInteger smaller = bigInt.add(bigInt);
		testConverter(converter, smaller);

		bigInt = new BigInteger(Long.toString(Long.MAX_VALUE));
		testConverter(converter, bigInt);
		BigInteger larger = bigInt.add(bigInt);
		testConverter(converter, larger);
	}
}
