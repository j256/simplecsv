package com.j256.simplecsv;

import java.lang.reflect.Field;

import com.j256.simplecsv.converter.Converter;

/**
 * Information about a particular field.
 * 
 * @author graywatson
 */
public class FieldInfo {

	private final Field field;
	private final Converter<?, ?> converter;
	private final Object configInfo;
	private final String cellName;
	private final boolean required;
	private final boolean allowNull;
	private final boolean trimInput;
	private final String defaultValue;

	private FieldInfo(Field field, Converter<?, ?> converter, Object configInfo, String cellName, boolean required,
			boolean allowNull, boolean trimInput, String defaultValue) {
		this.field = field;
		this.converter = converter;
		this.configInfo = configInfo;
		this.cellName = cellName;
		this.required = required;
		this.allowNull = allowNull;
		this.trimInput = trimInput;
		this.defaultValue = defaultValue;
	}

	public Field getField() {
		return field;
	}

	public Converter<?, ?> getConverter() {
		return converter;
	}

	public Object getConfigInfo() {
		return configInfo;
	}

	/**
	 * @see CsvField#cellName()
	 */
	public String getCellName() {
		return cellName;
	}

	/**
	 * @see CsvField#required()
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @see CsvField#allowNull()
	 */
	public boolean isAllowNull() {
		return allowNull;
	}

	/**
	 * @see CsvField#trimInput()
	 */
	public boolean isTrimInput() {
		return trimInput;
	}

	/**
	 * @see CsvField#defaultValue()
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Make a field-info instance from a Java Field.
	 */
	public static FieldInfo fromField(Field field, Converter<?, ?> converter) {
		CsvField csvField = field.getAnnotation(CsvField.class);
		if (csvField == null) {
			return null;
		}
		if (converter == null) {
			throw new IllegalArgumentException("No converter available for type: " + field.getType());
		}
		Object configInfo = converter.configure(csvField.format(), csvField.converterFlags(), field);

		String cellName;
		if (csvField.cellName().equals(CsvField.DEFAULT_VALUE)) {
			cellName = field.getName();
		} else {
			cellName = csvField.cellName();
		}
		String defaultValue = null;
		if (!csvField.defaultValue().equals(CsvField.DEFAULT_VALUE)) {
			defaultValue = csvField.defaultValue();
		}
		return new FieldInfo(field, converter, configInfo, cellName, csvField.required(), csvField.allowNull(),
				csvField.trimInput(), defaultValue);
	}

	/**
	 * For testing purposes.
	 */
	public static FieldInfo forTests(Converter<?, ?> converter, Object configInfo) {
		return new FieldInfo(null, converter, configInfo, "name", false, true, false, null);
	}
}
