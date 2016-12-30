package com.j256.simplecsv.processor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

	private final String fieldName;
	private final Class<T> type;
	// may be null
	private final Field field;
	// may be null
	private final Method getMethod;
	// may be null
	private final Method setMethod;
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

	private ColumnInfo(String fieldName, Class<T> type, Field field, Method getMethod, Method setMethod,
			Converter<T, ?> converter, String format, long converterFlags, String columnName, boolean mustNotBeBlank,
			boolean trimInput, String defaultValue, boolean mustBeSupplied, String afterColumn) {
		this.fieldName = fieldName;
		this.type = type;
		this.field = field;
		this.getMethod = getMethod;
		this.setMethod = setMethod;
		this.converter = converter;
		this.columnName = columnName;
		this.mustNotBeBlank = mustNotBeBlank;
		this.trimInput = trimInput;
		this.defaultValue = defaultValue;
		this.mustBeSupplied = mustBeSupplied;
		this.afterColumn = afterColumn;

		// now that we have setup this class we can call configure the converter to get our config-info
		this.configInfo = converter.configure(format, converterFlags, this);
		@SuppressWarnings("unchecked")
		Converter<Object, Object> castConverter = (Converter<Object, Object>) converter;
		this.needsQuotes = castConverter.isNeedsQuotes(configInfo);
	}

	/**
	 * Get the value associated with this field from the object parameter either by getting from the field or calling
	 * the get method.
	 */
	public T getValue(Object obj) throws IllegalAccessException, InvocationTargetException {
		if (field == null) {
			@SuppressWarnings("unchecked")
			T cast = (T) getMethod.invoke(obj);
			return cast;
		} else {
			@SuppressWarnings("unchecked")
			T cast = (T) field.get(obj);
			return cast;
		}
	}

	/**
	 * Set the value associated with this field from the object parameter either by setting via the field or calling the
	 * set method.
	 */
	public void setValue(Object obj, T value) throws IllegalAccessException, InvocationTargetException {
		if (field == null) {
			setMethod.invoke(obj, value);
		} else {
			field.set(obj, value);
		}
	}

	/**
	 * Name of the java field or the get/set methods.
	 */
	public String getFieldName() {
		return fieldName;
	}

	public Class<T> getType() {
		return type;
	}

	/**
	 * Associated reflection field or null if using get/set method.
	 */
	public Field getField() {
		return field;
	}

	/**
	 * Associated reflection get/is method or null if using field.
	 */
	public Method getGetMethod() {
		return getMethod;
	}

	/**
	 * Associated reflection set method or null if using field.
	 */
	public Method getSetMethod() {
		return setMethod;
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
	 * Returns the header name for this column.
	 * 
	 * @see CsvColumn#columnName()
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * Returns the position the column appears in the file.
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Set the position the column appears in the file.
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
	 * Column name that we come after to have the order not be field or method position based.
	 * 
	 * @see CsvColumn#afterColumn()
	 */
	public String getAfterColumn() {
		return afterColumn;
	}

	/**
	 * Make a column-info instance from a Java Field.
	 */
	public static <T> ColumnInfo<T> fromAnnotation(CsvColumn csvColumn, String fieldName, Class<T> type, Field field,
			Method getMethod, Method setMethod, Converter<T, ?> converter) {
		return fromAnnoation(csvColumn.converterClass(), csvColumn.format(), csvColumn.converterFlags(),
				csvColumn.columnName(), csvColumn.defaultValue(), csvColumn.afterColumn(), csvColumn.mustNotBeBlank(),
				csvColumn.mustBeSupplied(), csvColumn.trimInput(), fieldName, type, field, getMethod, setMethod,
				converter);
	}

	/**
	 * Make a column-info instance from a Java Field.
	 */
	public static <T> ColumnInfo<T> fromAnnotation(CsvField csvField, String fieldName, Class<T> type, Field field,
			Method getMethod, Method setMethod, Converter<T, ?> converter) {
		return fromAnnoation(csvField.converterClass(), csvField.format(), csvField.converterFlags(),
				csvField.columnName(), csvField.defaultValue(), null, csvField.mustNotBeBlank(),
				csvField.mustBeSupplied(), csvField.trimInput(), fieldName, type, field, getMethod, setMethod,
				converter);
	}

	private static <T> ColumnInfo<T> fromAnnoation(Class<? extends Converter<?, ?>> converterClass, String format,
			long converterFlags, String columnName, String defaultValue, String afterColumn, boolean mustNotBeBlank,
			boolean mustBeSupplied, boolean trimInput, String fieldName, Class<T> type, Field field, Method getMethod,
			Method setMethod, Converter<T, ?> converter) {
		if (converterClass == VoidConverter.class) {
			if (converter == null) {
				throw new IllegalArgumentException("No converter available for type: " + type);
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

		if (columnName != null && columnName.equals(CsvColumn.DEFAULT_VALUE)) {
			columnName = fieldName;
		}
		if (defaultValue != null && defaultValue.equals(CsvColumn.DEFAULT_VALUE)) {
			defaultValue = null;
		}
		if (afterColumn != null && afterColumn.equals(CsvColumn.DEFAULT_VALUE)) {
			afterColumn = null;
		}
		return new ColumnInfo<T>(fieldName, type, field, getMethod, setMethod, converter, format, converterFlags,
				columnName, mustNotBeBlank, trimInput, defaultValue, mustBeSupplied, afterColumn);
	}

	/**
	 * For testing purposes.
	 */
	public static <T> ColumnInfo<T> forTests(Converter<T, ?> converter, Class<?> type, String format,
			long converterFlags) {
		@SuppressWarnings("unchecked")
		Class<T> castType = (Class<T>) type;
		return new ColumnInfo<T>("name", castType, null, null, null, converter, format, converterFlags, "name", false,
				false, null, false, null);
	}

	@Override
	public String toString() {
		return "ColumnInfo [name=" + fieldName + ", type=" + type + "]";
	}
}
