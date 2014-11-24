package com.j256.simplecsv.converter;

import java.lang.reflect.Field;
import java.util.UUID;

import com.j256.simplecsv.common.CsvField;
import com.j256.simplecsv.processor.FieldInfo;
import com.j256.simplecsv.processor.ParseError;

/**
 * Converter for the Java String type.
 * 
 * <p>
 * The {@link CsvField#converterFlags()} parameter can be set to {@link #TRIM_INPUT} to call {@link String#trim()} when
 * reading a cell and/or {@link #TRIM_OUTPUT} for trimming before a cell is printed.
 * </p>
 * 
 * @author graywatson
 */
public class UuidConverter implements Converter<UUID, Void> {

	private static final UuidConverter singleton = new UuidConverter();

	/**
	 * Get singleton for class.
	 */
	public static UuidConverter getSingleton() {
		return singleton;
	}

	@Override
	public Void configure(String format, long flags, Field field) {
		// no op
		return null;
	}

	@Override
	public boolean isNeedsQuotes(Void configInfo) {
		return true;
	}

	@Override
	public String javaToString(FieldInfo fieldInfo, UUID value) {
		if (value == null) {
			return null;
		} else {
			return value.toString();
		}
	}

	@Override
	public UUID stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError) {
		if (value.isEmpty()) {
			return null;
		} else {
			return UUID.fromString(value);
		}
	}
}
