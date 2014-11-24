package com.j256.simplecsv;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

import com.j256.simplecsv.ParseError.ErrorType;
import com.j256.simplecsv.converter.BigDecimalConverter;
import com.j256.simplecsv.converter.BigIntegerConverter;
import com.j256.simplecsv.converter.BooleanConverter;
import com.j256.simplecsv.converter.ByteConverter;
import com.j256.simplecsv.converter.CharacterConverter;
import com.j256.simplecsv.converter.Converter;
import com.j256.simplecsv.converter.DateConverter;
import com.j256.simplecsv.converter.DoubleConverter;
import com.j256.simplecsv.converter.EnumConverter;
import com.j256.simplecsv.converter.FloatConverter;
import com.j256.simplecsv.converter.IntegerConverter;
import com.j256.simplecsv.converter.LongConverter;
import com.j256.simplecsv.converter.ShortConverter;
import com.j256.simplecsv.converter.StringConverter;
import com.j256.simplecsv.converter.UuidConverter;

/**
 * CSV reader and writer.
 * 
 * @param <T>
 *            Entity type that we are processing. It should have a public no-arg constructor so it can be created by
 *            this library.
 * 
 * @author graywatson
 */
public class CsvProcessor<T> {

	public static final char DEFAULT_CELL_SEPARATOR = ',';
	public static final char DEFAULT_CELL_QUOTE = '"';

	private char cellSeparator = DEFAULT_CELL_SEPARATOR;
	private char cellQuote = DEFAULT_CELL_QUOTE;
	private String rowTermination;
	private boolean allowPartialLines;

	private final FieldInfo[] fieldInfos;
	private final Constructor<T> constructor;

	private final Map<Class<?>, Converter<?, ?>> convertMap = new HashMap<Class<?>, Converter<?, ?>>();

	{
		convertMap.put(BigDecimal.class, BigDecimalConverter.getSingleton());
		convertMap.put(BigInteger.class, BigIntegerConverter.getSingleton());
		convertMap.put(Boolean.class, BooleanConverter.getSingleton());
		convertMap.put(boolean.class, BooleanConverter.getSingleton());
		convertMap.put(Byte.class, ByteConverter.getSingleton());
		convertMap.put(byte.class, ByteConverter.getSingleton());
		convertMap.put(Character.class, CharacterConverter.getSingleton());
		convertMap.put(char.class, CharacterConverter.getSingleton());
		convertMap.put(Date.class, DateConverter.getSingleton());
		convertMap.put(Double.class, DoubleConverter.getSingleton());
		convertMap.put(double.class, DoubleConverter.getSingleton());
		convertMap.put(Enum.class, EnumConverter.getSingleton());
		convertMap.put(Float.class, FloatConverter.getSingleton());
		convertMap.put(float.class, FloatConverter.getSingleton());
		convertMap.put(Integer.class, IntegerConverter.getSingleton());
		convertMap.put(int.class, IntegerConverter.getSingleton());
		convertMap.put(Long.class, LongConverter.getSingleton());
		convertMap.put(long.class, LongConverter.getSingleton());
		convertMap.put(Short.class, ShortConverter.getSingleton());
		convertMap.put(short.class, ShortConverter.getSingleton());
		convertMap.put(String.class, StringConverter.getSingleton());
		convertMap.put(UUID.class, UuidConverter.getSingleton());
	}

	/**
	 * Constructs a process with an entity class whose fields should be marked with {@link CvsField} annotations.
	 * 
	 * @throws IllegalArgumentException
	 */
	public CsvProcessor(Class<T> entityClass) throws IllegalArgumentException {
		List<FieldInfo> fieldInfos = new ArrayList<FieldInfo>();
		for (Class<?> clazz = entityClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
			for (Field field : entityClass.getDeclaredFields()) {
				Converter<?, ?> converter = convertMap.get(field.getType());
				FieldInfo fieldInfo = FieldInfo.fromField(field, converter);
				if (fieldInfo != null) {
					fieldInfos.add(fieldInfo);
					field.setAccessible(true);
				}
			}
		}
		this.fieldInfos = fieldInfos.toArray(new FieldInfo[fieldInfos.size()]);
		try {
			this.constructor = entityClass.getConstructor();
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not find public no-arg constructor for: " + entityClass);
		}
	}

	/**
	 * Process a header line and return the associated entity.
	 */
	public boolean validateHeader(String line) {
		// TODO:
		return false;
	}

	/**
	 * Read and process a line and return the associated entity.
	 */
	public T readLine(String line) throws ParseException {
		int fieldCount = 0;
		T result;
		try {
			result = constructor.newInstance();
		} catch (Exception e) {
			ParseException parseException =
					new ParseException("Could not construct instance of " + constructor.getDeclaringClass(), 0);
			parseException.initCause(e);
			throw parseException;
		}
		int linePos = 0;
		final ParseError parseError = new ParseError();
		for (FieldInfo fieldInfo : fieldInfos) {
			if (linePos == line.length()) {
				break;
			}
			parseError.reset();
			if (line.charAt(linePos) == cellQuote) {
				linePos++;
				linePos = processQuotedLine(line, 1, linePos, fieldInfo, result, parseError);
			} else {
				linePos = processUnquotedLine(line, 1, linePos, fieldInfo, result, parseError);
			}
			if (parseError.isError()) {
				throw new ParseException("Problems parsing line at position " + linePos + " (" + parseError + "): "
						+ line, linePos);
			}
		}
		if (fieldCount < fieldInfos.length && !allowPartialLines) {
			throw new ParseException("Line does not have " + fieldInfos.length + " cells: " + line, 0);
		}
		return result;
	}

