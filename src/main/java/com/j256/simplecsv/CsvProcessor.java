package com.j256.simplecsv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.j256.simplecsv.ParseError.ErrorType;
import com.j256.simplecsv.converter.Converter;
import com.j256.simplecsv.converter.ConverterUtils;

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
	private String lineTermination = System.getProperty("line.separator");
	private boolean allowPartialLines;

	private final FieldInfo[] fieldInfos;
	private final Constructor<T> constructor;

	private final Map<Class<?>, Converter<?, ?>> converterMap = new HashMap<Class<?>, Converter<?, ?>>();

	{
		ConverterUtils.addInternalConverters(converterMap);
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
				Converter<?, ?> converter = converterMap.get(field.getType());
				// NOTE: converter could be null in which case the CsvField.converterClass must be set
				@SuppressWarnings("unchecked")
				Converter<Object, Object> castConverter = (Converter<Object, Object>) converter;
				FieldInfo fieldInfo = FieldInfo.fromField(field, castConverter);
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
	 * Register a converter class for all instances of the class argument. The converter can also be specified with the
	 * {@link CsvField#converterClass()} annotation field.
	 */
	public <FT> void registerConverter(Class<FT> clazz, Converter<FT, ?> converter) {
		converterMap.put(clazz, converter);
	}

	/**
	 * Read in all of the entities in the file passed in.
	 * 
	 * @param file
	 *            Where to read the header and entities from. It will be closed when the method returns.
	 * @param firstLineHeader
	 *            Set to true to ignore the first line as the header.
	 * @param validateHeader
	 *            Set to true if the first line will be read and validated. If the first line header does not match what
	 *            it should then null will be returned.
	 * @param parseErrors
	 *            If not null, any errors will be added to the collection and null will be returned. If validateHeader
	 *            is true and the header does not match then no additional lines will be returned. If this is null then
	 *            a ParseException will be thrown on parsing problems.
	 * @return A list of entities read in or null if validateHeader is true and the first-line header was not valid.
	 * @throws ParseException
	 *             Thrown on any parsing problems. If parseErrors is not null then parse errors will be added there and
	 *             an exception should be thrown.
	 * @throws IOException
	 *             If there are any IO exceptions thrown when reading.
	 */
	public List<T> readAll(File file, boolean firstLineHeader, boolean validateHeader,
			Collection<ParseError> parseErrors) throws IOException, ParseException {
		return readAll(new FileReader(file), firstLineHeader, validateHeader, parseErrors);
	}

	/**
	 * Read in all of the entities in the reader passed in. It will use an internal buffered reader.
	 * 
	 * @param reader
	 *            Where to read the header and entities from. It will be closed when the method returns.
	 * @param firstLineHeader
	 *            Set to true to ignore the first line as the header.
	 * @param validateHeader
	 *            Set to true if the first line will be read and validated. If the first line header does not match what
	 *            it should then null will be returned.
	 * @param parseErrors
	 *            If not null, any errors will be added to the collection and null will be returned. If validateHeader
	 *            is true and the header does not match then no additional lines will be returned. If this is null then
	 *            a ParseException will be thrown on parsing problems.
	 * @return A list of entities read in or null if parseErrors is not null.
	 * @throws ParseException
	 *             Thrown on any parsing problems. If parseErrors is not null then parse errors will be added there and
	 *             an exception should be thrown.
	 * @throws IOException
	 *             If there are any IO exceptions thrown when reading.
	 */
	public List<T> readAll(Reader reader, boolean firstLineHeader, boolean validateHeader,
			Collection<ParseError> parseErrors) throws IOException, ParseException {
		BufferedReader bufferedReader = new BufferedReader(reader);
		try {
			ParseError parseError = new ParseError();
			if (firstLineHeader) {
				String line = bufferedReader.readLine();
				if (validateHeader) {
					if (line == null) {
						if (parseErrors != null) {
							parseError.setErrorType(ErrorType.NO_HEADER);
							parseErrors.add(parseError);
						}
						return null;
					} else if (!validateHeader(line, parseError)) {
						if (parseErrors != null) {
							parseErrors.add(parseError);
						}
						return null;
					}
				} else if (line == null) {
					return Collections.emptyList();
				}
			}
			List<T> results = new ArrayList<T>();
			if (parseErrors == null) {
				parseError = null;
			}
			while (true) {
				String line = bufferedReader.readLine();
				if (line == null) {
					return results;
				}
				T result = processRow(line, parseError);
				if (result == null) {
					// should not get here but let's be careful
					if (parseErrors != null) {
						parseErrors.add(parseError);
						parseError = new ParseError();
					}
				} else {
					results.add(result);
				}
			}
		} finally {
			bufferedReader.close();
		}
	}

	/**
	 * Process a line and divide it up into a series of quoted fields.
	 * 
	 * @param bufferedReader
	 *            Where to read the header from.
	 * @param parseError
	 *            If not null, this will be set with the first parse error and it will return null. If this is null then
	 *            a ParseException will be thrown instead.
	 * @return Array of header cell names or null on error.
	 * @throws ParseException
	 *             Thrown on any parsing problems. If parseError is not null then the error will be added there and an
	 *             exception should be thrown.
	 * @throws IOException
	 *             If there are any IO exceptions thrown when reading.
	 */
	public String[] readHeader(BufferedReader bufferedReader, ParseError parseError) throws ParseException, IOException {
		return processHeader(bufferedReader.readLine(), parseError);
	}

	/**
	 * Process a header line and return the associated entity.
	 * 
	 * @param line
	 *            Line to process to get our validate our header.
	 * @param parseError
	 *            If not null, this will be set with the first parse error and it will return null. If this is null then
	 *            a ParseException will be thrown instead.
	 * @return true if the header matched the cell names configured here otherwise false.
	 * @throws ParseException
	 *             Thrown on any parsing problems. If parseError is not null then the error will be added there and an
	 *             exception should be thrown.
	 */
	public boolean validateHeader(String line, ParseError parseError) throws ParseException {
		String[] columns = processHeader(line, parseError);
		if (columns.length != fieldInfos.length) {
			if (parseError != null) {
				parseError.setErrorType(ErrorType.INVALID_HEADER);
				parseError.setMessage("got " + columns.length + " header columns, expected " + fieldInfos.length);
			}
			return false;
		}
		for (int i = 0; i < columns.length; i++) {
			if (columns[i] == null || !columns[i].equals(fieldInfos[i].getCellName())) {
				if (parseError != null) {
					parseError.setErrorType(ErrorType.INVALID_HEADER);
					parseError.setMessage("got column name '" + columns[i] + "', expected '"
							+ fieldInfos[i].getCellName() + "'");
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * Process a line and divide it up into a series of quoted fields.
	 * 
	 * @param line
	 *            Line to process looking for header.
	 * @param parseError
	 *            If not null, this will be set with the first parse error and it will return null. If this is null then
	 *            a ParseException will be thrown instead.
	 * @return Returns an array of processed header names entity or null if an error and parseError has been set. The
	 *         array will be the same length as the number of configured fields so some elements may be null.
	 * @throws ParseException
	 *             Thrown on any parsing problems. If parseError is not null then the error will be added there and an
	 *             exception should be thrown.
	 */
	public String[] processHeader(String line, ParseError parseError) throws ParseException {
		String[] result = new String[fieldInfos.length];
		StringBuilder sb = new StringBuilder(32);
		int linePos = 0;
		ParseError localParseError = parseError;
		if (localParseError == null) {
			localParseError = new ParseError();
		}
		for (int i = 0; i < fieldInfos.length; i++) {
			boolean atEnd = (linePos == line.length());
			localParseError.reset();
			sb.setLength(0);
			if (linePos < line.length() && line.charAt(linePos) == cellQuote) {
				linePos = processQuotedLine(line, 1, linePos, null, null, sb, localParseError);
			} else {
				linePos = processUnquotedLine(line, 1, linePos, null, null, sb, localParseError);
			}
			if (localParseError.isError()) {
				if (localParseError == parseError) {
					return null;
				} else {
					throw new ParseException("Problems parsing line at position " + linePos + " (" + localParseError
							+ "): " + line, linePos);
				}
			}
			result[i] = sb.toString();
			if (atEnd) {
				break;
			}
		}
		if (linePos < line.length()) {
			throw new ParseException("Line has extra information past last column: " + line, linePos);
		}
		return result;
	}

	/**
	 * Read and process a line and return the associated entity.
	 * 
	 * @param line
	 *            to process to build our entity.
	 * @param parseError
	 *            If not null, this will be set with the first parse error and it will return null. If this is null then
	 *            a ParseException will be thrown instead.
	 * @return Returns a processed entity or null if an error and parseError has been set.
	 * @throws ParseException
	 *             Thrown on any parsing problems. If parseError is not null then the error will be added there and an
	 *             exception should be thrown.
	 */
	public T processRow(String line, ParseError parseError) throws ParseException {
		int fieldCount = 0;
		T target;
		try {
			target = constructor.newInstance();
		} catch (Exception e) {
			ParseException parseException =
					new ParseException("Could not construct instance of " + constructor.getDeclaringClass(), 0);
			parseException.initCause(e);
			throw parseException;
		}
		int linePos = 0;
		ParseError localParseError = parseError;
		if (localParseError == null) {
			localParseError = new ParseError();
		}
		for (FieldInfo fieldInfo : fieldInfos) {
			boolean atEnd = (linePos == line.length());
			localParseError.reset();
			if (linePos < line.length() && line.charAt(linePos) == cellQuote) {
				linePos = processQuotedLine(line, 1, linePos, fieldInfo, target, null, localParseError);
			} else {
				linePos = processUnquotedLine(line, 1, linePos, fieldInfo, target, null, localParseError);
			}
			if (localParseError.isError()) {
				if (localParseError == parseError) {
					// parseError has the error information
					return null;
				} else {
					throw new ParseException("Problems parsing line at position " + linePos + " (" + localParseError
							+ "): " + line, linePos);
				}
			}
			fieldCount++;
			if (atEnd) {
				break;
			}
		}
		if (fieldCount < fieldInfos.length && !allowPartialLines) {
			throw new ParseException("Line does not have " + fieldInfos.length + " cells: " + line, linePos);
		}
		if (linePos < line.length()) {
			throw new ParseException("Line has extra information past last column: " + line, linePos);
		}
		return target;
	}

	/**
	 * Write a collection of entities to the writer.
	 * 
	 * @param file
	 *            Where to write the header and entities.
	 * @param entities
	 *            Collection of entities to write to the writer.
	 * @param writeHeader
	 *            Set to true to write header at the start of the output file.
	 * @throws IOException
	 *             If there are any IO exceptions thrown when writing.
	 */
	public void writeAll(File file, Collection<T> entities, boolean writeHeader) throws IOException {
		writeAll(new FileWriter(file), entities, writeHeader);
	}

	/**
	 * Write a header and then the collection of entities to the writer.
	 * 
	 * @param writer
	 *            Where to write the header and entities. It will be closed before this method returns.
	 * @param entities
	 *            Collection of entities to write to the writer.
	 * @param writeHeader
	 *            Set to true to write header at the start of the writer.
	 * @throws IOException
	 *             If there are any IO exceptions thrown when writing.
	 */
	public void writeAll(Writer writer, Collection<T> entities, boolean writeHeader) throws IOException {
		BufferedWriter bufferedWriter = new BufferedWriter(writer);
		try {
			if (writeHeader) {
				writeHeader(bufferedWriter, true);
			}
			for (T entity : entities) {
				String line = buildLine(entity, true);
				bufferedWriter.write(line);
			}
		} finally {
			bufferedWriter.close();
		}
	}

	/**
	 * Write the header line to the writer with a newline.
	 * 
	 * @param bufferedWriter
	 *            Where to write our header information.
	 * @param appendLineTermination
	 *            Set to true to add the newline to the end of the line.
	 * @throws IOException
	 *             If there are any IO exceptions thrown when writing.
	 */
	public void writeHeader(BufferedWriter bufferedWriter, boolean appendLineTermination) throws IOException {
		bufferedWriter.write(buildHeaderLine(appendLineTermination));
	}

	/**
	 * Convert the entity into a string suitable to be written.
	 * 
	 * @param appendLineTermination
	 *            Set to true to add the newline to the end of the line.
	 */
	public String buildHeaderLine(boolean appendLineTermination) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (FieldInfo fieldInfo : fieldInfos) {
			if (first) {
				first = false;
			} else {
				sb.append(cellSeparator);
			}
			String header = fieldInfo.getCellName();
			// need to protect the cell if it contains a quote
			if (header.indexOf(cellQuote) >= 0) {
				writeQuoted(sb, header);
				continue;
			}
			sb.append(cellQuote);
			sb.append(header);
			sb.append(cellQuote);
		}
		if (appendLineTermination) {
			sb.append(lineTermination);
		}
		return sb.toString();
	}

	/**
	 * Convert the entity into a string suitable to be written.
	 */
	public String buildLine(T entity, boolean appendLineTermination) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (FieldInfo fieldInfo : fieldInfos) {
			if (first) {
				first = false;
			} else {
				sb.append(cellSeparator);
			}
			Field field = fieldInfo.getField();
			Object value;
			try {
				value = field.get(entity);
			} catch (Exception e) {
				throw new IllegalStateException("Could not get value from entity field: " + field);
			}
			@SuppressWarnings("unchecked")
			Converter<Object, Object> castConverter = (Converter<Object, Object>) fieldInfo.getConverter();
			String str = castConverter.javaToString(fieldInfo, value);
			// need to protect the cell if it contains a quote
			if (str.indexOf(cellQuote) >= 0) {
				writeQuoted(sb, str);
				continue;
			}
			boolean needsQuotes = fieldInfo.isNeedsQuotes();
			if (!needsQuotes) {
				for (int i = 0; i < str.length(); i++) {
					char ch = str.charAt(i);
					if (ch == cellSeparator || ch == '\r' || ch == '\n' || ch == '\t' || ch == '\b') {
						needsQuotes = true;
						break;
					}
				}
			}
			if (needsQuotes) {
				sb.append(cellQuote);
			}
			sb.append(str);
			if (needsQuotes) {
				sb.append(cellQuote);
			}
		}
		if (appendLineTermination) {
			sb.append(lineTermination);
		}
		return sb.toString();
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
	public void setLineTermination(String lineTermination) {
		this.lineTermination = lineTermination;
	}

	/**
	 * Set to true to allow lines that do not have values for all of the cells. Otherwise an IllegalArgumentException is
	 * thrown.
	 */
	public void setAllowPartialLines(boolean allowPartialLines) {
		this.allowPartialLines = allowPartialLines;
	}

	private int processQuotedLine(String line, int lineNumber, int linePos, FieldInfo fieldInfo, Object target,
			StringBuilder headerSb, ParseError parseError) {

		// linePos is pointing at the first quote, move past it
		linePos++;
		int fieldStart = linePos;
		int fieldEnd = linePos;

		StringBuilder sb = null;
		while (linePos < line.length()) {

			// look for the next quote
			fieldEnd = line.indexOf(cellQuote, linePos);
			if (fieldEnd < 0) {
				parseError.setErrorType(ErrorType.TRUNCATED_VALUE);
				parseError.setMessage("Field not terminated with quote '" + cellQuote + "'");
				parseError.setLineNumber(lineNumber);
				parseError.setLinePos(linePos);
				return line.length();
			}

			linePos = fieldEnd + 1;
			if (linePos == line.length()) {
				break;
			} else if (line.charAt(linePos) == cellSeparator) {
				linePos++;
				break;
			}

			// must have a quote following a quote if there wasn't a cellSeparator
			if (line.charAt(linePos) != cellQuote) {
				parseError.setErrorType(ErrorType.INVALID_FORMAT);
				parseError.setMessage("quote '" + cellQuote + "' is not followed up separator '" + cellSeparator + "'");
				parseError.setLineNumber(lineNumber);
				parseError.setLinePos(linePos);
				return linePos;
			}

			fieldEnd = linePos;
			linePos++;
			if (linePos == line.length()) {
				break;
			}
			if (line.charAt(linePos) == cellSeparator) {
				linePos++;
				break;
			}

			// need to build the string dynamically now
			if (sb == null) {
				sb = new StringBuilder(32);
			}
			// add to the string-builder the field + 1 quote
			sb.append(line, fieldStart, fieldEnd);
			// line-pos is pointing past 2nd (maybe 3rd) quote
			fieldStart = linePos;
		}

		if (sb == null) {
			if (headerSb == null) {
				extractAndAssignValue(line, lineNumber, fieldInfo, fieldStart, fieldEnd, target, parseError);
			} else {
				headerSb.append(line, fieldStart, fieldEnd);
			}
		} else {
			sb.append(line, fieldStart, fieldEnd);
			String str = sb.toString();
			if (headerSb == null) {
				extractAndAssignValue(str, lineNumber, fieldInfo, 0, str.length(), target, parseError);
			} else {
				headerSb.append(sb);
			}
		}
		return linePos;
	}

	private int processUnquotedLine(String line, int lineNumber, int linePos, FieldInfo fieldInfo, Object target,
			StringBuilder headerSb, ParseError parseError) {
		int fieldStart = linePos;
		linePos = line.indexOf(cellSeparator, fieldStart);
		if (linePos < 0) {
			linePos = line.length();
		}

		if (headerSb == null) {
			extractAndAssignValue(line, lineNumber, fieldInfo, fieldStart, linePos, target, parseError);
		} else {
			headerSb.append(line, fieldStart, linePos);
		}

		if (linePos < line.length()) {
			// skip over the separator
			linePos++;
		}
		return linePos;
	}

	private void writeQuoted(StringBuilder sb, String str) {
		sb.append(cellQuote);
		int start = 0;
		while (true) {
			int linePos = str.indexOf(cellQuote, start);
			if (linePos < 0) {
				sb.append(str, start, str.length());
				break;
			}
			// move past the quote so we can output it
			linePos++;
			sb.append(str, start, linePos);
			// output another quote
			sb.append(cellQuote);
			start = linePos;
		}
		sb.append(cellQuote);
	}

	/**
	 * Extract a value from the line, convert it into its java equivalent, and assign it to our target object.
	 */
	private void extractAndAssignValue(String line, int lineNumber, FieldInfo fieldInfo, int fieldStart, int fieldEnd,
			Object target, ParseError parseError) {
		Object value = extractValue(line, lineNumber, fieldInfo, fieldStart, fieldEnd, target, parseError);
		if (value == null) {
			// either error or no value
			return;
		}
		try {
			fieldInfo.getField().set(target, value);
		} catch (Exception e) {
			parseError.setErrorType(ErrorType.INTERNAL_ERROR);
			parseError.setMessage(e.getMessage());
			parseError.setLineNumber(lineNumber);
			parseError.setLinePos(fieldStart);
		}
	}

	/**
	 * Extract a value from the line and convert it into its java equivalent.
	 */
	private Object extractValue(String line, int lineNumber, FieldInfo fieldInfo, int fieldStart, int fieldEnd,
			Object target, ParseError parseError) {

		String cellStr = line.substring(fieldStart, fieldEnd);
		if (fieldInfo.isTrimInput()) {
			cellStr = cellStr.trim();
		}
		if (cellStr.isEmpty() && fieldInfo.getDefaultValue() != null) {
			cellStr = fieldInfo.getDefaultValue();
		}
		if (cellStr.isEmpty() && fieldInfo.isRequired()) {
			parseError.setErrorType(ErrorType.NO_HEADER);
			parseError.setLineNumber(lineNumber);
			parseError.setLinePos(fieldStart);
			return null;
		}

		try {
			return fieldInfo.getConverter().stringToJava(line, lineNumber, fieldInfo, cellStr, parseError);
		} catch (ParseException e) {
			parseError.setErrorType(ErrorType.INVALID_FORMAT);
			parseError.setMessage(e.getMessage());
			parseError.setLineNumber(lineNumber);
			parseError.setLinePos(fieldStart);
			return null;
		} catch (Exception e) {
			parseError.setErrorType(ErrorType.INTERNAL_ERROR);
			parseError.setMessage(e.getMessage());
			parseError.setLineNumber(lineNumber);
			parseError.setLinePos(fieldStart);
			return null;
		}
	}
}
