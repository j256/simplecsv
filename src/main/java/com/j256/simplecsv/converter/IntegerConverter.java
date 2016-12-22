package com.j256.simplecsv.converter;

import java.text.DecimalFormat;

import com.j256.simplecsv.common.CsvColumn;

/**
 * Converter for the Java Integer type.
 * 
 * <p>
 * NOTE: The {@link CsvColumn#format()} is the same pattern used by {@link DecimalFormat} and will be used in both
 * {@link #javaToString} and {@link #stringToJava} methods.
 * </p>
 * 
 * @author graywatson
 */
public class IntegerConverter extends AbstractNumberConverter<Integer> {

	private static final IntegerConverter singleton = new IntegerConverter();

	/**
	 * Get singleton for class.
	 */
	public static IntegerConverter getSingleton() {
		return singleton;
	}

	@Override
	protected Integer numberToValue(Number number) {
		return number.intValue();
	}

	@Override
	protected Integer parseString(String value) throws NumberFormatException {
		return Integer.parseInt(value);
	}
}
