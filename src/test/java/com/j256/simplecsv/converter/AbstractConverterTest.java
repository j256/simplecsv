package com.j256.simplecsv.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.text.ParseException;

import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;

public abstract class AbstractConverterTest {

	protected <T, C> String testConverter(Converter<T, C> converter, C configInfo, T value) throws ParseException {
		FieldInfo fieldInfo = FieldInfo.forTests(converter, configInfo);
		String strVal = converter.javaToString(fieldInfo, value);
		ParseError parseError = new ParseError();
		T converted = null;
		if (strVal != null) {
			converted = converter.stringToJava(strVal, 1, fieldInfo, strVal, parseError);
		}
		assertFalse(parseError.isError());
		System.out.println("value '" + value + "' == converted '" + converted + "' from string '" + strVal + "'");
		assertEquals(value, converted);
		return strVal;
	}
}
