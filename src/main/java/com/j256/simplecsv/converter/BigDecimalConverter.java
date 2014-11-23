package com.j256.simplecsv.converter;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;

/**
 * Converter for the Java String type.
 * 
 * @author graywatson
 */
public class BigDecimalConverter implements Converter<BigDecimal> {

	@Override
	public void configure(boolean allowNull, String format, long flags, Field field) {
		// no op
	}

	@Override
	public void javaToString(FieldInfo fieldInfo, BigDecimal value, StringBuilder sb) {
		if (value != null) {
			sb.append(value);
		}
	}

	@Override
	public BigDecimal stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError) {
		if (value == null) {
			return null;
		} else {
			return new BigDecimal(value);
		}
	}
}
