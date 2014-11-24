package com.j256.simplecsv.converter;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;

import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;

/**
 * Converter for the Java String type.
 * 
 * @author graywatson
 */
public class BigIntegerConverter implements Converter<BigInteger> {

	private DecimalFormat decimalFormat;

	@Override
	public void configure(String format, long flags, Field field) {
		if (format != null) {
			decimalFormat = new DecimalFormat(format);
			decimalFormat.setParseBigDecimal(true);
			decimalFormat.setParseIntegerOnly(true);
		}
	}

	@Override
	public void javaToString(FieldInfo fieldInfo, BigInteger value, StringBuilder sb) {
		if (value == null) {
			return;
		} else if (decimalFormat == null) {
			sb.append(value);
		} else {
			sb.append(decimalFormat.format(value));
		}
	}

	@Override
	public BigInteger stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError)
			throws ParseException {
		if (value.isEmpty()) {
			return null;
		} else if (decimalFormat == null) {
			return new BigInteger(value);
		} else {
			BigDecimal bigDecimal = (BigDecimal) decimalFormat.parse(value);
			return bigDecimal.toBigInteger();
		}
	}
}
