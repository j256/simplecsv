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
import java.util.concurrent.Callable;

import com.j256.simplecsv.common.CsvField;
import com.j256.simplecsv.converter.Converter;
import com.j256.simplecsv.converter.ConverterUtils;
import com.j256.simplecsv.processor.ParseError.ErrorType;

/**
 * CSV reader and writer.
 * 
 * <p>
 * <b>NOTE:</b> You need to set the entity class either in the constructor or with {@link #setEntityClass(Class)} or
 * {@link #withEntityClass(Class)}. Then you need to do any {@link #registerConverter(Class, Converter)} or
 * {@link #withConverter(Class, Converter)} calls before the processor gets configured. You can then let the processor
 * be auto-configured on the first read/write method call or call {@link #initialize()} directly (or in spring).
 * </p>
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

	private static ColumnNameMatcher stringEqualsColumnNameMatcher = new ColumnNameMatcher() {
		@Override
		public boolean matchesColumnName(String definitionName, String csvName) {
			return definitionName.equals(csvName);
		}
	};

	private char columnSeparator = DEFAULT_COLUMN_SEPARATOR;
	private char columnQuote = DEFAULT_COLUMN_QUOTE;
	private String lineTermination = DEFAULT_LINE_TERMINATION;
	private boolean allowPartialLines;
	private boolean alwaysTrimInput;
	private boolean headerValidation = true;
	private boolean firstLineHeader = true;
	private boolean flexibleOrder;
	private boolean ignoreUnknownColumns;
	private ColumnNameMatcher columnNameMatcher = stringEqualsColumnNameMatcher;

	private Class<T> entityClass;
	private Constructor<T> constructor;
	private Callable<T> constructorCallable;

	private final Map<Class<?>, Converter<?, ?>> converterMap = new HashMap<Class<?>, Converter<?, ?>>();

	private List<ColumnInfo> allColumnInfos;
	private Map<Integer, ColumnInfo> columnPositionInfoMap;

	{
		ConverterUtils.addInternalConverters(converterMap);
	}

	public CsvProcessor() {
		// for spring
	}

	/**
	 * Constructs a processor with an entity class whose fields should be marked with {@link CsvField} annotations. The
	 * entity-class must also define a public no-arg contructor so the processor can instantiate them using reflection.
	 */
	public CsvProcessor(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	/**
	 * Register a converter class for all instances of the class argument. The converter can also be specified with the
	 * {@link CsvField#converterClass()} annotation field.
	 */
	public <FT> void registerConverter(Class<FT> clazz, Converter<FT, ?> converter) {
		converterMap.put(clazz, converter);
	}

	/**
	 * Register a converter class for all instances of the class argument. The converter can also be specified with the
	 * {@link CsvField#converterClass()} annotation field. Alternative way to do
	 * {@link #registerConverter(Class, Converter)}.
	 */
	public <FT> CsvProcessor<T> withConverter(Class<FT> clazz, Converter<FT, ?> converter) {
		converterMap.put(clazz, converter);
		return this;
	}

	/**
	 * This initializing the internal configuration information. It will self initialize if you start calling read/write
	 * methods but this is here if you are using the class concurrently and need to force the initialization.
	 */
	public CsvProcessor<T> initialize() {
		configureEntityClass();
		return this;
	}

	/**
	 * Read in all of the entities in the file passed in.
	 * 
	 * @param file
	 *            Where to read the header and entities from. It will be closed when the method returns.
	 * @param parseErrors
	 *            If not null, any errors will be added to the collection and null will be returned. If validateHeader
	 *            is true and the header does not match then no additional lines will be returned. If this is null then
	 *            a ParseException will be thrown on parsing problems.
	 * @return A list of entities read in or null if validateHeader is true and the first-line header was not valid.
	 * @throws ParseException
	 *             Thrown on any parsing problems. If parseErrors is not null then parse errors will be added there and
	 *             an exception should not be thrown.
	 * @throws IOException
	 *             If there are any IO exceptions thrown when reading.
	 */
	public List<T> readAll(File file, Collection<ParseError> parseErrors) throws IOException, ParseException {
		checkEntityConfig();
		return readAll(new FileReader(file), parseErrors);
	}

	/**
	 * Read in all of the entities in the reader passed in. It will use an internal buffered reader.
	 * 
	 * @param reader
	 *            Where to read the header and entities from. It will be closed when the method returns.
	 * @param parseErrors
	 *            If not null, any errors will be added to the collection and null will be returned. If validateHeader
	 *            is true and the header does not match then no additional lines will be returned. If this is null then
	 *            a ParseException will be thrown on parsing problems.
	 * @return A list of entities read in or null if parseErrors is not null.
	 * @throws ParseException
	 *             Thrown on any parsing problems. If parseErrors is not null then parse errors will be added there and
	 *             an exception should not be thrown.
	 * @throws IOException
	 *             If there are any IO exceptions thrown when reading.
	 */
	public List<T> readAll(Reader reader, Collection<ParseError> parseErrors) throws IOException, ParseException {
		checkEntityConfig();
		BufferedReader bufferedReader = new BufferedReader(reader);
		try {
			ParseError parseError = null;
			// we do this to reuse the parse error objects if we can
			if (parseErrors != null) {
				parseError = new ParseError();
			}
			if (firstLineHeader) {
				if (readHeader(bufferedReader, parseError) == null) {
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
					// once we use it, we need to create another one
					parseError = new ParseError();
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
	 * @return Array of header column names or null on error.
	 * @throws ParseException
	 *             Thrown on any parsing problems. If parseError is not null then the error will be added there and an
	 *             exception should not be thrown.
	 * @throws IOException
	 *             If there are any IO exceptions thrown when reading.
	 */
	public String[] readHeader(BufferedReader bufferedReader, ParseError parseError) throws ParseException, IOException {
		checkEntityConfig();
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
		} else if (headerValidation && !validateHeaderColumns(columns, parseError)) {
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
	 *             exception should not be thrown.
	 * @throws IOException
	 *             If there are any IO exceptions thrown when reading.
	 */
	public T readRow(BufferedReader bufferedReader, ParseError parseError) throws ParseException, IOException {
		checkEntityConfig();
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
	 *             exception should not be thrown.
	 */
	public boolean validateHeader(String line, ParseError parseError) throws ParseException {
		checkEntityConfig();
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
	 *             exception should not be thrown.
	 */
	public boolean validateHeaderColumns(String[] columns, ParseError parseError) {
		checkEntityConfig();
		boolean result = true;

		Map<String, ColumnInfo> columnNameToInfoMap = new HashMap<String, ColumnInfo>();
		for (ColumnInfo columnInfo : allColumnInfos) {
			columnNameToInfoMap.put(columnInfo.getColumnName(), columnInfo);
		}

		Map<Integer, ColumnInfo> columnPositionInfoMap = new HashMap<Integer, ColumnInfo>();
		int lastColumnInfoPosition = -1;
		for (int i = 0; i < columns.length; i++) {
			ColumnInfo matchedColumnInfo = null;
			if (columnNameMatcher == null) {
				matchedColumnInfo = columnNameToInfoMap.get(columns[i]);
			} else {
				// have to do a N^2 search
				for (ColumnInfo columnInfo : allColumnInfos) {
					if (columnNameMatcher.matchesColumnName(columnInfo.getColumnName(), columns[i])) {
						matchedColumnInfo = columnInfo;
						break;
					}
				}
			}

			if (matchedColumnInfo == null) {
				if (!ignoreUnknownColumns) {
					if (parseError != null) {
						parseError.setErrorType(ErrorType.INVALID_HEADER);
						parseError.setMessage("column name '" + columns[i] + "' is unknown");
					}
					result = false;
				}
			} else {
				if (!flexibleOrder && matchedColumnInfo.getPosition() <= lastColumnInfoPosition) {
					if (parseError != null) {
						parseError.setErrorType(ErrorType.INVALID_HEADER);
						parseError.setMessage("column name '" + columns[i] + "' is not in the proper order");
					}
					result = false;
				} else {
					lastColumnInfoPosition = matchedColumnInfo.getPosition();
				}
				// remove it from the map once we've matched with it
				columnNameToInfoMap.remove(matchedColumnInfo.getColumnName());
				columnPositionInfoMap.put(i, matchedColumnInfo);
			}
		}
		// did the column position information change
		if (!columnPositionInfoMap.equals(this.columnPositionInfoMap)) {
			this.columnPositionInfoMap = columnPositionInfoMap;
		}

		// now look for non-optional columns
		for (ColumnInfo columnInfo : columnNameToInfoMap.values()) {
			if (!columnInfo.isOptionalColumn()) {
				if (parseError != null) {
					parseError.setErrorType(ErrorType.INVALID_HEADER);
					parseError.setMessage("column '" + columnInfo.getColumnName()
							+ "' is not optional and must be suppled");
				}
				result = false;
			}
		}

		// if we have an error then reset the columnCount
		if (!result) {
			resetColumnPositionInfoMap();
		}
		return result;
	}

	/**
	 * Process a line and divide it up into a series of quoted columns.
	 * 
	 * @param line
	 *            Line to process looking for header.
	 * @param parseError
	 *            If not null, this will be set with the first parse error and it will return null. If this is null then
	 *            a ParseException will be thrown instead.
	 * @return Returns an array of processed header names entity or null if an error and parseError has been set. The
	 *         array will be the same length as the number of configured columns so some elements may be null.
	 * @throws ParseException
	 *             Thrown on any parsing problems. If parseError is not null then the error will be added there and an
	 *             exception should not be thrown.
	 */
	public String[] processHeader(String line, ParseError parseError) throws ParseException {
		checkEntityConfig();
		StringBuilder sb = new StringBuilder(32);
		int linePos = 0;
		ParseError localParseError = parseError;
		if (localParseError == null) {
			localParseError = new ParseError();
		}
		List<String> headerColumns = new ArrayList<String>();
		while (true) {
			boolean atEnd = (linePos == line.length());
			localParseError.reset();
			sb.setLength(0);
			if (linePos < line.length() && line.charAt(linePos) == columnQuote) {
				linePos = processQuotedColumn(line, 1, linePos, null, null, sb, localParseError);
			} else {
				linePos = processUnquotedColumn(line, 1, linePos, null, null, sb, localParseError);
			}
			if (localParseError.isError()) {
				if (localParseError == parseError) {
					// if we pass in an error then it gets set and we return null
					return null;
				} else {
					// if no error passed in then we throw
					throw new ParseException("Problems parsing header line at position " + linePos + " ("
							+ localParseError + "): " + line, linePos);
				}
			}
			if (sb.length() > 0) {
				headerColumns.add(sb.toString());
			}
			if (atEnd) {
				break;
			}
		}
		return headerColumns.toArray(new String[headerColumns.size()]);
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
	 *             exception should not be thrown.
	 */
	public T processRow(String line, ParseError parseError) throws ParseException {
		checkEntityConfig();
		T target = constructEntity();
		int linePos = 0;
		ParseError localParseError = parseError;
		if (localParseError == null) {
			localParseError = new ParseError();
		}
		int columnCount = 0;
		while (true) {
			ColumnInfo columnInfo = columnPositionInfoMap.get(columnCount);
			// we have to do this because a blank column may be ok
			boolean atEnd = (linePos == line.length());
			localParseError.reset();
			if (linePos < line.length() && line.charAt(linePos) == columnQuote) {
				linePos = processQuotedColumn(line, 1, linePos, columnInfo, target, null, localParseError);
			} else {
				linePos = processUnquotedColumn(line, 1, linePos, columnInfo, target, null, localParseError);
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
			columnCount++;
			if (atEnd) {
				break;
			}
			// NOTE: we can't break here if we are at the end of line because might be blank column
		}
		if (columnCount < columnPositionInfoMap.size() && !allowPartialLines) {
			throw new ParseException("Line does not have " + columnPositionInfoMap.size() + " columns: " + line,
					linePos);
		}
		if (linePos < line.length() && !ignoreUnknownColumns) {
			throw new ParseException(
					"Line has extra information past last column at position " + linePos + ": " + line, linePos);
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
		checkEntityConfig();
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
		checkEntityConfig();
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
		checkEntityConfig();
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
		checkEntityConfig();
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (ColumnInfo columnInfo : allColumnInfos) {
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
		checkEntityConfig();
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (ColumnInfo columnInfo : allColumnInfos) {
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
	 * Class that we are processing.
	 */
	public void setEntityClass(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	/**
	 * Class that we are processing. Alternative way to do {@link #setEntityClass(Class)}.
	 */
	public CsvProcessor<T> withEntityClass(Class<T> entityClass) {
		this.entityClass = entityClass;
		return this;
	}

	/**
	 * Set the a method that will construct the entity we are loading. This is used in case there are is not a no-arg
	 * constructor for the entity.
	 */
	public void setConstructorCallable(Callable<T> constructorCallable) {
		this.constructorCallable = constructorCallable;
	}

	/**
	 * Set the a method that will construct the entity we are loading. This is used in case there are is not a no-arg
	 * constructor for the entity. Alternative way to do {@link #setConstructorCallable(Callable)}.
	 */
	public CsvProcessor<T> withConstructorCallable(Callable<T> constructorCallable) {
		this.constructorCallable = constructorCallable;
		return this;
	}

	/**
	 * String that separates columns in out CSV input and output.
	 */
	public void setColumnSeparator(char columnSeparator) {
		this.columnSeparator = columnSeparator;
	}

	/**
	 * String that separates columns in out CSV input and output. Alternative way to do
	 * {@link #setColumnSeparator(char)}.
	 */
	public CsvProcessor<T> withColumnSeparator(char columnSeparator) {
		this.columnSeparator = columnSeparator;
		return this;
	}

	/**
	 * Quote character that is used to wrap each column.
	 */
	public void setColumnQuote(char columnQuote) {
		this.columnQuote = columnQuote;
	}

	/**
	 * Quote character that is used to wrap each column. Alternative way to do {@link #setColumnQuote(char)}.
	 */
	public CsvProcessor<T> withColumnQuote(char columnQuote) {
		this.columnQuote = columnQuote;
		return this;
	}

	/**
	 * Sets the character which is written at the end of the row. Default is to use
	 * System.getProperty("line.separator");.
	 */
	public void setLineTermination(String lineTermination) {
		this.lineTermination = lineTermination;
	}

	/**
	 * Sets the character which is written at the end of the row. Default is to use
	 * System.getProperty("line.separator");. Alternative way to do {@link #setLineTermination(String)}.
	 */
	public CsvProcessor<T> withLineTermination(String lineTermination) {
		this.lineTermination = lineTermination;
		return this;
	}

	/**
	 * Set to true to allow lines that do not have values for all of the columns. Otherwise an IllegalArgumentException
	 * is thrown.
	 */
	public void setAllowPartialLines(boolean allowPartialLines) {
		this.allowPartialLines = allowPartialLines;
	}

	/**
	 * Set to true to allow lines that do not have values for all of the columns. Otherwise an IllegalArgumentException
	 * is thrown. Alternative way to do {@link #setAllowPartialLines(boolean)}.
	 */
	public CsvProcessor<T> withAllowPartialLines(boolean allowPartialLines) {
		this.allowPartialLines = allowPartialLines;
		return this;
	}

	/**
	 * Set to true to always call {@link String#trim()} on data input columns to remove any spaces from the start or
	 * end.
	 */
	public void setAlwaysTrimInput(boolean alwaysTrimInput) {
		this.alwaysTrimInput = alwaysTrimInput;
	}

	/**
	 * Set to true to always call {@link String#trim()} on data input columns to remove any spaces from the start or
	 * end. Alternative way to do {@link #setAlwaysTrimInput(boolean)}.
	 */
	public CsvProcessor<T> withAlwaysTrimInput(boolean alwaysTrimInput) {
		this.alwaysTrimInput = alwaysTrimInput;
		return this;
	}

	/**
	 * Set to false to not validate the header when it is read in. Default is true.
	 */
	public CsvProcessor<T> withHeaderValidation(boolean headerValidation) {
		this.headerValidation = headerValidation;
		return this;
	}

	/**
	 * Set to false if the first line is a header line to be processed. Default is true.
	 */
	public CsvProcessor<T> withFirstLineHeader(boolean firstLineHeader) {
		this.firstLineHeader = firstLineHeader;
		return this;
	}

	/**
	 * Set the column name matcher class which will be used to see if the column from the CSV file matches the
	 * definition name. This can be used if you have optional suffix characters such as "*" or something. Default is
	 * {@link String#equals(Object)}.
	 */
	public CsvProcessor<T> withColumnNameMatcher(ColumnNameMatcher columnNameMatcher) {
		this.columnNameMatcher = columnNameMatcher;
		return this;
	}

	/**
	 * Set to true if the order of the input columns is flexible and does not have to match the order of the definition
	 * fields in the entity. The order is determined by the header columns so their must be a header. Default is false.
	 * 
	 * <b>WARNING:</b> If you are using flexible ordering, this CsvProcessor cannot be used with multiple files at the
	 * same time since the column orders are dynamic.
	 */
	public CsvProcessor<T> withFlexibleOrder(boolean flexibleOrder) {
		this.flexibleOrder = flexibleOrder;
		return this;
	}

	/**
	 * Set to true to ignore columns that are not know to the configuration. Default is to raise an error.
	 * 
	 * <b>WARNING:</b> If you are using unknown columns, this CsvProcessor cannot be used with multiple files at the
	 * same time since the column position is dynamic.
	 */
	public CsvProcessor<T> withIgnoreUnknownColumns(boolean ignoreUnknownColumns) {
		this.ignoreUnknownColumns = ignoreUnknownColumns;
		return this;
	}

	private T constructEntity() throws ParseException {
		try {
			if (constructorCallable == null) {
				return constructor.newInstance();
			} else {
				return constructorCallable.call();
			}
		} catch (Exception e) {
			ParseException parseException = new ParseException("Could not construct instance of " + entityClass, 0);
			parseException.initCause(e);
			throw parseException;
		}
	}

	private void checkEntityConfig() {
		if (allColumnInfos == null) {
			configureEntityClass();
		}
	}

	private void configureEntityClass() {
		if (entityClass == null) {
			throw new IllegalStateException("Entity class not configured for CSV processor");
		}
		List<ColumnInfo> columnInfos = new ArrayList<ColumnInfo>();
		Set<String> knownFields = new HashSet<String>();
		int fieldCount = 0;
		for (Class<?> clazz = entityClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
			for (Field field : clazz.getDeclaredFields()) {
				if (!knownFields.add(field.getName())) {
					continue;
				}
				Converter<?, ?> converter = converterMap.get(field.getType());
				// NOTE: converter could be null in which case the CsvField.converterClass must be set
				@SuppressWarnings("unchecked")
				Converter<Object, Object> castConverter = (Converter<Object, Object>) converter;
				ColumnInfo columnInfo = ColumnInfo.fromField(field, castConverter, fieldCount++);
				if (columnInfo != null) {
					columnInfos.add(columnInfo);
					field.setAccessible(true);
				}
			}
		}
		if (columnInfos.isEmpty()) {
			throw new IllegalArgumentException("Could not find any exposed CSV fields in: " + entityClass);
		}
		this.allColumnInfos = columnInfos;
		resetColumnPositionInfoMap();
		if (constructorCallable == null) {
			try {
				this.constructor = entityClass.getConstructor();
			} catch (Exception e) {
				throw new IllegalStateException(
						"No callable configured or could not find public no-arg constructor for: " + entityClass);
			}
		}
	}

	private void resetColumnPositionInfoMap() {
		Map<Integer, ColumnInfo> columnPositionInfoMap = new HashMap<Integer, ColumnInfo>();
		int columnCount = 0;
		for (ColumnInfo columnInfo : allColumnInfos) {
			columnPositionInfoMap.put(columnCount, columnInfo);
			columnCount++;
		}
		this.columnPositionInfoMap = columnPositionInfoMap;
	}

	private int processQuotedColumn(String line, int lineNumber, int linePos, ColumnInfo columnInfo, Object target,
			StringBuilder headerSb, ParseError parseError) {

		// linePos is pointing at the first quote, move past it
		linePos++;
		int columnStart = linePos;
		int sectionStart = linePos;
		int sectionEnd = linePos;

		StringBuilder sb = null;
		while (linePos < line.length()) {

			// look for the next quote
			sectionEnd = line.indexOf(columnQuote, linePos);
			if (sectionEnd < 0) {
				parseError.setErrorType(ErrorType.TRUNCATED_VALUE);
				parseError.setMessage("Column not terminated with quote '" + columnQuote + "'");
				parseError.setLineNumber(lineNumber);
				parseError.setLinePos(linePos);
				return line.length();
			}

			linePos = sectionEnd + 1;
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

			sectionEnd = linePos;
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
			// add to the string-builder the column + 1 quote
			sb.append(line, sectionStart, sectionEnd);
			// line-pos is pointing past 2nd (maybe 3rd) quote
			sectionStart = linePos;
		}

		if (sb == null) {
			if (headerSb == null) {
				String columnStr = line.substring(sectionStart, sectionEnd);
				if (columnInfo != null) {
					extractAndAssignValue(line, lineNumber, columnInfo, columnStr, columnStart, target, parseError);
				}
			} else {
				headerSb.append(line, sectionStart, sectionEnd);
			}
		} else {
			sb.append(line, sectionStart, sectionEnd);
			String str = sb.toString();
			if (headerSb == null) {
				if (columnInfo != null) {
					extractAndAssignValue(str, lineNumber, columnInfo, str, columnStart, target, parseError);
				}
			} else {
				headerSb.append(str);
			}
		}
		return linePos;
	}

	private int processUnquotedColumn(String line, int lineNumber, int linePos, ColumnInfo columnInfo, Object target,
			StringBuilder headerSb, ParseError parseError) {
		int columnStart = linePos;
		linePos = line.indexOf(columnSeparator, columnStart);
		if (linePos < 0) {
			linePos = line.length();
		}

		if (headerSb == null) {
			String columnStr = line.substring(columnStart, linePos);
			if (columnInfo != null) {
				extractAndAssignValue(line, lineNumber, columnInfo, columnStr, columnStart, target, parseError);
			}
		} else {
			headerSb.append(line, columnStart, linePos);
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
	private void extractAndAssignValue(String line, int lineNumber, ColumnInfo columnInfo, String columnStr,
			int linePos, Object target, ParseError parseError) {
		Object value = extractValue(line, lineNumber, columnInfo, columnStr, linePos, target, parseError);
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
			parseError.setLinePos(linePos);
		}
	}

	/**
	 * Extract a value from the line and convert it into its java equivalent.
	 */
	private Object extractValue(String line, int lineNumber, ColumnInfo columnInfo, String columnStr, int linePos,
			Object target, ParseError parseError) {

		Converter<?, ?> converter = columnInfo.getConverter();
		if (alwaysTrimInput || columnInfo.isTrimInput() || converter.isAlwaysTrimInput()) {
			columnStr = columnStr.trim();
		}
		if (columnStr.isEmpty() && columnInfo.getDefaultValue() != null) {
			columnStr = columnInfo.getDefaultValue();
		}
		if (columnStr.isEmpty() && columnInfo.isRequired()) {
			parseError.setErrorType(ErrorType.REQUIRED);
			parseError.setLineNumber(lineNumber);
			parseError.setLinePos(linePos);
			return null;
		}

		try {
			return converter.stringToJava(line, lineNumber, columnInfo, columnStr, parseError);
		} catch (ParseException e) {
			parseError.setErrorType(ErrorType.INVALID_FORMAT);
			parseError.setMessage(e.getMessage());
			parseError.setLineNumber(lineNumber);
			parseError.setLinePos(linePos);
			return null;
		} catch (Exception e) {
			parseError.setErrorType(ErrorType.INTERNAL_ERROR);
			parseError.setMessage(e.getMessage());
			parseError.setLineNumber(lineNumber);
			parseError.setLinePos(linePos);
			return null;
		}
	}
}
