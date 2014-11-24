package com.j256.simplecsv.converter;

import java.lang.reflect.Field;

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
public class StringConverter implements Converter<String, StringConverter.ConfigInfo> {

	/**
	 * If enabled, trim() to be called on the string before it is printed.
	 */
	public static final long TRIM_OUTPUT = 1 << 1;
	/**
	 * If enabled, a blank string will be interpreted as a null value.
	 */
	public static final long BLANK_IS_NULL = 1 << 2;

	private static final StringConverter singleton = new StringConverter();

	/**
	 * Get singleton for class.
	 */
	public static StringConverter getSingleton() {
		return singleton;
	}

	@Override
	public ConfigInfo configure(String format, long flags, Field field) {
		boolean trimOutput = ((flags & TRIM_OUTPUT) != 0);
		boolean blankIsNull = ((flags & BLANK_IS_NULL) != 0);
		return new ConfigInfo(trimOutput, blankIsNull);
	}

	@Override
	public boolean isNeedsQuotes(ConfigInfo configInfo) {
		return true;
	}

	@Override
	public String javaToString(FieldInfo fieldInfo, String value) {
		if (value == null) {
			return null;
		} else {
			ConfigInfo configInfo = (ConfigInfo) fieldInfo.getConfigInfo();
			if (configInfo.trimOutput) {
				return value.trim();
			} else {
				return value;
			}
		}
	}

	@Override
	public String stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError) {
		ConfigInfo configInfo = (ConfigInfo) fieldInfo.getConfigInfo();
		if (value.isEmpty() && configInfo.blankIsNull) {
			return null;
		} else {
			return value;
		}
	}

	public static class ConfigInfo {
		final boolean trimOutput;
		final boolean blankIsNull;
		private ConfigInfo(boolean trimOutput, boolean blankIsNull) {
			this.trimOutput = trimOutput;
			this.blankIsNull = blankIsNull;
		}

	}
}
