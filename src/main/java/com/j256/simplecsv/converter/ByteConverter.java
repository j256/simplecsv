package com.j256.simplecsv.converter;

import java.text.DecimalFormat;

import com.j256.simplecsv.CsvField;

/**
 * Converter for the Java Byte type.
 * 
 * <p>
 * NOTE: The {@link CsvField#format()} is the same pattern used by {@link DecimalFormat} and will be used in both
 * {@link #javaToString} and {@link #stringToJava} methods.
 * </p>
 * 
 * @author graywatson
 */
public class ByteConverter extends AbstractNumberConverter<Byte> {

	private static final ByteConverter singleton = new ByteConverter();

	/**
	 * Get singleton for class.
	 */
	public static ByteConverter getSingleton() {
		return singleton;
	}

	@Override
	protected Byte numberToValue(Number number) {
		return number.byteValue();
	}

	@Override
	protected Byte parseString(String value) throws NumberFormatException {
		return Byte.parseByte(value);
	}
}
