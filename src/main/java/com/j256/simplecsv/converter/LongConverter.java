package com.j256.simplecsv.converter;

import java.text.DecimalFormat;

import com.j256.simplecsv.CsvField;

/**
 * Converter for the Java Long type.
 * 
 * <p>
 * NOTE: The {@link CsvField#format()} is the same pattern used by {@link DecimalFormat} and will be used in both
 * {@link #javaToString} and {@link #stringToJava} methods.
 * </p>
 * 
 * @author graywatson
 */
public class LongConverter extends AbstractNumberConverter<Long> {

	@Override
	protected Long numberToValue(Number number) {
		return number.longValue();
	}

	@Override
	protected Long parseString(String value) throws NumberFormatException {
		return Long.parseLong(value);
	}
}
