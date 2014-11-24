package com.j256.simplecsv.converter;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;

/**
 * Converter for the Java String type.
 * 
 * @author graywatson
 */
public class BigDecimalConverter implements Converter<BigDecimal> {

	private DecimalFormat decimalFormat;

	@Override
	public void configure(String format, long flags, Field field) {
		if (format != null) {
			decimalFormat = new DecimalFormat(format);
			decimalFormat.setParseBigDecimal(true);
		}
	}

	@Override
	public void javaToString(FieldInfo fieldInfo, BigDecimal value, StringBuilder sb) {
		if (value == null) {
			return;
		} else if (decimalFormat == null) {
			sb.append(value);
		} else {
			sb.append(decimalFormat.format(value));
		}
	}

	@Override
	public BigDecimal stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError)
			throws ParseException {
		if (value.isEmpty()) {
			return null;
		} else if (decimalFormat == null) {
			return new BigDecimal(value);
		} else {
			return (BigDecimal) decimalFormat.parse(value);
		}
	}
}
