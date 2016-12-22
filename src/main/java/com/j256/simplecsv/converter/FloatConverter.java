package com.j256.simplecsv.converter;

import java.text.DecimalFormat;

import com.j256.simplecsv.common.CsvColumn;

/**
 * Converter for the Java Float type.
 * 
 * <p>
 * NOTE: The {@link CsvColumn#format()} is the same pattern used by {@link DecimalFormat} and will be used in both
 * {@link #javaToString} and {@link #stringToJava} methods.
 * </p>
 * 
 * @author graywatson
 */
public class FloatConverter extends AbstractNumberConverter<Float> {

	private static final FloatConverter singleton = new FloatConverter();

	/**
	 * Get singleton for class.
	 */
	public static FloatConverter getSingleton() {
		return singleton;
	}

	@Override
	protected Float numberToValue(Number number) {
		return number.floatValue();
	}

	@Override
	protected Float parseString(String value) throws NumberFormatException {
		return Float.parseFloat(value);
	}
}
