package com.j256.simplecsv.converter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

import com.j256.simplecsv.processor.ColumnInfo;
import com.j256.simplecsv.processor.FieldInfo;
import com.j256.simplecsv.processor.ParseError;

/**
 * Converter for the Java BigDecimal type.
 * 
 * @author graywatson
 */
public class BigDecimalConverter implements Converter<BigDecimal, DecimalFormat> {

	private static final BigDecimalConverter singleton = new BigDecimalConverter();

	/**
	 * Get singleton for class.
	 */
	public static BigDecimalConverter getSingleton() {
		return singleton;
	}

	@Override
	public DecimalFormat configure(String format, long flags, FieldInfo<BigDecimal> fieldInfo) {
		if (format == null) {
			return null;
		} else {
			DecimalFormat decimalFormat = new DecimalFormat(format);
			decimalFormat.setParseBigDecimal(true);
			return decimalFormat;
		}
	}

	@Override
	public boolean isNeedsQuotes(DecimalFormat decimalFormat) {
		return true;
	}

	@Override
	public boolean isAlwaysTrimInput() {
		return true;
	}

	@Override
	public String javaToString(ColumnInfo<BigDecimal> columnInfo, BigDecimal value) {
		DecimalFormat decimalFormat = (DecimalFormat) columnInfo.getConfigInfo();
		if (value == null) {
			return null;
		} else if (decimalFormat == null) {
			return value.toString();
		} else {
			return decimalFormat.format(value);
		}
	}

	@Override
	public BigDecimal stringToJava(String line, int lineNumber, int linePos, ColumnInfo<BigDecimal> columnInfo,
			String value, ParseError parseError) throws ParseException {
		DecimalFormat decimalFormat = (DecimalFormat) columnInfo.getConfigInfo();
		if (value.isEmpty()) {
			return null;
		} else if (decimalFormat == null) {
			return new BigDecimal(value);
		} else {
			return (BigDecimal) decimalFormat.parse(value);
		}
	}
}
