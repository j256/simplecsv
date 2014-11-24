package com.j256.simplecsv.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.simplecsv.processor.FieldInfo;
import com.j256.simplecsv.processor.ParseError;

public class CharacterConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		CharacterConverter converter = new CharacterConverter();
		Boolean configInfo = converter.configure(null, 0, null);

		testConverter(converter, configInfo, '1');
		testConverter(converter, configInfo, '2');
		// this should become \\r in the string
		testConverter(converter, configInfo, '\r');
		testConverter(converter, configInfo, Character.MIN_VALUE);
		testConverter(converter, configInfo, Character.MAX_VALUE);
		testConverter(converter, configInfo, null);
	}

	@Test
	public void testMoreThanOne() {
		CharacterConverter converter = new CharacterConverter();
		Boolean configInfo = converter.configure(null, 0, null);
		FieldInfo fieldInfo = FieldInfo.forTests(converter, configInfo);

		ParseError parseError = new ParseError();
		char one = '1';
		char two = '2';
		String cellVal = new String(new char[] { one, two });
		assertEquals((Object) one, converter.stringToJava("line", 1, fieldInfo, cellVal, parseError));
		assertFalse(parseError.isError());

		configInfo = converter.configure(null, CharacterConverter.PARSE_ERROR_IF_MORE_THAN_ONE_CHAR, null);
		fieldInfo = FieldInfo.forTests(converter, configInfo);
		converter.stringToJava("line", 1, fieldInfo, cellVal, parseError);
		assertTrue(parseError.isError());
	}
}
