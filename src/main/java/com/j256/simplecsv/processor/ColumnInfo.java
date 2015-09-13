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
	private final boolean mustNotBeBlank;
	private final boolean trimInput;
	private final boolean needsQuotes;
	private final String defaultValue;
	private final boolean mustBeSupplied;

	private ColumnInfo(Field field, Converter<?, ?> converter, Object configInfo, String columnName, int position,
			boolean mustNotBeBlank, boolean trimInput, boolean needsQuotes, String defaultValue, boolean mustBeSupplied) {
		this.field = field;
		this.converter = converter;
		this.configInfo = configInfo;
		this.columnName = columnName;
		this.position = position;
		this.mustNotBeBlank = mustNotBeBlank;
		this.trimInput = trimInput;
		this.needsQuotes = needsQuotes;
		this.defaultValue = defaultValue;
		this.mustBeSupplied = mustBeSupplied;
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
	 * @see CsvField#mustNotBeBlank()
	 */
	public boolean isMustNotBeBlank() {
		return mustNotBeBlank;
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
	 * @see CsvField#mustBeSupplied()
	 */
	public boolean isMustBeSupplied() {
		return mustBeSupplied;
	}

	/**
	 * Make a column-info instance from a Java Field.
	 */
	public static ColumnInfo fromField(Field field, Converter<?, ?> converter, int position) {
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
		return new ColumnInfo(field, converter, configInfo, columnName, position, fieldMustNotBeBlank(csvField),
				csvField.trimInput(), needsQuotes, defaultValue, fieldMustBeSupplied(csvField));
	}

	/**
	 * For testing purposes.
	 */
	public static ColumnInfo forTests(Converter<?, ?> converter, Object configInfo) {
		return new ColumnInfo(null, converter, configInfo, "name", 0, false, false, false, null, false);
	}

	/**
	 * To isolate the suppress warnings.
	 */
	@SuppressWarnings("deprecation")
	private static boolean fieldMustNotBeBlank(CsvField csvField) {
		return (csvField.required() || csvField.mustNotBeBlank());
	}

	/**
	 * To isolate the suppress warnings.
	 */
	@SuppressWarnings("deprecation")
	private static boolean fieldMustBeSupplied(CsvField csvField) {
		// we are explicit here because if the use must-be-suppled we have to take that value
		if (!csvField.mustBeSupplied() || csvField.optionalColumn()) {
			return false;
		} else {
			return true;
		}
	}
}
