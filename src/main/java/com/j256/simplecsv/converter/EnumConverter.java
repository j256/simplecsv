package com.j256.simplecsv.converter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;
import com.j256.simplecsv.ParseError.ErrorType;

/**
 * Converter for the Java Enum type associated with an Enum field.
 * 
 * @author graywatson
 */
public class EnumConverter implements Converter<Enum<?>> {

	private final Map<String, Enum<?>> enumStringMap = new HashMap<String, Enum<?>>();

	@Override
	public void configure(boolean allowNull, String format, long flags, Field field) {
		Enum<?>[] constants = (Enum<?>[]) field.getType().getEnumConstants();
		if (constants == null) {
			throw new IllegalArgumentException("Field " + field + " improperly configured as a enum");
		}
		for (Enum<?> enumVal : constants) {
			enumStringMap.put(enumVal.name(), enumVal);
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
		if (value == null) {
			return null;
		}
		Enum<?> enumValue = enumStringMap.get(value);
		if (enumValue == null) {
			parseError.setErrorType(ErrorType.INVALID_FORMAT);
			parseError.setMessage(value);
			return null;
		} else {
			return enumValue;
		}
	}
}
