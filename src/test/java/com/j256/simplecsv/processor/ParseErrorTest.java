package com.j256.simplecsv.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.simplecsv.processor.ParseError.ErrorType;

public class ParseErrorTest {

	@Test
	public void testStuff() {
		ParseError parseError = new ParseError();
		ErrorType errorType = ErrorType.INVALID_FORMAT;
		parseError.setErrorType(errorType);
		assertEquals(errorType, parseError.getErrorType());
		assertTrue(parseError.isError());
		String msg = "eopfjewpfjwef";
		parseError.setMessage(msg);
		assertEquals(msg, parseError.getMessage());
		String line = "fpeowjfpewofjewpfjewopfjewopfjw";
		parseError.setLine(line);
		assertEquals(line, parseError.getLine());
		int lineNumber = 21321312;
		parseError.setLineNumber(lineNumber);
		assertEquals(lineNumber, parseError.getLineNumber());
		int linePos = 21321312;
		parseError.setLinePos(linePos);
		assertEquals(linePos, parseError.getLinePos());
		assertNull(parseError.getColumnName());
		String colName = "pefwjpgowe";
		parseError.setColumnName(colName);
		assertEquals(colName, parseError.getColumnName());
		String colValue = "pjfpoewefjpoefwjpgowe";
		assertNull(parseError.getColumnValue());
		parseError.setColumnValue(colValue);
		assertEquals(colValue, parseError.getColumnValue());
		assertNull(parseError.getColumnType());
		parseError.setColumnType(getClass());
		assertEquals(getClass(), parseError.getColumnType());

		parseError.reset();
		assertEquals(ErrorType.NONE, parseError.getErrorType());
		assertNull(parseError.getMessage());
		assertNull(parseError.getColumnName());
		assertNull(parseError.getColumnValue());
		assertNull(parseError.getColumnType());
		assertNull(parseError.getLine());
		assertEquals(0, parseError.getLineNumber());
		assertEquals(0, parseError.getLinePos());
		assertFalse(parseError.isError());
	}

	@Test
	public void testToString() {
		ParseError parseError = new ParseError();
		ErrorType errorType = ErrorType.INVALID_FORMAT;
		parseError.setErrorType(errorType);
		assertEquals(errorType.getTypeMessage(), parseError.toString());

		String msg = "eopfjewpfjwef";
		parseError.setMessage(msg);
		String toString = parseError.toString();
		assertTrue(toString.contains(errorType.getTypeMessage()));
		assertTrue(toString.contains(msg));
	}

	@Test
	public void testFullString() {
		ParseError parseError = new ParseError();
		String msg = "jrgrpjegrjpoergrpjoe";
		parseError.setMessage(msg);
		String colName = "col";
		parseError.setColumnName(colName);
		String colValue = "val";
		parseError.setColumnValue(colValue);
		parseError.setColumnType(getClass());
		ErrorType errorType = ErrorType.INVALID_FORMAT;
		parseError.setErrorType(errorType);
		String toString = parseError.toString();
		assertTrue(toString.contains(errorType.getTypeMessage()));
		assertTrue(toString.contains(colName));
		assertTrue(toString.contains(colValue));
		assertTrue(toString.contains(msg));
		assertTrue(toString.contains(getClass().getSimpleName()));
		System.out.println(toString);
	}
}