	/**
	 * String that separates cells in out CSV input and output.
	 */
	public void setCellSeparator(char cellSeparator) {
		this.cellSeparator = cellSeparator;
	}

	/**
	 * Quote character that is used to wrap each cell.
	 */
	public void setCellQuote(char cellQuote) {
		this.cellQuote = cellQuote;
	}

	/**
	 * Sets the character which is written at the end of the row. Default is to use
	 * System.getProperty("line.separator");.
	 */
	public void setRowTermination(String rowTermination) {
		this.rowTermination = rowTermination;
	}

	/**
	 * Set to true to allow lines that do not have values for all of the cells. Otherwise an IllegalArgumentException is
	 * thrown.
	 */
	public void setAllowPartialLines(boolean allowPartialLines) {
		this.allowPartialLines = allowPartialLines;
	}

	private int processQuotedLine(String line, int lineNumber, int linePos, FieldInfo fieldInfo, Object result,
			ParseError parseError) {

		int fieldStart = linePos;

		// look for the next quote
		int fieldEnd = line.indexOf(cellQuote, linePos);
		if (fieldEnd < 0) {
			extractValue(line, lineNumber, fieldInfo, fieldStart, line.length(), result, parseError);
			return linePos;
		}
		linePos = fieldEnd + 1;
		if (linePos == line.length()) {
			extractValue(line, lineNumber, fieldInfo, fieldStart, linePos, result, parseError);
			return linePos;
		} else if (line.charAt(linePos) == cellSeparator) {
			extractValue(line, lineNumber, fieldInfo, fieldStart, linePos, result, parseError);
			return linePos + 1;
		}

		// must have a quote following a quote if there wasn't a cellSeparator
		if (line.charAt(linePos) != cellQuote) {
			parseError.setErrorType(ErrorType.INVALID_FORMAT);
			parseError.setMessage("quote '" + cellQuote + "' is not followed up separator '" + cellSeparator + "'");
			return linePos;
		}

		// need to build the string dynamically now
		StringBuilder sb = new StringBuilder(32);
		sb.append(line, fieldStart, linePos);
		// skip over quote
		linePos++;

		int start = linePos;
		while (linePos < line.length()) {
			// look for the next quote
			fieldEnd = line.indexOf(cellQuote, linePos);
			if (fieldEnd < 0) {
				sb.append(line, start, line.length());
				break;
			}
			linePos = fieldEnd + 1;
			if (linePos == line.length() || line.charAt(linePos) == cellSeparator) {
				sb.append(line, start, linePos);
				break;
			}

			if (line.charAt(linePos) != cellQuote) {
				parseError.setErrorType(ErrorType.INVALID_FORMAT);
				parseError.setMessage("quote '" + cellQuote + "' is not followed up separator '" + cellSeparator + "'");
				return linePos;
			}
			sb.append(line, start, linePos);
			linePos++;
			start = linePos;
		}

		String str = sb.toString();
		extractValue(str, lineNumber, fieldInfo, 0, str.length(), result, parseError);
		return linePos;
	}

	private int processUnquotedLine(String line, int lineNumber, int linePos, FieldInfo fieldInfo, Object result,
			ParseError parseError) {
		int fieldStart = linePos;
		linePos = line.indexOf(cellSeparator, fieldStart);
		if (linePos < 0) {
			linePos = line.length();
		}

		extractValue(line, lineNumber, fieldInfo, fieldStart, linePos, result, parseError);

		if (linePos < line.length()) {
			// skip over the separator
			linePos++;
		}
		return linePos;
	}

	private void extractValue(String line, int lineNum, FieldInfo fieldInfo, int fieldStart, int fieldEnd, Object obj,
			ParseError parseError) {

		String cellStr = line.substring(fieldStart, fieldEnd);
		if (fieldInfo.isTrimInput()) {
			cellStr = cellStr.trim();
		}

		if (cellStr.isEmpty() && fieldInfo.getDefaultValue() != null) {
			cellStr = fieldInfo.getDefaultValue();
		}

		Object value;
		try {
			value = fieldInfo.getConverter().stringToJava(line, lineNum, fieldInfo, cellStr, parseError);
		} catch (ParseException e) {
			// TODO: how to handle
			e.printStackTrace();
			return;
		} catch (Exception e) {
			// TODO: how to handle
			e.printStackTrace();
			return;
		}

		if (value == null && parseError.isError()) {
			// TODO: how to handle
			System.err.println("got error: " + parseError);
			return;
		}

		try {
			fieldInfo.getField().set(obj, value);
		} catch (Exception e) {
			// TODO: how to handle
			e.printStackTrace();
			return;
		}
	}

	@SuppressWarnings("unused")
	private static class CsvEscaper {

		private static final char CSV_DELIMITER = ',';
		private static final char CSV_QUOTE = '"';
		private static final String CSV_QUOTE_STR = String.valueOf(CSV_QUOTE);
		private static final char[] CSV_SEARCH_CHARS = new char[] { CSV_DELIMITER, CSV_QUOTE, CharUtils.CR,
				CharUtils.LF };

		public int translate(CharSequence input, Writer out) throws IOException {
			if (StringUtils.containsNone(input.toString(), CSV_SEARCH_CHARS)) {
				out.write(input.toString());
			} else {
				out.write(CSV_QUOTE);
				out.write(StringUtils.replace(input.toString(), CSV_QUOTE_STR, CSV_QUOTE_STR + CSV_QUOTE_STR));
				out.write(CSV_QUOTE);
			}
			return input.length();
		}
	}
}
