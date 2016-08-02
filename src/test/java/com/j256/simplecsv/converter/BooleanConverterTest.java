package com.j256.simplecsv.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.junit.Test;

import com.j256.simplecsv.converter.BooleanConverter.ConfigInfo;
import com.j256.simplecsv.processor.ColumnInfo;
import com.j256.simplecsv.processor.ParseError;

public class BooleanConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws ParseException {
		BooleanConverter converter = BooleanConverter.getSingleton();
		ConfigInfo configInfo = converter.configure(null, 0, null);
		testConverter(converter, configInfo, true);
		testConverter(converter, configInfo, false);
		testConverter(converter, configInfo, null);
		converter = new BooleanConverter();
		converter.configure("1,0", 0, null);
		testConverter(converter, configInfo, true);
		testConverter(converter, configInfo, false);
		testConverter(converter, configInfo, null);
	}

	@Test
	public void testCaseSensitive() {
		BooleanConverter converter = BooleanConverter.getSingleton();
		ConfigInfo configInfo = converter.configure(null, BooleanConverter.CASE_SENSITIVE, null);
		ColumnInfo columnInfo = ColumnInfo.forTests(converter, configInfo);
		ParseError parseError = new ParseError();
		assertTrue(converter.stringToJava("line", 1, 2, columnInfo, "true", parseError));
		assertFalse(converter.stringToJava("line", 1, 2, columnInfo, "True", parseError));
		assertFalse(converter.stringToJava("line", 1, 2, columnInfo, "TRUE", parseError));
		assertFalse(converter.stringToJava("line", 1, 2, columnInfo, "wrong", parseError));

		configInfo = converter.configure(null, 0, null);
		columnInfo = ColumnInfo.forTests(converter, configInfo);
		assertTrue(converter.stringToJava("line", 1, 2, columnInfo, "true", parseError));
		assertTrue(converter.stringToJava("line", 1, 2, columnInfo, "True", parseError));
		assertTrue(converter.stringToJava("line", 1, 2, columnInfo, "TRUE", parseError));
		assertFalse(converter.stringToJava("line", 1, 2, columnInfo, "wrong", parseError));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadFormat() {
		BooleanConverter converter = BooleanConverter.getSingleton();
		converter.configure("1", 0, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyTrue() {
		BooleanConverter converter = BooleanConverter.getSingleton();
		converter.configure(",F", 0, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyFalse() {
		BooleanConverter converter = BooleanConverter.getSingleton();
		converter.configure("T,", 0, null);
	}

	@Test
	public void testNeedsQuotes() {
		BooleanConverter converter = BooleanConverter.getSingleton();
		ConfigInfo configInfo = converter.configure("1,0", 0, null);
		assertFalse(converter.isNeedsQuotes(configInfo));
	}

	@Test
	public void testConverage() {
		BooleanConverter converter = BooleanConverter.getSingleton();
		ConfigInfo configInfo = converter.configure(null, 0, null);
		assertFalse(converter.isNeedsQuotes(configInfo));
		assertFalse(converter.isAlwaysTrimInput());
	}

	@Test
	public void testParseErrorOnBadValue() {
		BooleanConverter converter = new BooleanConverter();
		ConfigInfo configInfo = converter.configure(null, 0, null);
		ColumnInfo columnInfo = ColumnInfo.forTests(converter, configInfo);
		ParseError parseError = new ParseError();
		assertEquals(false, converter.stringToJava("line", 1, 2, columnInfo, "unknown", parseError));
		assertFalse(parseError.isError());

		configInfo = converter.configure(null, BooleanConverter.PARSE_ERROR_ON_INVALID_VALUE, null);
		columnInfo = ColumnInfo.forTests(converter, configInfo);
		parseError.reset();
		int linePos = 123213;
		assertNull(converter.stringToJava("line", 0, linePos, columnInfo, "unknown", parseError));
		assertTrue(parseError.isError());
		assertEquals(linePos, parseError.getLinePos());
	}

	@Test
	public void testNull() {
		BooleanConverter converter = new BooleanConverter();
		assertNull(converter.javaToString(null, null));
	}
}
