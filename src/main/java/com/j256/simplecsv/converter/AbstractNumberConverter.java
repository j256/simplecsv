package com.j256.simplecsv.converter;

import java.text.DecimalFormat;
import java.text.ParseException;

import com.j256.simplecsv.processor.ColumnInfo;
import com.j256.simplecsv.processor.ParseError;
import com.j256.simplecsv.processor.ParseError.ErrorType;

/**
 * Abstract converter for Java Number types.
 * 
 * @author graywatson
 */
public abstract class AbstractNumberConverter<T extends Number> implements Converter<T, DecimalFormat> {

	/**
	 * Convert a number to the appropriate Java type.
	 */
	protected abstract T numberToValue(Number number);

	/**
	 * Parse a string into the appropriate Java type.
	 */
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
	public boolean isAlwaysTrimInput() {
		return true;
	}

	@Override
	public DecimalFormat configure(String format, long flags, ColumnInfo<T> fieldInfo) {
		if (format == null) {
			return null;
		} else {
			return new DecimalFormat(format);
		}
	}

	@Override
	public String javaToString(ColumnInfo<T> columnInfo, T value) {
		DecimalFormat decimalFormat = (DecimalFormat) columnInfo.getConfigInfo();
		if (value == null) {
			return null;
		} else if (decimalFormat == null) {
			return value.toString();
		} else {
			return decimalFormat.format(numberToValue(value));
		}
	}

	@Override
	public T stringToJava(String line, int lineNumber, int linePos, ColumnInfo<T> columnInfo, String value,
			ParseError parseError) throws ParseException {
		DecimalFormat decimalFormat = (DecimalFormat) columnInfo.getConfigInfo();
		if (value.length() == 0) {
			return null;
		} else if (decimalFormat == null) {
			try {
				return parseString(value);
			} catch (NumberFormatException nfe) {
				parseError.setErrorType(ErrorType.INVALID_FORMAT);
				parseError.setMessage(nfe.getMessage());
				parseError.setLinePos(linePos);
				return null;
			}
		} else {
			return numberToValue(decimalFormat.parse(value));
		}
	}
}
