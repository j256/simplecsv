package com.j256.simplecsv.converter;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.ParseException;

import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;
import com.j256.simplecsv.ParseError.ErrorType;

/**
 * Abstract converter for Java Number types.
 * 
 * @author graywatson
 */
public abstract class AbstractNumberConverter<T extends Number> implements Converter<T, DecimalFormat> {

	protected abstract T numberToValue(Number number);
	protected abstract T parseString(String value) throws NumberFormatException;

	@Override
	public boolean isNeedsQuotes(DecimalFormat decimalFormat) {
		if (decimalFormat == null) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public DecimalFormat configure(String format, long flags, Field field) {
		if (format == null) {
			return null;
		} else {
			return new DecimalFormat(format);
		}
	}

	@Override
	public void javaToString(FieldInfo fieldInfo, T value, StringBuilder sb) {
		DecimalFormat decimalFormat = (DecimalFormat) fieldInfo.getConfigInfo();
		if (value == null) {
			return;
		} else if (decimalFormat == null) {
			sb.append(value);
		} else {
			sb.append(decimalFormat.format(numberToValue(value)));
		}
	}

	@Override
	public T stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError)
			throws ParseException {
		DecimalFormat decimalFormat = (DecimalFormat) fieldInfo.getConfigInfo();
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
