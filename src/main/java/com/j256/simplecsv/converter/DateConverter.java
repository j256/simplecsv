package com.j256.simplecsv.converter;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;
import com.j256.simplecsv.annotations.CsvField;

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
public class DateConverter implements Converter<Date> {

	private String datePattern;

	private final ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat(datePattern);
		}
	};

	@Override
	public void configure(boolean allowNull, String format, long flags, Field field) {
		this.datePattern = format;
	}

	@Override
	public void javaToString(FieldInfo fieldInfo, Date value, StringBuilder sb) {
		if (value != null) {
			String str = threadLocal.get().format(value);
			sb.append(str);
		}
	}

	@Override
	public Date stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError)
			throws ParseException {
		if (value == null) {
			return null;
		} else {
			return threadLocal.get().parse(value);
		}
	}
}
