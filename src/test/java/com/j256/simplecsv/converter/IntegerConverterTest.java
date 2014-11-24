package com.j256.simplecsv.converter;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.junit.Test;

import com.j256.simplecsv.ParseError;

public class IntegerConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		IntegerConverter converter = new IntegerConverter();
		converter.configure(null, 0, null);
		testNumbers(converter);
	}

	@Test
	public void testFormat() throws Exception {
		IntegerConverter converter = new IntegerConverter();
		converter.configure("###,##0", 0, null);
		testNumbers(converter);
	}

	@Test
	public void testInvalidFormat() throws Exception {
		IntegerConverter converter = new IntegerConverter();

		ParseError parseError = new ParseError();
		assertNull(converter.stringToJava("line", 1, null, "notanumber", parseError));
		assertTrue(parseError.isError());
	}

	private void testNumbers(IntegerConverter converter) throws ParseException {
		testConverter(converter, -1);
		testConverter(converter, 0);
		testConverter(converter, 1);
		testConverter(converter, Integer.MIN_VALUE);
		testConverter(converter, Integer.MAX_VALUE);
		testConverter(converter, null);
	}
}
