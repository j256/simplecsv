package com.j256.simplecsv.converter;

import java.lang.reflect.Field;
import java.text.ParseException;

import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;
import com.j256.simplecsv.ParseError.ErrorType;

/**
 * Converts from a textual representation to a Java representation.
 * 
 * <p>
 * Converters must have a public no-arg constructor so they can be constructed by the library.
 * </p>
 * 
 * @author graywatson
 */
public interface Converter<T> {

	/**
	 * Configure this instance of the converter based on the associated params.
	 * 
	 * <p>
	 * NOTE: It is assumed that a converter associated with the same type, same format, and same flags can be reused
	 * across multiple CSV entities.
	 * </p>
	 * 
	 * @param format
	 *            Optional string format which affects the output and parsing of the field. Null if none supplied in
	 *            which case the default format is used.
	 * @param flags
	 *            Optional numerical flags which affect the output and parsing of the field. 0 if no flags supplied.
	 * @param field
	 *            Reflection field associated with this converter.
	 */
	public void configure(String format, long flags, Field field);

	/**
	 * Converts from a Java representation to string.
	 * 
	 * @param fieldInfo
	 *            Information about the field we are processing.
	 * @param value
	 *            Value of the field that we are converting.
	 * @param sb
	 *            String builder to which to append the string version of the value.
	 * @return The String equivalent object of the value parameter or null in which case "" will be printed.
	 */
	public void javaToString(FieldInfo fieldInfo, T value, StringBuilder sb);

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
	 * @return The Java equivalent object of the value parameter or null. Null can mean a null value or if the
	 *         parseError type is set to something other than {@link ErrorType#NONE}.
	 * @throws ParseException
	 *             If there was some sort of parse or other error. It is better to use the parseError argument instead.
	 *             All RuntimeExceptions will be caught as well.
	 */
	public T stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError)
			throws ParseException;
}
