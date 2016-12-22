package com.j256.simplecsv.converter;

import java.util.HashMap;
import java.util.Map;

import com.j256.simplecsv.common.CsvField;
import com.j256.simplecsv.processor.ColumnInfo;
import com.j256.simplecsv.processor.FieldInfo;
import com.j256.simplecsv.processor.ParseError;
import com.j256.simplecsv.processor.ParseError.ErrorType;

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
public class EnumConverter implements Converter<Enum<?>, EnumConverter.ConfigInfo> {

	/**
	 * If this flag is set then the {@link CsvField#format()} string is actually the name of the enum constant that will
	 * be used if the value in the column is unknown. So, for example, if the column value is "red" but there is not an
	 * enum name that corresponds to "red" then the format value "blue" will be used instead.
	 */
	public static final long FORMAT_IS_UNKNOWN_VALUE = 1 << 1;

	private static final EnumConverter singleton = new EnumConverter();

	/**
	 * Get singleton for class.
	 */
	public static EnumConverter getSingleton() {
		return singleton;
	}

	@Override
	public ConfigInfo configure(String format, long flags, FieldInfo<Enum<?>> fieldInfo) {

		Map<String, Enum<?>> enumStringMap = new HashMap<String, Enum<?>>();
		Enum<?>[] constants = (Enum<?>[]) fieldInfo.getType().getEnumConstants();
		if (constants == null) {
			throw new IllegalArgumentException("Field " + fieldInfo + " improperly configured as a enum");
		}
		for (Enum<?> enumVal : constants) {
			enumStringMap.put(enumVal.name(), enumVal);
		}

		Enum<?> unknownValue = null;
		if ((flags & FORMAT_IS_UNKNOWN_VALUE) != 0) {
			unknownValue = enumStringMap.get(format);
			if (unknownValue == null) {
				throw new IllegalArgumentException(
						"Format string '" + format + "' is not a valid enum value for " + fieldInfo.getType());
			}
		}

		return new ConfigInfo(enumStringMap, unknownValue);
	}

	@Override
	public boolean isNeedsQuotes(ConfigInfo configInfo) {
		return true;
	}

	@Override
	public boolean isAlwaysTrimInput() {
		return true;
	}

	@Override
	public String javaToString(ColumnInfo<Enum<?>> columnInfo, Enum<?> value) {
		if (value == null) {
			return null;
		} else {
			return value.name();
		}
	}

	@Override
	public Enum<?> stringToJava(String line, int lineNumber, int linePos, ColumnInfo<Enum<?>> columnInfo, String value,
			ParseError parseError) {
		if (value.isEmpty()) {
			return null;
		}
		ConfigInfo configInfo = (ConfigInfo) columnInfo.getConfigInfo();
		Enum<?> enumValue = configInfo.enumStringMap.get(value);
		if (enumValue != null) {
			return enumValue;
		} else if (configInfo.unknownValue != null) {
			return configInfo.unknownValue;
		} else {
			parseError.setErrorType(ErrorType.INVALID_FORMAT);
			parseError.setMessage(value);
			parseError.setLinePos(linePos);
			return null;
		}
	}

	static class ConfigInfo {
		final Map<String, Enum<?>> enumStringMap;
		final Enum<?> unknownValue;

		private ConfigInfo(Map<String, Enum<?>> enumStringMap, Enum<?> unknownValue) {
			this.enumStringMap = enumStringMap;
			this.unknownValue = unknownValue;
		}
	}
}
