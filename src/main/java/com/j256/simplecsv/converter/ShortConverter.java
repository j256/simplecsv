package com.j256.simplecsv.converter;

import java.text.DecimalFormat;

import com.j256.simplecsv.CsvField;

/**
 * Converter for the Java Short type.
 * 
 * <p>
 * NOTE: The {@link CsvField#format()} is the same pattern used by {@link DecimalFormat} and will be used in both
 * {@link #javaToString} and {@link #stringToJava} methods.
 * </p>
 * 
 * @author graywatson
 */
public class ShortConverter extends AbstractNumberConverter<Short> {

	private static final ShortConverter singleton = new ShortConverter();

	/**
	 * Get singleton for class.
	 */
	public static ShortConverter getSingleton() {
		return singleton;
	}

	@Override
	protected Short numberToValue(Number number) {
		return number.shortValue();
	}

	@Override
	protected Short parseString(String value) throws NumberFormatException {
		return Short.parseShort(value);
	}
}
