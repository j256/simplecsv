package com.j256.simplecsv.converter;

import com.j256.simplecsv.common.CsvField;
import com.j256.simplecsv.processor.ColumnInfo;
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
 * The {@link CsvField#converterFlags()} can be set with {@link #PARSE_ERROR_ON_INVALID_VALUE} if you want a parse error
 * generated on unknown values, {@link #CASE_SENSITIVE} if you want to compare the true and false values in a
 * case-sensitive manner, and/or{@link #NEEDS_QUOTES} if you want the output to be surrounded by quotes.
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
	/**
	 * Set this flag using {@link CsvField#converterFlags()} if you want the boolean formats to be compared
	 * case-sensitively. So "TRUE" and "True" would be converted into false. Default is case-insensitive.
	 */
	public static final long CASE_SENSITIVE = 1 << 2;
	/**
	 * Set this flag using {@link CsvField#converterFlags()} if you want the output to be surrounded by quotes. Default
	 * is none.
	 */
	public static final long NEEDS_QUOTES = 1 << 3;

	private static final BooleanConverter singleton = new BooleanConverter();

	private static final String DEFAULT_TRUE_STRING = "true";
	private static final String DEFAULT_FALSE_STRING = "false";

	/**
	 * Get singleton for class.
	 */
	public static BooleanConverter getSingleton() {
		return singleton;
	}

	@Override
	public ConfigInfo configure(String format, long flags, FieldInfo<Boolean> fieldInfo) {
		String trueString;
		String falseString;
		if (format == null) {
			trueString = DEFAULT_TRUE_STRING;
			falseString = DEFAULT_FALSE_STRING;
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
		boolean caseSensitive = ((flags & CASE_SENSITIVE) != 0);
		boolean needsQuotes = ((flags & NEEDS_QUOTES) != 0);
		return new ConfigInfo(trueString, falseString, parseErrorOnInvalid, caseSensitive, needsQuotes);
	}

	@Override
	public boolean isNeedsQuotes(ConfigInfo configInfo) {
		return configInfo.needsQuotes;
	}

	@Override
	public boolean isAlwaysTrimInput() {
		return false;
	}

	@Override
	public String javaToString(ColumnInfo<Boolean> columnInfo, Boolean value) {
		if (value == null) {
			return null;
		}
		ConfigInfo configInfo = (ConfigInfo) columnInfo.getConfigInfo();
		if (value) {
			return configInfo.trueString;
		} else {
			return configInfo.falseString;
		}
	}

	@Override
	public Boolean stringToJava(String line, int lineNumber, int linePos, ColumnInfo<Boolean> columnInfo, String value,
			ParseError parseError) {
		ConfigInfo configInfo = (ConfigInfo) columnInfo.getConfigInfo();
		if (value.isEmpty()) {
			return null;
		} else if (isEquals(configInfo, value, configInfo.trueString)) {
			return true;
		} else if (isEquals(configInfo, value, configInfo.falseString)) {
			return false;
		} else if (configInfo.parseErrorOnInvalid) {
			parseError.setErrorType(ErrorType.INVALID_FORMAT);
			parseError.setLinePos(linePos);
			return null;
		} else {
			return false;
		}
	}

	private boolean isEquals(ConfigInfo configInfo, String value, String formatValue) {
		if (configInfo.caseSensitive) {
			return value.equals(formatValue);
		} else {
			return value.equalsIgnoreCase(formatValue);
		}
	}

	/**
	 * Exposed for testing.
	 */
	static class ConfigInfo {
		final String trueString;
		final String falseString;
		final boolean parseErrorOnInvalid;
		final boolean caseSensitive;
		final boolean needsQuotes;

		private ConfigInfo(String trueString, String falseString, boolean parseErrorOnInvalid, boolean caseSensitive,
				boolean needsQuotes) {
			this.trueString = trueString;
			this.falseString = falseString;
			this.parseErrorOnInvalid = parseErrorOnInvalid;
			this.caseSensitive = caseSensitive;
			this.needsQuotes = needsQuotes;
		}
	}
}
