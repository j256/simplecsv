package com.j256.simplecsv.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.j256.simplecsv.ParseError;

public class StringConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		StringConverter converter = new StringConverter();
		converter.configure(null, 0, null);

		testConverter(converter, "");
		testConverter(converter, "one");
		testConverter(converter, "two");
	}

	@Test
	public void testBlankNull() throws Exception {
		StringConverter converter = new StringConverter();

		ParseError parseError = new ParseError();
		assertEquals("", converter.stringToJava("line", 1, null, "", parseError));
		assertFalse(parseError.isError());

		converter.configure(null, StringConverter.BLANK_IS_NULL, null);
		assertNull(converter.stringToJava("line", 1, null, "", parseError));
		assertFalse(parseError.isError());

		testConverter(converter, null);
	}

	@Test
	public void testTrimOutput() {
		StringConverter converter = new StringConverter();

		String ok = "ok";
		String spacedOk = " " + ok + " ";
		StringBuilder sb = new StringBuilder();
		converter.javaToString(null, spacedOk, sb);
		assertEquals(spacedOk, sb.toString());

		converter.configure(null, StringConverter.TRIM_OUTPUT, null);
		sb.setLength(0);
		converter.javaToString(null, spacedOk, sb);
		assertEquals(ok, sb.toString());
	}
}
