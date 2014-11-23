package com.j256.simplecsv.converter;

import java.text.DecimalFormat;

import com.j256.simplecsv.annotations.CsvField;

/**
 * Converter for the Java Double type.
 * 
 * <p>
 * NOTE: The {@link CsvField#format()} is the same pattern used by {@link DecimalFormat} and will be used in both
 * {@link #javaToString} and {@link #stringToJava} methods.
 * </p>
 * 
 * @author graywatson
 */
public class DoubleConverter extends NumberConverter<Double> {

	@Override
	protected Double numberToValue(Number number) {
		return number.doubleValue();
	}

	@Override
	protected Double parseString(String value) throws NumberFormatException {
		return Double.parseDouble(value);
	}
}
