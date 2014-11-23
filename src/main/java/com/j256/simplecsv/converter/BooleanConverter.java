package com.j256.simplecsv.converter;

import java.lang.reflect.Field;

import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;
import com.j256.simplecsv.ParseError.ErrorType;
import com.j256.simplecsv.annotations.CsvField;

/**
 * Converter for the Java Boolean type.
 * 
 * <p>
 * The {@link CsvField#format()} parameter can be set to a comma separated list of 2 strings. The string before the
 * comma will be printed for true, and the string after the comma will be printed for false. For example "1,0" will
 * output and read 1 for true and 0 for false.
 * </p>
 * 
 * @author graywatson
 */
public class BooleanConverter implements Converter<Boolean> {

	/**
	 * Set this flag using {@link CsvField#converterFlags()} if you want a parse error to be generated if the value is
	 * not either false or true (or the ones specified in the format). Default is that an invalid value will generate
	 * false.
	 */
	public static final long PARSE_ERROR_ON_INVALID_VALUE = 1 << 1;

	private String trueString = "true";
	private String falseString = "false";
	private boolean parseErrorOnInvalid;

	@Override
	public void configure(boolean allowNull, String format, long flags, Field field) {
		if (format != null) {

		}
		parseErrorOnInvalid = ((flags & PARSE_ERROR_ON_INVALID_VALUE) != 0);
	}

	@Override
	public void javaToString(FieldInfo fieldInfo, Boolean value, StringBuilder sb) {
		if (value != null) {
			if (trueString == null) {
				sb.append(value);
			} else if (value) {
				sb.append(trueString);
			} else {
				sb.append(falseString);
			}
		}
	}

	@Override
	public Boolean stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError) {
		String trimmed = value.trim();
		if (trimmed.length() == 0) {
			return null;
		}
		if (trimmed.equals(trueString)) {
			return true;
		} else if (trimmed.equals(falseString)) {
			return false;
		} else if (parseErrorOnInvalid) {
			parseError.setErrorType(ErrorType.INVALID_FORMAT);
			return null;
		} else {
			return false;
		}
	}
}
