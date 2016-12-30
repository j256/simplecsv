package com.j256.simplecsv.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.j256.simplecsv.common.CsvColumn;
import com.j256.simplecsv.processor.ColumnInfo;
import com.j256.simplecsv.processor.ParseError;

/**
 * Converter for the Java java.util.Date type which uses the {@link SimpleDateFormat} -- don't worry I protect it for
 * reentrance.
 * 
 * <p>
 * The {@link CsvColumn#format()} parameter can be set the {@link SimpleDateFormat} format string to read and write the
 * date.
 * </p>
 * 
 * @author graywatson
 */
public class DateConverter implements Converter<Date, String> {

	/**
	 * Default {@link SimpleDateFormat} format pattern used to read/write java.util.Date types.
	 */
	public static final String DEFAULT_DATE_PATTERN = "MM/dd/yyyy";

	/*
	 * We need to do this because SimpleDateFormat is not thread safe.
	 */
	private final ThreadLocal<OurDateFormatter> threadLocal = new ThreadLocal<OurDateFormatter>() {
		@Override
		protected OurDateFormatter initialValue() {
			return new OurDateFormatter();
		}
	};

	private static final DateConverter singleton = new DateConverter();

	/**
	 * Get singleton for class.
	 */
	public static DateConverter getSingleton() {
		return singleton;
	}

	@Override
	public String configure(String format, long flags, ColumnInfo<Date> fieldInfo) {
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
	public boolean isAlwaysTrimInput() {
		return false;
	}

	@Override
	public String javaToString(ColumnInfo<Date> columnInfo, Date value) {
		if (value == null) {
			return null;
		} else {
			String datePattern = (String) columnInfo.getConfigInfo();
			return threadLocal.get().format(datePattern, value);
		}
	}

	@Override
	public Date stringToJava(String line, int lineNumber, int linePos, ColumnInfo<Date> columnInfo, String value,
			ParseError parseError) throws ParseException {
		if (value.isEmpty()) {
			return null;
		}
		String datePattern = (String) columnInfo.getConfigInfo();
		try {
			return threadLocal.get().parse(datePattern, value);
		} catch (ParseException pe) {
			ParseException wrappedPe =
					new ParseException("Problem when using date-pattern: " + datePattern, pe.getErrorOffset());
			wrappedPe.initCause(pe);
			throw wrappedPe;
		}
	}

	/**
	 * Formatter which saves a single SimpleDateFormat object in a thread-local.
	 */
	private static class OurDateFormatter {

		private String format;
		private SimpleDateFormat formatter;

		public Date parse(String format, String dateString) throws ParseException {
			return checkFormatter(format).parse(dateString);
		}

		public String format(String format, Date date) {
			return checkFormatter(format).format(date);
		}

		private SimpleDateFormat checkFormatter(String format) {
			// if we have no format or the format doesn't matched the cached one
			if (this.format == null || !this.format.equals(format)) {
				this.formatter = new SimpleDateFormat(format);
				this.format = format;
			}
			return this.formatter;
		}
	}
}
