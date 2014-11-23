package com.j256.simplecsv.converter;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import com.j256.simplecsv.ParseError;

public abstract class AbstractConverterTest {

	/**
	 * Test the converter.
	 */
	protected <T> String testConverter(Converter<T> converter, T value) throws ParseException {
		StringBuilder sb = new StringBuilder();
		converter.javaToString(null, value, sb);
		String strVal = sb.toString();
		T converted = converter.stringToJava(strVal, 1, null, strVal, new ParseError());
		System.out.println("value '" + value + "' == converted '" + converted + "' from string '" + strVal + "'");
		assertEquals(value, converted);
		return strVal;
	}
}
