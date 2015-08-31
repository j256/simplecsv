package com.j256.simplecsv.processor;

import java.lang.reflect.Field;

import com.j256.simplecsv.common.CsvField;
import com.j256.simplecsv.converter.Converter;
import com.j256.simplecsv.converter.ConverterUtils;
import com.j256.simplecsv.converter.VoidConverter;

/**
 * Information about a particular column used internally to keep track of the CSV columns.
 * 
 * @author graywatson
 */
public class ColumnInfo {

	private final Field field;
	private final Converter<?, ?> converter;
	private final Object configInfo;
	private final String columnName;
	private final int position;
	private final boolean required;
	private final boolean trimInput;
	private final boolean needsQuotes;
	private final String defaultValue;
	private final boolean optionalColumn;

	private ColumnInfo(Field field, Converter<?, ?> converter, Object configInfo, String columnName, int position,
			boolean required, boolean trimInput, boolean needsQuotes, String defaultValue, boolean optionalColumn) {
		this.field = field;
		this.converter = converter;
		this.configInfo = configInfo;
		this.columnName = columnName;
		this.position = position;
		this.required = required;
		this.trimInput = trimInput;
		this.needsQuotes = needsQuotes;
		this.defaultValue = defaultValue;
		this.optionalColumn = optionalColumn;
	}

	/**
	 * Returns the Java reflection Field associated with the column.
	 */
	public Field getField() {
		return field;
	}

	/**
	 * Returns the converter class associated with the column.
	 */
	public Converter<?, ?> getConverter() {
		return converter;
	}

	/**
	 * Returns the configuration information associated with the column, if any.
	 */
	public Object getConfigInfo() {
		return configInfo;
	}

	/**
	 * Returns whether the header name for this column.
	 * 
	 * @see CsvField#columnName()
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * Position the column appears in the file.
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Returns whether this column is required.
	 * 
	 * @see CsvField#required()
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * Returns whether this column should be trimmed when read.
	 * 
	 * @see CsvField#trimInput()
	 */
	public boolean isTrimInput() {
		return trimInput;
	}

	/**
	 * Returns whether this column should be surrounded by quotes or not.
	 */
	public boolean isNeedsQuotes() {
		return needsQuotes;
	}

	/**
	 * Returns the default string for the column or null if none.
	 * 
	 * @see CsvField#defaultValue()
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Returns whether the column is optional or not.
	 * 
	 * @see CsvField#optionalColumn()
	 */
	public boolean isOptionalColumn() {
		return optionalColumn;
	}

	/**
	 * Make a column-info instance from a Java Field.
	 */
	public static ColumnInfo fromField(Field field, Converter<?, ?> converter, int columnCount) {
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
		return new ColumnInfo(field, converter, configInfo, columnName, columnCount, csvField.required(),
				csvField.trimInput(), needsQuotes, defaultValue, csvField.optionalColumn());
	}

	/**
	 * For testing purposes.
	 */
	public static ColumnInfo forTests(Converter<?, ?> converter, Object configInfo) {
		return new ColumnInfo(null, converter, configInfo, "name", 0, false, false, false, null, false);
	}
}
