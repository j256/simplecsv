package com.j256.simplecsv.processor;

import com.j256.simplecsv.common.CsvColumn;
import com.j256.simplecsv.common.CsvField;
import com.j256.simplecsv.converter.Converter;
import com.j256.simplecsv.converter.ConverterUtils;
import com.j256.simplecsv.converter.VoidConverter;

/**
 * Information about a particular CSV column used internally to keep track of the CSV columns.
 * 
 * @param <T>
 *            The type of the column whose information we are storing in here.
 * @author graywatson
 */
@SuppressWarnings("deprecation")
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
	private final String afterColumn;

	private ColumnInfo(FieldInfo<T> fieldInfo, Converter<T, ?> converter, Object configInfo, String columnName,
			boolean mustNotBeBlank, boolean trimInput, boolean needsQuotes, String defaultValue, boolean mustBeSupplied,
			String afterColumn) {
		this.fieldInfo = fieldInfo;
		this.converter = converter;
		this.configInfo = configInfo;
		this.columnName = columnName;
		this.mustNotBeBlank = mustNotBeBlank;
		this.trimInput = trimInput;
		this.needsQuotes = needsQuotes;
		this.defaultValue = defaultValue;
		this.mustBeSupplied = mustBeSupplied;
		this.afterColumn = afterColumn;
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
	 * Column name that we come after.
	 */
	public String getAfterColumn() {
		return afterColumn;
	}

	/**
	 * Make a column-info instance from a Java Field.
	 */
	public static <T> ColumnInfo<T> fromFieldInfo(CsvColumn csvColumn, FieldInfo<T> fieldInfo,
			Converter<T, ?> converter) {
		return fromFieldInfo(csvColumn.converterClass(), csvColumn.format(), csvColumn.converterFlags(),
				csvColumn.columnName(), csvColumn.defaultValue(), csvColumn.afterColumn(), csvColumn.mustNotBeBlank(),
				csvColumn.mustBeSupplied(), csvColumn.trimInput(), fieldInfo, converter);
	}

	/**
	 * Make a column-info instance from a Java Field.
	 */
	public static <T> ColumnInfo<T> fromFieldInfo(CsvField csvField, FieldInfo<T> fieldInfo,
			Converter<T, ?> converter) {
		return fromFieldInfo(csvField.converterClass(), csvField.format(), csvField.converterFlags(),
				csvField.columnName(), csvField.defaultValue(), null, csvField.mustNotBeBlank(),
				csvField.mustBeSupplied(), csvField.trimInput(), fieldInfo, converter);
	}

	private static <T> ColumnInfo<T> fromFieldInfo(Class<? extends Converter<?, ?>> converterClass, String format,
			long converterFlags, String columnName, String defaultValue, String afterColumn, boolean mustNotBeBlank,
			boolean mustBeSupplied, boolean trimInput, FieldInfo<T> fieldInfo, Converter<T, ?> converter) {
		if (converterClass == VoidConverter.class) {
			if (converter == null) {
				throw new IllegalArgumentException("No converter available for type: " + fieldInfo.getType());
			} else {
				// use the passed in one
			}
		} else {
			@SuppressWarnings("unchecked")
			Converter<T, Object> castConverter =
					(Converter<T, Object>) ConverterUtils.constructConverter(converterClass);
			converter = castConverter;
		}
		if (format != null && format.equals(CsvColumn.DEFAULT_VALUE)) {
			format = null;
		}
		Object configInfo = converter.configure(format, converterFlags, fieldInfo);
		@SuppressWarnings("unchecked")
		Converter<Object, Object> castConverter = (Converter<Object, Object>) converter;
		boolean needsQuotes = castConverter.isNeedsQuotes(configInfo);

		if (columnName != null && columnName.equals(CsvColumn.DEFAULT_VALUE)) {
			columnName = fieldInfo.getName();
		}
		if (defaultValue != null && defaultValue.equals(CsvColumn.DEFAULT_VALUE)) {
			defaultValue = null;
		}
		if (afterColumn != null && afterColumn.equals(CsvColumn.DEFAULT_VALUE)) {
			afterColumn = null;
		}
		return new ColumnInfo<T>(fieldInfo, converter, configInfo, columnName, mustNotBeBlank, trimInput, needsQuotes,
				defaultValue, mustBeSupplied, afterColumn);
	}

	/**
	 * For testing purposes.
	 */
	public static <T> ColumnInfo<T> forTests(Converter<T, ?> converter, Object configInfo) {
		return new ColumnInfo<T>(null, converter, configInfo, "name", false, false, false, null, false, null);
	}

	@Override
	public String toString() {
		return "ColumnInfo [name=" + fieldInfo.getName() + ", type=" + fieldInfo.getType() + "]";
	}
}
