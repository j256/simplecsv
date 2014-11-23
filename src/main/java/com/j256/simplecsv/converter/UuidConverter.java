package com.j256.simplecsv.converter;

import java.lang.reflect.Field;
import java.util.UUID;

import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;
import com.j256.simplecsv.annotations.CsvField;

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
public class UuidConverter implements Converter<UUID> {

	@Override
	public void configure(boolean allowNull, String format, long flags, Field field) {
		// no op
	}

	@Override
	public void javaToString(FieldInfo fieldInfo, UUID value, StringBuilder sb) {
		if (value != null) {
			sb.append(value.toString());
		}
	}

	@Override
	public UUID stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError) {
		if (value == null) {
			return null;
		} else {
			return UUID.fromString(value);
		}
	}
}
