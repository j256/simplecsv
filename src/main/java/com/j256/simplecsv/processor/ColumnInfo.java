package com.j256.simplecsv.processor;

import com.j256.simplecsv.common.CsvColumn;
import com.j256.simplecsv.converter.Converter;
import com.j256.simplecsv.converter.ConverterUtils;
import com.j256.simplecsv.converter.VoidConverter;

/**
 * Information about a particular column used internally to keep track of the CSV columns.
 * 
 * @author graywatson
 */
public class ColumnInfo<T> {

	private final FieldInfo<T> fieldInfo;
	private final Converter<T, ?> converter;
	private final Object configInfo;
	private final String columnName;
	private int position;
	private final boolean mustNotBeBlank;
	private final boolean trimInput;
	private final boolean needsQuotes;
	private final String defaultValue;
	private final boolean mustBeSupplied;

	private ColumnInfo(FieldInfo<T> fieldInfo, Converter<T, ?> converter, Object configInfo, String columnName,
			boolean mustNotBeBlank, boolean trimInput, boolean needsQuotes, String defaultValue,
			boolean mustBeSupplied) {
		this.fieldInfo = fieldInfo;
		this.converter = converter;
		this.configInfo = configInfo;
		this.columnName = columnName;
		this.mustNotBeBlank = mustNotBeBlank;
		this.trimInput = trimInput;
		this.needsQuotes = needsQuotes;
		this.defaultValue = defaultValue;
		this.mustBeSupplied = mustBeSupplied;
	}

	/**
	 * Returns the Java reflection Field associated with the column.
	 */
	public FieldInfo<T> getFieldInfo() {
		return fieldInfo;
	}

	/**
	 * Returns the converter class associated with the column.
	 */
	public Converter<T, ?> getConverter() {
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
	 * @see CsvColumn#columnName()
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
	 * Position the column appears in the file.
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * Returns whether this column is required.
	 * 
	 * @see CsvColumn#mustNotBeBlank()
	 */
	public boolean isMustNotBeBlank() {
		return mustNotBeBlank;
	}

	/**
	 * Returns whether this column should be trimmed when read.
	 * 
	 * @see CsvColumn#trimInput()
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
	 * @see CsvColumn#defaultValue()
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Returns whether the column is optional or not.
	 * 
	 * @see CsvColumn#mustBeSupplied()
	 */
	public boolean isMustBeSupplied() {
		return mustBeSupplied;
	}

	/**
	 * Make a column-info instance from a Java Field.
	 */
	public static <T> ColumnInfo<T> fromFieldInfo(CsvColumn csvField, FieldInfo<T> fieldInfo,
			Converter<T, ?> converter) {
		if (csvField.converterClass() == VoidConverter.class) {
			if (converter == null) {
				throw new IllegalArgumentException("No converter available for type: " + fieldInfo.getType());
			} else {
				// use the passed in one
			}
		} else {
			@SuppressWarnings("unchecked")
			Converter<T, Object> castConverter =
					(Converter<T, Object>) ConverterUtils.constructConverter(csvField.converterClass());
			converter = castConverter;
		}
		String format;
		if (csvField.format().equals(CsvColumn.DEFAULT_VALUE)) {
			format = null;
		} else {
			format = csvField.format();
		}
		Object configInfo = converter.configure(format, csvField.converterFlags(), fieldInfo);
		@SuppressWarnings("unchecked")
		Converter<Object, Object> castConverter = (Converter<Object, Object>) converter;
		boolean needsQuotes = castConverter.isNeedsQuotes(configInfo);

		String columnName;
		if (csvField.columnName().equals(CsvColumn.DEFAULT_VALUE)) {
			columnName = fieldInfo.getName();
		} else {
			columnName = csvField.columnName();
		}
		String defaultValue = null;
		if (!csvField.defaultValue().equals(CsvColumn.DEFAULT_VALUE)) {
			defaultValue = csvField.defaultValue();
		}
		return new ColumnInfo<T>(fieldInfo, converter, configInfo, columnName, fieldMustNotBeBlank(csvField),
				csvField.trimInput(), needsQuotes, defaultValue, fieldMustBeSupplied(csvField));
	}

	/**
	 * For testing purposes.
	 */
	public static <T> ColumnInfo<T> forTests(Converter<T, ?> converter, Object configInfo) {
		return new ColumnInfo<T>(null, converter, configInfo, "name", false, false, false, null, false);
	}

	/**
	 * To isolate the suppress warnings.
	 */
	@SuppressWarnings("deprecation")
	private static boolean fieldMustNotBeBlank(CsvColumn csvField) {
		return (csvField.mustNotBeBlank() || csvField.required());
	}

	/**
	 * To isolate the suppress warnings.
	 */
	@SuppressWarnings("deprecation")
	private static boolean fieldMustBeSupplied(CsvColumn csvField) {
		// must-be-suppled default true but optionalColumn default was false
		return (csvField.mustBeSupplied() && !csvField.optionalColumn());
	}
}
