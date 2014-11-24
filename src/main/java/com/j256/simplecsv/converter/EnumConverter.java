package com.j256.simplecsv.converter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.j256.simplecsv.CsvField;
import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;
import com.j256.simplecsv.ParseError.ErrorType;

/**
 * Converter for the Java Enum type associated with an Enum field.
 * 
 * <p>
 * Use the {@link #FORMAT_IS_UNKNOWN_VALUE} flag to set the unknown enum field name set in the {@link CsvField#format()}
 * .
 * </p>
 * 
 * @author graywatson
 */
public class EnumConverter implements Converter<Enum<?>> {

	/**
	 * If this flag is set then the {@link CsvField#format()} string is actually the name of the enum constant that will
	 * be used if the value in the cell is unknown. So, for example, if the cell value is "red" but there is not an enum
	 * name that corresponds to "red" then the format value "blue" will be used instead.
	 */
	public static final long FORMAT_IS_UNKNOWN_VALUE = 1 << 1;

	private final Map<String, Enum<?>> enumStringMap = new HashMap<String, Enum<?>>();
	private Enum<?> unknownValue;

	@Override
	public void configure(String format, long flags, Field field) {

		Enum<?>[] constants = (Enum<?>[]) field.getType().getEnumConstants();
		if (constants == null) {
			throw new IllegalArgumentException("Field " + field + " improperly configured as a enum");
		}
		for (Enum<?> enumVal : constants) {
			enumStringMap.put(enumVal.name(), enumVal);
		}

		if ((flags & FORMAT_IS_UNKNOWN_VALUE) != 0) {
			unknownValue = enumStringMap.get(format);
			if (unknownValue == null) {
				throw new IllegalArgumentException("Format string '" + format + "' is not a valid enum value for "
						+ field.getType());
			}
		}
	}

	@Override
	public void javaToString(FieldInfo fieldInfo, Enum<?> value, StringBuilder sb) {
		if (value != null) {
			sb.append(value.toString());
		}
	}

	@Override
	public Enum<?> stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError) {
		if (value.isEmpty()) {
			return null;
		}
		Enum<?> enumValue = enumStringMap.get(value);
		if (enumValue != null) {
			return enumValue;
		} else if (unknownValue != null) {
			return unknownValue;
		} else {
			parseError.setErrorType(ErrorType.INVALID_FORMAT);
			parseError.setMessage(value);
			return null;
		}
	}
}
