package com.j256.simplecsv.processor;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.j256.simplecsv.common.CsvField;
import com.j256.simplecsv.converter.Converter;
import com.j256.simplecsv.converter.ConverterUtils;
import com.j256.simplecsv.processor.ParseError.ErrorType;

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

	/**
	 * Default separator character for columns. This can be changed with {@link #setColumnSeparator(char)}.
	 */
	public static final char DEFAULT_COLUMN_SEPARATOR = ',';
	/**
	 * Default quote character for columns to wrap them if they have special characters. This can be changed with
	 * {@link #setColumnQuote(char)}.
	 */
	public static final char DEFAULT_COLUMN_QUOTE = '"';
	/**
	 * Default line termination string to be written at the end of CSV lines. This can be changed with
	 * {@link #setLineTermination(String)}.
	 */
	public static final String DEFAULT_LINE_TERMINATION = System.getProperty("line.separator");

	private char columnSeparator = DEFAULT_COLUMN_SEPARATOR;
	private char columnQuote = DEFAULT_COLUMN_QUOTE;
	private String lineTermination = DEFAULT_LINE_TERMINATION;
	private boolean allowPartialLines;
	private boolean alwaysTrimInput;

	private final ColumnInfo[] columnInfos;
	private final Constructor<T> constructor;

	private final Map<Class<?>, Converter<?, ?>> converterMap = new HashMap<Class<?>, Converter<?, ?>>();

	{
		ConverterUtils.addInternalConverters(converterMap);
	}

	/**
	 * Constructs a processor with an entity class whose fields should be marked with {@link CsvField} annotations. The
	 * entity-class must also define a public no-arg contructor so the processor can instantiate them using reflection.
	 */
	public CsvProcessor(Class<T> entityClass) throws IllegalArgumentException {
		List<ColumnInfo> columnInfos = new ArrayList<ColumnInfo>();
		Set<Field> knownFields = new HashSet<Field>();
		for (Class<?> clazz = entityClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
			for (Field field : entityClass.getDeclaredFields()) {
				if (!knownFields.add(field)) {
					continue;
				}
				Converter<?, ?> converter = converterMap.get(field.getType());
				// NOTE: converter could be null in which case the CsvField.converterClass must be set
				@SuppressWarnings("unchecked")
				Converter<Object, Object> castConverter = (Converter<Object, Object>) converter;
				ColumnInfo columnInfo = ColumnInfo.fromField(field, castConverter);
				if (columnInfo != null) {
					columnInfos.add(columnInfo);
					field.setAccessible(true);
				}
			}
		}
		this.columnInfos = columnInfos.toArray(new ColumnInfo[columnInfos.size()]);
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
			ParseError parseError = null;
			if (parseErrors != null) {
				parseError = new ParseError();
			}
			if (firstLineHeader) {
				if (readHeader(bufferedReader, validateHeader, parseError) == null) {
					if (parseError != null && parseError.isError()) {
						parseErrors.add(parseError);
					}
					return null;
				}
			}
			List<T> results = new ArrayList<T>();
			while (true) {
				if (parseError != null) {
					parseError.reset();
				}
				T result = readRow(bufferedReader, parseError);
				if (result != null) {
					results.add(result);
				} else if (parseError != null && parseError.isError()) {
					// if there was an error then add it to the list
					parseErrors.add(parseError);
				} else {
					// if no error (and no exception) then EOF
					return results;
				}
			}
		} finally {
			bufferedReader.close();
		}
	}

	/**
	 * Read in a line and process it as a CSV header.
	 * 
	 * @param bufferedReader
	 *            Where to read the header from.
	 * @param parseError
	 *            If not null, this will be set with the first parse error and it will return null. If this is null then
	 *            a ParseException will be thrown instead.
	 * @param validate
	 *            Validate the header after it is read. This will return null if the header is not valid.
	 * @return Array of header column names or null on error.
	 * @throws ParseException
	 *             Thrown on any parsing problems. If parseError is not null then the error will be added there and an
	 *             exception should be thrown.
	 * @throws IOException
	 *             If there are any IO exceptions thrown when reading.
	 */
	public String[] readHeader(BufferedReader bufferedReader, boolean validate, ParseError parseError)
			throws ParseException, IOException {
		String header = bufferedReader.readLine();
		if (header == null) {
			if (parseError == null) {
				throw new ParseException("no header line read", 0);
			} else {
				parseError.setErrorType(ErrorType.NO_HEADER);
				parseError.setLineNumber(1);
				return null;
			}
		}
		String[] columns = processHeader(header, parseError);
		if (columns == null) {
			return null;
		} else if (validate && !validateHeaderColumns(columns, parseError)) {
			if (parseError == null) {
				throw new ParseException("header line is not valid: " + header, 0);
			} else {
				return null;
			}
		}
		return columns;
	}

	/**
	 * Read an entity line from the reader.
	 * 
	 * @param bufferedReader
	 *            Where to read the row from.
	 * @param parseError
	 *            If not null, this will be set with the first parse error and it will return null. If this is null then
	 *            a ParseException will be thrown instead.
	 * @return Entity read in or null on EOF or error. Check {@link ParseError#isError()} to see if it was an error or
	 *         EOF.
	 * @throws ParseException
	 *             Thrown on any parsing problems. If parseError is not null then the error will be added there and an
	 *             exception should be thrown.
	 * @throws IOException
	 *             If there are any IO exceptions thrown when reading.
	 */
	public T readRow(BufferedReader bufferedReader, ParseError parseError) throws ParseException, IOException {
		String line = bufferedReader.readLine();
		if (line == null) {
			return null;
		} else {
			return processRow(line, parseError);
		}
	}

	/**
	 * Process a header row and return the associated entity.
	 * 
	 * @param line
	 *            Line to process to get our validate our header.
	 * @param parseError
	 *            If not null, this will be set with the first parse error and it will return null. If this is null then
	 *            a ParseException will be thrown instead.
	 * @return true if the header matched the column names configured here otherwise false.
	 * @throws ParseException
	 *             Thrown on any parsing problems. If parseError is not null then the error will be added there and an
	 *             exception should be thrown.
	 */
	public boolean validateHeader(String line, ParseError parseError) throws ParseException {
		String[] columns = processHeader(line, parseError);
		return validateHeaderColumns(columns, parseError);
	}

	/**
	 * Validate header columns returned by {@link #processHeader(String, ParseError)}.
	 * 
	 * @param columns
	 *            Array of columns to validate.
	 * @param parseError
	 *            If not null, this will be set with the first parse error and it will return null. If this is null then
	 *            a ParseException will be thrown instead.
	 * @return true if the header matched the column names configured here otherwise false.
	 * @throws ParseException
	 *             Thrown on any parsing problems. If parseError is not null then the error will be added there and an
	 *             exception should be thrown.
	 */
	public boolean validateHeaderColumns(String[] columns, ParseError parseError) {
		if (columns.length != columnInfos.length) {
			if (parseError != null) {
				parseError.setErrorType(ErrorType.INVALID_HEADER);
				parseError.setMessage("got " + columns.length + " header columns, expected " + columnInfos.length);
			}
			return false;
		}
		for (int i = 0; i < columns.length; i++) {
			if (columns[i] == null || !columns[i].equals(columnInfos[i].getColumnName())) {
				if (parseError != null) {
					parseError.setErrorType(ErrorType.INVALID_HEADER);
					parseError.setMessage("got column name '" + columns[i] + "', expected '"
							+ columnInfos[i].getColumnName() + "'");
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
		String[] headerColumns = new String[columnInfos.length];
		StringBuilder sb = new StringBuilder(32);
		int linePos = 0;
		ParseError localParseError = parseError;
		if (localParseError == null) {
			localParseError = new ParseError();
		}
		for (int i = 0; i < columnInfos.length; i++) {
			boolean atEnd = (linePos == line.length());
			localParseError.reset();
			sb.setLength(0);
			if (linePos < line.length() && line.charAt(linePos) == columnQuote) {
				linePos = processQuotedLine(line, 1, linePos, null, null, sb, localParseError);
			} else {
				linePos = processUnquotedLine(line, 1, linePos, null, null, sb, localParseError);
			}
			if (localParseError.isError()) {
				if (localParseError == parseError) {
					return null;
				} else {
					throw new ParseException("Problems parsing header line at position " + linePos + " ("
							+ localParseError + "): " + line, linePos);
				}
			}
			headerColumns[i] = sb.toString();
			if (atEnd) {
				break;
			}
		}
		if (linePos < line.length()) {
			throw new ParseException("Line has extra information past last column: " + line, linePos);
		}
		return headerColumns;
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
		for (ColumnInfo columnInfo : columnInfos) {
			// we have to do this because a blank column may be ok
			boolean atEnd = (linePos == line.length());
			localParseError.reset();
			if (linePos < line.length() && line.charAt(linePos) == columnQuote) {
				linePos = processQuotedLine(line, 1, linePos, columnInfo, target, null, localParseError);
			} else {
				linePos = processUnquotedLine(line, 1, linePos, columnInfo, target, null, localParseError);
			}
			if (localParseError.isError()) {
				if (localParseError == parseError) {
					// parseError has the error information
					return null;
				} else {
					throw new ParseException("Problems parsing line at position " + linePos + " for type "
							+ columnInfo.getField().getType().getSimpleName() + " (" + localParseError + "): " + line,
							linePos);
				}
			}
			fieldCount++;
			if (atEnd) {
				break;
			}
			// NOTE: we can't break here if we are at the end of line because might be blank field
		}
		if (fieldCount < columnInfos.length && !allowPartialLines) {
			throw new ParseException("Line does not have " + columnInfos.length + " columns: " + line, linePos);
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
				writeRow(bufferedWriter, entity, true);
			}
		} finally {
			bufferedWriter.close();
		}
	}

	/**
	 * Write the header line to the writer.
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
	 * Write an entity row to the writer.
	 * 
	 * @param bufferedWriter
	 *            Where to write our header information.
	 * @param entity
	 *            The entity we are writing to the buffered writer.
	 * @param appendLineTermination
	 *            Set to true to add the newline to the end of the line.
	 * @throws IOException
	 *             If there are any IO exceptions thrown when writing.
	 */
	public void writeRow(BufferedWriter bufferedWriter, T entity, boolean appendLineTermination) throws IOException {
		String line = buildLine(entity, appendLineTermination);
		bufferedWriter.write(line);
	}

	/**
	 * Build and return a header string made up of quoted column names.
	 * 
	 * @param appendLineTermination
	 *            Set to true to add the newline to the end of the line.
	 */
	public String buildHeaderLine(boolean appendLineTermination) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (ColumnInfo columnInfo : columnInfos) {
			if (first) {
				first = false;
			} else {
				sb.append(columnSeparator);
			}
			String header = columnInfo.getColumnName();
			// need to protect the column if it contains a quote
			if (header.indexOf(columnQuote) >= 0) {
				writeQuoted(sb, header);
				continue;
			}
			sb.append(columnQuote);
			sb.append(header);
			sb.append(columnQuote);
		}
		if (appendLineTermination) {
			sb.append(lineTermination);
		}
		return sb.toString();
	}

	/**
	 * Convert the entity into a string of column values.
	 * 
	 * @param appendLineTermination
	 *            Set to true to add the newline to the end of the line.
	 */
	public String buildLine(T entity, boolean appendLineTermination) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (ColumnInfo columnInfo : columnInfos) {
			if (first) {
				first = false;
			} else {
				sb.append(columnSeparator);
			}
			Field field = columnInfo.getField();
			Object value;
			try {
				value = field.get(entity);
			} catch (Exception e) {
				throw new IllegalStateException("Could not get value from entity field: " + field);
			}
			@SuppressWarnings("unchecked")
			Converter<Object, Object> castConverter = (Converter<Object, Object>) columnInfo.getConverter();
			String str = castConverter.javaToString(columnInfo, value);
			boolean needsQuotes = columnInfo.isNeedsQuotes();
			if (str != null) {
				// need to protect the column if it contains a quote
				if (str.indexOf(columnQuote) >= 0) {
					writeQuoted(sb, str);
					continue;
				}
				if (!needsQuotes) {
					for (int i = 0; i < str.length(); i++) {
						char ch = str.charAt(i);
						if (ch == columnSeparator || ch == '\r' || ch == '\n' || ch == '\t' || ch == '\b') {
							needsQuotes = true;
							break;
						}
					}
				}
			}
			if (needsQuotes) {
				sb.append(columnQuote);
			}
			if (str != null) {
				sb.append(str);
			}
			if (needsQuotes) {
				sb.append(columnQuote);
			}
		}
		if (appendLineTermination) {
			sb.append(lineTermination);
		}
		return sb.toString();
	}

	/**
	 * String that separates columns in out CSV input and output.
	 */
	public void setColumnSeparator(char columnSeparator) {
		this.columnSeparator = columnSeparator;
	}

	/**
	 * Quote character that is used to wrap each column.
	 */
	public void setColumnQuote(char columnQuote) {
		this.columnQuote = columnQuote;
	}

	/**
	 * Sets the character which is written at the end of the row. Default is to use
	 * System.getProperty("line.separator");.
	 */
	public void setLineTermination(String lineTermination) {
		this.lineTermination = lineTermination;
	}

	/**
	 * Set to true to allow lines that do not have values for all of the columns. Otherwise an IllegalArgumentException
	 * is thrown.
	 */
	public void setAllowPartialLines(boolean allowPartialLines) {
		this.allowPartialLines = allowPartialLines;
	}

	/**
	 * Set to true to always call {@link String#trim()} on data input columns to remove any spaces from the start or
	 * end.
	 */
	public void setAlwaysTrimInput(boolean alwaysTrimInput) {
		this.alwaysTrimInput = alwaysTrimInput;
	}

	private int processQuotedLine(String line, int lineNumber, int linePos, ColumnInfo columnInfo, Object target,
			StringBuilder headerSb, ParseError parseError) {

		// linePos is pointing at the first quote, move past it
		linePos++;
		int fieldStart = linePos;
		int fieldEnd = linePos;

		StringBuilder sb = null;
		while (linePos < line.length()) {

			// look for the next quote
			fieldEnd = line.indexOf(columnQuote, linePos);
			if (fieldEnd < 0) {
				parseError.setErrorType(ErrorType.TRUNCATED_VALUE);
				parseError.setMessage("Field not terminated with quote '" + columnQuote + "'");
				parseError.setLineNumber(lineNumber);
				parseError.setLinePos(linePos);
				return line.length();
			}

			linePos = fieldEnd + 1;
			if (linePos == line.length()) {
				break;
			} else if (line.charAt(linePos) == columnSeparator) {
				linePos++;
				break;
			}

			// must have a quote following a quote if there wasn't a columnSeparator
			if (line.charAt(linePos) != columnQuote) {
				parseError.setErrorType(ErrorType.INVALID_FORMAT);
				parseError.setMessage("quote '" + columnQuote + "' is not followed up separator '" + columnSeparator
						+ "'");
				parseError.setLineNumber(lineNumber);
				parseError.setLinePos(linePos);
				return linePos;
			}

			fieldEnd = linePos;
			// move past possibly end quote
			linePos++;
			if (linePos == line.length()) {
				break;
			}
			if (line.charAt(linePos) == columnSeparator) {
				// move past the comma
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
				extractAndAssignValue(line, lineNumber, columnInfo, fieldStart, fieldEnd, target, parseError);
			} else {
				headerSb.append(line, fieldStart, fieldEnd);
			}
		} else {
			sb.append(line, fieldStart, fieldEnd);
			String str = sb.toString();
			if (headerSb == null) {
				extractAndAssignValue(str, lineNumber, columnInfo, 0, str.length(), target, parseError);
			} else {
				headerSb.append(sb);
			}
		}
		return linePos;
	}

	private int processUnquotedLine(String line, int lineNumber, int linePos, ColumnInfo columnInfo, Object target,
			StringBuilder headerSb, ParseError parseError) {
		int fieldStart = linePos;
		linePos = line.indexOf(columnSeparator, fieldStart);
		if (linePos < 0) {
			linePos = line.length();
		}

		if (headerSb == null) {
			extractAndAssignValue(line, lineNumber, columnInfo, fieldStart, linePos, target, parseError);
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
		sb.append(columnQuote);
		int start = 0;
		while (true) {
			int linePos = str.indexOf(columnQuote, start);
			if (linePos < 0) {
				sb.append(str, start, str.length());
				break;
			}
			// move past the quote so we can output it
			linePos++;
			sb.append(str, start, linePos);
			// output another quote
			sb.append(columnQuote);
			start = linePos;
		}
		sb.append(columnQuote);
	}

	/**
	 * Extract a value from the line, convert it into its java equivalent, and assign it to our target object.
	 */
	private void extractAndAssignValue(String line, int lineNumber, ColumnInfo columnInfo, int fieldStart, int fieldEnd,
			Object target, ParseError parseError) {
		Object value = extractValue(line, lineNumber, columnInfo, fieldStart, fieldEnd, target, parseError);
		if (value == null) {
			// either error or no value
			return;
		}
		try {
			columnInfo.getField().set(target, value);
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
	private Object extractValue(String line, int lineNumber, ColumnInfo columnInfo, int fieldStart, int fieldEnd,
			Object target, ParseError parseError) {

		String columnStr = line.substring(fieldStart, fieldEnd);
		Converter<?, ?> converter = columnInfo.getConverter();
		if (alwaysTrimInput || columnInfo.isTrimInput() || converter.isAlwaysTrimInput()) {
			columnStr = columnStr.trim();
		}
		if (columnStr.isEmpty() && columnInfo.getDefaultValue() != null) {
			columnStr = columnInfo.getDefaultValue();
		}
		if (columnStr.isEmpty() && columnInfo.isRequired()) {
			parseError.setErrorType(ErrorType.NO_HEADER);
			parseError.setLineNumber(lineNumber);
			parseError.setLinePos(fieldStart);
			return null;
		}

		try {
			return converter.stringToJava(line, lineNumber, columnInfo, columnStr, parseError);
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
