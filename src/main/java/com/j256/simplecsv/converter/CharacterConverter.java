package com.j256.simplecsv.converter;

import java.lang.reflect.Field;

import com.j256.simplecsv.CsvField;
import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;
import com.j256.simplecsv.ParseError.ErrorType;

/**
 * Converter for the Java String type.
 * 
 * <p>
 * The {@link CsvField#converterFlags()} parameter can be set to {@link #PARSE_ERROR_IF_MORE_THAN_ONE_CHAR} to throw a
 * parse error if the input has more than one character.
 * </p>
 * 
 * @author graywatson
 */
public class CharacterConverter implements Converter<Character> {

	/**
	 * Use this flag if you want a parse error generated when the input has more than one character. Default is to just
	 * take the first character.
	 */
	public static final long PARSE_ERROR_IF_MORE_THAN_ONE_CHAR = 1 << 1;

	private boolean parseErrorOnMoreThanOne;

	@Override
	public void configure(String format, long flags, Field field) {
		this.parseErrorOnMoreThanOne = ((flags & PARSE_ERROR_IF_MORE_THAN_ONE_CHAR) != 0);
	}

	@Override
	public void javaToString(FieldInfo fieldInfo, Character value, StringBuilder sb) {
		if (value != null) {
			sb.append(value);
		}
	}

	@Override
	public Character stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError) {
		if (value.isEmpty()) {
			return null;
		} else if (value.length() > 1 && parseErrorOnMoreThanOne) {
			parseError.setErrorType(ErrorType.INVALID_FORMAT);
			parseError.setMessage("More than one character specified");
			return null;
		} else {
			return value.charAt(0);
		}
	}
}
