package com.j256.simplecsv.converter;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.ParseException;

import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;
import com.j256.simplecsv.ParseError.ErrorType;
import com.j256.simplecsv.annotations.CsvField;

/**
 * Converter for the Java Integer type.
 * 
 * <p>
 * NOTE: The {@link CsvField#format()} is the same pattern used by {@link DecimalFormat} and will be used in both
 * {@link #javaToString} and {@link #stringToJava} methods.
 * </p>
 * 
 * @author graywatson
 */
public abstract class NumberConverter<T extends Number> implements Converter<T> {

	private DecimalFormat decimalFormat;

	protected abstract T numberToValue(Number number);
	protected abstract T parseString(String value) throws NumberFormatException;

	@Override
	public void configure(boolean allowNull, String format, long flags, Field field) {
		if (format != null) {
			decimalFormat = new DecimalFormat(format);
		}
	}

	@Override
	public void javaToString(FieldInfo fieldInfo, T value, StringBuilder sb) {
		if (value == null) {
			return;
		}
		if (decimalFormat == null) {
			sb.append(value);
		} else {
			decimalFormat.format(numberToValue(value));
		}
	}

	@Override
	public T stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError)
			throws ParseException {
		if (value.length() == 0) {
			return null;
		} else if (decimalFormat == null) {
			try {
				return parseString(value);
			} catch (NumberFormatException nfe) {
				parseError.setErrorType(ErrorType.INVALID_FORMAT);
				parseError.setMessage(nfe.getMessage());
				return null;
			}
		} else {
			return numberToValue(decimalFormat.parse(value));
		}
	}
}
