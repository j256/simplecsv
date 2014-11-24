package com.j256.simplecsv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.simplecsv.ParseError.ErrorType;

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

		parseError.reset();
		assertEquals(ErrorType.NONE, parseError.getErrorType());
		assertNull(parseError.getMessage());
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
}
