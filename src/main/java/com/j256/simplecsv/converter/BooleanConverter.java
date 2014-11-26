package com.j256.simplecsv.converter;

import java.lang.reflect.Field;

import com.j256.simplecsv.common.CsvField;
import com.j256.simplecsv.processor.FieldInfo;
import com.j256.simplecsv.processor.ParseError;
import com.j256.simplecsv.processor.ParseError.ErrorType;

/**
 * Converter for the Java Boolean type.
 * 
 * <p>
 * The {@link CsvField#format()} parameter can be set to a comma separated list of 2 strings. The string before the
 * comma will be printed for true, and the string after the comma will be printed for false. For example "1,0" will
 * output and read 1 for true and 0 for false.
 * </p>
 * 
 * <p>
 * The {@link CsvField#converterFlags()} can be set {@link #PARSE_ERROR_ON_INVALID_VALUE} if you want a parse error
 * generated on unknown values.
 * </p>
 * 
 * @author graywatson
 */
public class BooleanConverter implements Converter<Boolean, BooleanConverter.ConfigInfo> {

	/**
	 * Set this flag using {@link CsvField#converterFlags()} if you want a parse error to be generated if the value is
	 * not either false or true (or the ones specified in the format). Default is that an invalid value will generate
	 * false.
	 */
	public static final long PARSE_ERROR_ON_INVALID_VALUE = 1 << 1;

	private static final BooleanConverter singleton = new BooleanConverter();

	/**
	 * Get singleton for class.
	 */
	public static BooleanConverter getSingleton() {
		return singleton;
	}

	@Override
	public ConfigInfo configure(String format, long flags, Field field) {
		String trueString;
		String falseString;
		if (format == null) {
			trueString = "true";
			falseString = "false";
		} else {
			String[] parts = format.split(",", 2);
			if (parts.length != 2) {
				throw new IllegalArgumentException("Invalid boolean format should in the form of T,F: " + format);
			}
			trueString = parts[0];
			if (trueString.length() == 0) {
				throw new IllegalArgumentException("Invalid boolean format should in the form of T,F: " + format);
			}
			falseString = parts[1];
			if (falseString.length() == 0) {
				throw new IllegalArgumentException("Invalid boolean format should in the form of T,F: " + format);
			}
		}
		boolean parseErrorOnInvalid = ((flags & PARSE_ERROR_ON_INVALID_VALUE) != 0);
		return new ConfigInfo(trueString, falseString, parseErrorOnInvalid);
	}

	@Override
	public boolean isNeedsQuotes(ConfigInfo configInfo) {
		try {
			Long.parseLong(configInfo.trueString);
			Long.parseLong(configInfo.falseString);
			// if they are both numbers then no
			return false;
		} catch (NumberFormatException nfe) {
			return true;
		}
	}

	@Override
	public boolean isAlwaysTrimInput() {
		return false;
	}

	@Override
	public String javaToString(FieldInfo fieldInfo, Boolean value) {
		if (value == null) {
			return null;
		}
		ConfigInfo configInfo = (ConfigInfo) fieldInfo.getConfigInfo();
		if (value) {
			return configInfo.trueString;
		} else {
			return configInfo.falseString;
		}
	}

	@Override
	public Boolean stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError) {
		ConfigInfo configInfo = (ConfigInfo) fieldInfo.getConfigInfo();
		if (value.isEmpty()) {
			return null;
		} else if (value.equals(configInfo.trueString)) {
			return true;
		} else if (value.equals(configInfo.falseString)) {
			return false;
		} else if (configInfo.parseErrorOnInvalid) {
			parseError.setErrorType(ErrorType.INVALID_FORMAT);
			return null;
		} else {
			return false;
		}
	}

	static class ConfigInfo {
		final String trueString;
		final String falseString;
		final boolean parseErrorOnInvalid;
		private ConfigInfo(String trueString, String falseString, boolean parseErrorOnInvalid) {
			this.trueString = trueString;
			this.falseString = falseString;
			this.parseErrorOnInvalid = parseErrorOnInvalid;
		}
	}
}
