package com.j256.simplecsv.converter;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.j256.simplecsv.CsvField;
import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;

/**
 * Converter for the Java Date type which uses the {@link SimpleDateFormat} -- don't worry I protect it for reentrance.
 * 
 * <p>
 * The {@link CsvField#converterFlags()} parameter can be set to {@link #TRIM_INPUT} to call {@link String#trim()} when
 * reading a cell and/or {@link #TRIM_OUTPUT} for trimming before a cell is printed.
 * </p>
 * 
 * @author graywatson
 */
public class DateConverter implements Converter<Date, String> {

	public static final String DEFAULT_DATE_PATTERN = "MM/dd/yyyy";

	/*
	 * We need to do this because SimpleDateFormat is not thread safe.
	 */
	private final ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<SimpleDateFormat>();

	private static final DateConverter singleton = new DateConverter();

	/**
	 * Get singleton for class.
	 */
	public static DateConverter getSingleton() {
		return singleton;
	}

	@Override
	public String configure(String format, long flags, Field field) {
		String datePattern;
		if (format == null) {
			datePattern = DEFAULT_DATE_PATTERN;
		} else {
			datePattern = format;
		}
		// we do this to validate that the pattern is correct so we throw immediately here
		new SimpleDateFormat(datePattern);
		return datePattern;
	}

	@Override
	public boolean isNeedsQuotes(String datePattern) {
		return true;
	}

	@Override
	public String javaToString(FieldInfo fieldInfo, Date value) {
		if (value == null) {
			return null;
		} else {
			String datePattern = (String) fieldInfo.getConfigInfo();
			return getDateFormatter(datePattern).format(value);
		}
	}

	@Override
	public Date stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError)
			throws ParseException {
		if (value.isEmpty()) {
			return null;
		} else {
			String datePattern = (String) fieldInfo.getConfigInfo();
			return getDateFormatter(datePattern).parse(value);
		}
	}

	private SimpleDateFormat getDateFormatter(String format) {
		// we do this because we can't use the initValue() method
		SimpleDateFormat formatter = threadLocal.get();
		if (formatter == null) {
			formatter = new SimpleDateFormat(format);
			threadLocal.set(formatter);
		}
		return formatter;
	}

}
