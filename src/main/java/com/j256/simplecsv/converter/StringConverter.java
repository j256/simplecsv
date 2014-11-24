package com.j256.simplecsv.converter;

import java.lang.reflect.Field;

import com.j256.simplecsv.CsvField;
import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;

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
public class StringConverter implements Converter<String> {

	/**
	 * If enabled, trim() to be called on the string before it is printed.
	 */
	public static final long TRIM_OUTPUT = 1 << 1;
	/**
	 * If enabled, a blank string will be interpreted as a null value.
	 */
	public static final long BLANK_IS_NULL = 1 << 2;

	private boolean trimOutput;
	private boolean blankIsNull;

	@Override
	public void configure(String format, long flags, Field field) {
		trimOutput = ((flags & TRIM_OUTPUT) != 0);
		blankIsNull = ((flags & BLANK_IS_NULL) != 0);
	}

	@Override
	public void javaToString(FieldInfo fieldInfo, String value, StringBuilder sb) {
		if (value != null) {
			if (trimOutput) {
				sb.append(value.trim());
			} else {
				sb.append(value);
			}
		}
	}

	@Override
	public String stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError) {
		if (value.isEmpty() && blankIsNull) {
			return null;
		} else {
			return value;
		}
	}
}
