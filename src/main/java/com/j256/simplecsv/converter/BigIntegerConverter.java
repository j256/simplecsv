package com.j256.simplecsv.converter;

import java.lang.reflect.Field;
import java.math.BigInteger;

import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;

/**
 * Converter for the Java String type.
 * 
 * @author graywatson
 */
public class BigIntegerConverter implements Converter<BigInteger> {

	@Override
	public void configure(boolean allowNull, String format, long flags, Field field) {
		// no op
	}

	@Override
	public void javaToString(FieldInfo fieldInfo, BigInteger value, StringBuilder sb) {
		if (value != null) {
			sb.append(value);
		}
	}

	@Override
	public BigInteger stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError) {
		if (value == null) {
			return null;
		} else {
			return new BigInteger(value);
		}
	}
}
