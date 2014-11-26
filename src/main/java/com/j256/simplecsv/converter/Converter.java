package com.j256.simplecsv.converter;

import java.lang.reflect.Field;
import java.text.ParseException;

import com.j256.simplecsv.processor.FieldInfo;
import com.j256.simplecsv.processor.ParseError;
import com.j256.simplecsv.processor.ParseError.ErrorType;

/**
 * Converts from a textual representation to a Java representation.
 * 
 * <p>
 * Converters must have a public no-arg constructor so they can be constructed by the library. Also, they will be reused
 * so all local state should be saved in the config-info object that is returned by the
 * {@link #configure(String, long, Field)} method and passed to the other methods.
 * </p>
 * 
 * @param <T>
 *            The Java type that we are converting from/to.
 * @param <C>
 *            The configuration information object (or null if none) that we use to share state so we can use this
 *            converter with multiple entities. If your converter has no config-info then you can use Void here and
 *            return null from {@link #configure(String, long, Field)}.
 * 
 * @author graywatson
 */
public interface Converter<T, C> {

	/**
	 * Configure this instance of the converter based on the associated params.
	 * 
	 * @param format
	 *            Optional string format which affects the output and parsing of the field. Null if none supplied in
	 *            which case the default format is used.
	 * @param flags
	 *            Optional numerical flags which affect the output and parsing of the field. 0 if no flags supplied.
	 * @param field
	 *            Reflection field associated with this converter.
	 * @return Information structure or null if none. This will be passed to the other methods.
	 */
	public C configure(String format, long flags, Field field);

	/**
	 * Returns true if the field needs to be quoted in the CSV output.
	 */
	public boolean isNeedsQuotes(C configInfo);

	/**
	 * Returns true if the field should always trim before it is processed.
	 */
	public boolean isAlwaysTrimInput();

	/**
	 * Converts from a Java representation to string.
	 * 
	 * @param fieldInfo
	 *            Information about the field we are processing.
	 * @param value
	 *            Value of the field that we are converting.
	 * 
	 * @return The String equivalent object of the value parameter or null in which case nothing will be printed.
	 */
	public String javaToString(FieldInfo fieldInfo, T value);

	/**
	 * Converts from a string representation to Java.
	 * 
	 * @param line
	 *            Line we are processing for logging purposes.
	 * @param lineNumber
	 *            Number of the line we are processing for logging purposes.
	 * @param fieldInfo
	 *            Information about the field we are processing.
	 * @param value
	 *            Value of the field that we are converting.
	 * @param parseError
	 *            Parse error which can we use to set information about parse errors here. If there are no parse errors
	 *            then just ignore this field. Any exceptions thrown will also be caught and interpreted as errors.
	 * 
	 * @return The Java equivalent object of the value parameter or null. Null can mean a null value or if the
	 *         parseError type is set to something other than {@link ErrorType#NONE}.
	 * @throws ParseException
	 *             If there was some sort of parse or other error. It is better to use the parseError argument instead.
	 *             All RuntimeExceptions will be caught as well.
	 */
	public T stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError)
			throws ParseException;
}
