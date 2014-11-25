package com.j256.simplecsv.processor;

import java.lang.reflect.Field;

import com.j256.simplecsv.common.CsvField;
import com.j256.simplecsv.converter.Converter;
import com.j256.simplecsv.converter.ConverterUtils;
import com.j256.simplecsv.converter.VoidConverter;

/**
 * Information about a particular field used internally to keep track of the CSV fields.
 * 
 * @author graywatson
 */
public class FieldInfo {

	private final Field field;
	private final Converter<?, ?> converter;
	private final Object configInfo;
	private final String columnName;
	private final boolean required;
	private final boolean trimInput;
	private final boolean needsQuotes;
	private final String defaultValue;

	private FieldInfo(Field field, Converter<?, ?> converter, Object configInfo, String columnName, boolean required,
			boolean trimInput, boolean needsQuotes, String defaultValue) {
		this.field = field;
		this.converter = converter;
		this.configInfo = configInfo;
		this.columnName = columnName;
		this.required = required;
		this.trimInput = trimInput;
		this.needsQuotes = needsQuotes;
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
	 * @see CsvField#columnName()
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @see CsvField#required()
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @see CsvField#trimInput()
	 */
	public boolean isTrimInput() {
		return trimInput;
	}

	/**
	 * Returns whether this field should be surrounded by quotes or not.
	 */
	public boolean isNeedsQuotes() {
		return needsQuotes;
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
		if (csvField.converterClass() == VoidConverter.class) {
			if (converter == null) {
				throw new IllegalArgumentException("No converter available for type: " + field.getType());
			} else {
				// use the passed in one
			}
		} else {
			@SuppressWarnings("unchecked")
			Converter<Object, Object> castConverter =
					(Converter<Object, Object>) ConverterUtils.constructConverter(csvField.converterClass());
			converter = castConverter;
		}
		String format;
		if (csvField.format().equals(CsvField.DEFAULT_VALUE)) {
			format = null;
		} else {
			format = csvField.format();
		}
		Object configInfo = converter.configure(format, csvField.converterFlags(), field);
		@SuppressWarnings("unchecked")
		Converter<Object, Object> castConverter = (Converter<Object, Object>) converter;
		boolean needsQuotes = castConverter.isNeedsQuotes(configInfo);

		String columnName;
		if (csvField.columnName().equals(CsvField.DEFAULT_VALUE)) {
			columnName = field.getName();
		} else {
			columnName = csvField.columnName();
		}
		String defaultValue = null;
		if (!csvField.defaultValue().equals(CsvField.DEFAULT_VALUE)) {
			defaultValue = csvField.defaultValue();
		}
		return new FieldInfo(field, converter, configInfo, columnName, csvField.required(), csvField.trimInput(),
				needsQuotes, defaultValue);
	}

	/**
	 * For testing purposes.
	 */
	public static FieldInfo forTests(Converter<?, ?> converter, Object configInfo) {
		return new FieldInfo(null, converter, configInfo, "name", false, false, false, null);
	}
}
