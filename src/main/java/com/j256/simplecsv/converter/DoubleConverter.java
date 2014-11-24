package com.j256.simplecsv.converter;

import java.text.DecimalFormat;

import com.j256.simplecsv.CsvField;

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
public class DoubleConverter extends AbstractNumberConverter<Double> {

	private static final DoubleConverter singleton = new DoubleConverter();

	/**
	 * Get singleton for class.
	 */
	public static DoubleConverter getSingleton() {
		return singleton;
	}

	@Override
	protected Double numberToValue(Number number) {
		return number.doubleValue();
	}

	@Override
	protected Double parseString(String value) throws NumberFormatException {
		return Double.parseDouble(value);
	}
}
