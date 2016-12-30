package com.j256.simplecsv.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import com.j256.simplecsv.converter.StringConverter.ConfigInfo;
import com.j256.simplecsv.processor.ColumnInfo;
import com.j256.simplecsv.processor.ParseError;

public abstract class AbstractConverterTest {

	protected <T, C> String testConverter(Converter<T, C> converter, C configInfo, T value) throws ParseException {
		ColumnInfo<T> columnInfo = ColumnInfo.forTests(converter, configInfo);
		String strVal = converter.javaToString(columnInfo, value);
		ParseError parseError = new ParseError();
		T converted = null;
		if (strVal != null) {
			converted = converter.stringToJava(strVal, 1, 2, columnInfo, strVal, parseError);
		}
		assertFalse(parseError.isError());
		//System.out.println("value '" + value + "' == converted '" + converted + "' from string '" + strVal + "'");
		assertEquals(value, converted);
		if (converter instanceof StringConverter) {
			String val = (String) converter.stringToJava("", 1, 2, columnInfo, "", parseError);
			if (((ConfigInfo) configInfo).blankIsNull) {
				assertNull(val);
			} else {
				assertTrue(val.isEmpty());
			}
		} else {
			assertNull(converter.stringToJava("", 1, 2, columnInfo, "", parseError));
		}
		return strVal;
	}
}
