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
public class CharacterConverter implements Converter<Character, Boolean> {

	/**
	 * Use this flag if you want a parse error generated when the input has more than one character. Default is to just
	 * take the first character.
	 */
	public static final long PARSE_ERROR_IF_MORE_THAN_ONE_CHAR = 1 << 1;

	private static final CharacterConverter singleton = new CharacterConverter();

	/**
	 * Get singleton for class.
	 */
	public static CharacterConverter getSingleton() {
		return singleton;
	}

	@Override
	public Boolean configure(String format, long flags, Field field) {
		boolean parseErrorOnMoreThanOne = ((flags & PARSE_ERROR_IF_MORE_THAN_ONE_CHAR) != 0);
		return parseErrorOnMoreThanOne;
	}

	@Override
	public boolean isNeedsQuotes(Boolean parseErrorOnMoreThanOne) {
		return true;
	}

	@Override
	public String javaToString(FieldInfo fieldInfo, Character value) {
		if (value == null) {
			return null;
		} else {
			return value.toString();
		}
	}

	@Override
	public Character stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError) {
		Boolean parseErrorOnMoreThanOne = (Boolean) fieldInfo.getConfigInfo();
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
