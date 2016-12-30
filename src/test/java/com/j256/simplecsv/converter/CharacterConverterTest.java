package com.j256.simplecsv.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.simplecsv.processor.ColumnInfo;
import com.j256.simplecsv.processor.ParseError;

public class CharacterConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		CharacterConverter converter = CharacterConverter.getSingleton();

		testConverter(converter, Character.class, null, 0, '1');
		testConverter(converter, Character.class, null, 0, '2');
		// this should become \\r in the string
		testConverter(converter, Character.class, null, 0, '\r');
		testConverter(converter, Character.class, null, 0, Character.MIN_VALUE);
		testConverter(converter, Character.class, null, 0, Character.MAX_VALUE);
		testConverter(converter, Character.class, null, 0, null);
	}

	@Test
	public void testMoreThanOne() {
		CharacterConverter converter = CharacterConverter.getSingleton();
		ColumnInfo<Character> columnInfo = ColumnInfo.forTests(converter, Character.class, null, 0);

		ParseError parseError = new ParseError();
		char one = '1';
		char two = '2';
		String columnVal = new String(new char[] { one, two });
		assertEquals((Object) one, converter.stringToJava("line", 1, 2, columnInfo, columnVal, parseError));
		assertFalse(parseError.isError());

		columnInfo = ColumnInfo.forTests(converter, Character.class, null,
				CharacterConverter.PARSE_ERROR_IF_MORE_THAN_ONE_CHAR);
		converter.stringToJava("line", 1, 2, columnInfo, columnVal, parseError);
		assertTrue(parseError.isError());
	}

	@Test
	public void testConverage() {
		CharacterConverter converter = CharacterConverter.getSingleton();
		assertTrue(converter.isNeedsQuotes(null));
		assertFalse(converter.isAlwaysTrimInput());
	}
}
