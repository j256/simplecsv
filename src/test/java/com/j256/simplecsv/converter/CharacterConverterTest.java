package com.j256.simplecsv.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.simplecsv.ParseError;

public class CharacterConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		CharacterConverter converter = new CharacterConverter();
		converter.configure(null, 0, null);

		testConverter(converter, '1');
		testConverter(converter, '2');
		// this should become \\r in the string
		testConverter(converter, '\r');
		testConverter(converter, Character.MIN_VALUE);
		testConverter(converter, Character.MAX_VALUE);
		testConverter(converter, null);
	}

	@Test
	public void testMoreThanOne() {
		CharacterConverter converter = new CharacterConverter();

		ParseError parseError = new ParseError();
		char one = '1';
		char two = '2';
		String cellVal = new String(new char[] { one, two });
		assertEquals((Object) one, converter.stringToJava("line", 1, null, cellVal, parseError));
		assertFalse(parseError.isError());

		converter.configure(null, CharacterConverter.PARSE_ERROR_IF_MORE_THAN_ONE_CHAR, null);
		converter.stringToJava("line", 1, null, cellVal, parseError);
		assertTrue(parseError.isError());
	}
}
