package com.j256.simplecsv.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.DecimalFormat;

import com.j256.simplecsv.converter.Converter;
import com.j256.simplecsv.converter.VoidConverter;
import com.j256.simplecsv.processor.CsvProcessor;

/**
 * Annotation to be added to a field to mark it as a column in a CSV file.
 * 
 * @author graywatson
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvField {

	/** Used internally to detect whether or not a value has been configured. */
	public static final String DEFAULT_VALUE = "__simplecsv__ default";

	/**
	 * This allows you to override and set a column name for the field. By default it will use the field name. This
	 * column name is used when you are generating and validating the header line.
	 */
	public String columnName() default DEFAULT_VALUE;

	/**
	 * Set to true if a value in the column cannot be empty when it is being read in and a parse error or exception will
	 * be generated.
	 */
	public boolean mustNotBeBlank() default false;

	/**
	 * Set to true if you want the column read from the line to be trimmed (using {@link String#trim()}) before it is
	 * converted to Java. This may not be applicable to all field types.
	 */
	public boolean trimInput() default false;

	/**
	 * Sets the format for this column. Not all types use the format specifier. Take a look at the particular converter
	 * class javadocs for more particulars. The default format tends to be the toString() of the type, and (for example)
	 * the {@link DecimalFormat} class is used to override for numbers.
	 */
	public String format() default DEFAULT_VALUE;

	/**
	 * Optional flags for the converter which adjust the output. The flags that are used depend on the converter. See
	 * the converter Javadocs for more information. These need to be constants that are added together. For example,
	 * 
	 * <pre>
	 * &#064;CsvField(converterFlags = XxxConverter.FLAG1 + XxxConverter.FLAG2)
	 * private Xxx dollarAmount;
	 * </pre>
	 */
	public long converterFlags() default 0;

	/**
	 * Sets the converter to use to convert this column if you don't want to use the default appropriate internal class.
	 * This will construct and instance of the class for this particular field. If you want to use a singleton then you
	 * should register the type using {@link CsvProcessor#registerConverter(Class, Converter)}. This converter class
	 * must have a public no-arg constructor.
	 */
	public Class<? extends Converter<?, ?>> converterClass() default VoidConverter.class;

	/**
	 * Set this to a default string for the column. If the column is empty when read, the value will be used instead.
	 * Default is the empty string.
	 */
	public String defaultValue() default DEFAULT_VALUE;

	/**
	 * Set to false if a column is optional and can be skipped in the input altogether. If this is false then the column
	 * doesn't have to be in the header or the lines at all. Default is true.
	 * 
	 * <b>WARNING:</b> If you are using optional ordering, the same CsvProcessor cannot be used with multiple files at
	 * the same time since the column lists can be dynamic depending on the input file being read.
	 */
	public boolean mustBeSupplied() default true;

	/**
	 * @deprecated Should use {@link #mustNotBeBlank()} instead.
	 */
	@Deprecated
	public boolean required() default false;

	/**
	 * @deprecated Should use {@link #mustBeSupplied()} instead.
	 */
	@Deprecated
	public boolean optionalColumn() default false;
}
