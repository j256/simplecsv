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
 * Annotation to be added to a field or method to mark it as a column in a CSV file.
 * 
 * <p>
 * <b>NOTE:</b> When using on a get/is/set methods you need to add this annotation to <i>both</i> the get/is and the set
 * methods. Also, you need to make sure that the CsvColumn annotation fields are the same on both the get/is and set
 * methods.
 * </p>
 * 
 * @author graywatson
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface CsvColumn {

	/** Used internally to detect whether or not a value has been configured. */
	public static final String DEFAULT_VALUE = "__simplecsv__ default";

	/**
	 * This allows you to override and set a column name. By default it will use the field or method name. This column
	 * name is used when you are generating and validating the header line.
	 */
	public String columnName() default DEFAULT_VALUE;

	/**
	 * Set to true if a value in the column cannot be empty when it is being read in and a parse error or exception will
	 * be generated.
	 */
	public boolean mustNotBeBlank() default false;

	/**
	 * Set to true if you want the column read from the line to be trimmed (using {@link String#trim()}) before it is
	 * converted to Java. This may not be applicable to all column types.
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
	 * &#064;CsvColumn(converterFlags = XxxConverter.FLAG1 + XxxConverter.FLAG2)
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
	 * Used to set the order of the columns by setting the column-name that this column comes after. If this is not
	 * specified then the order in which the fields and methods are discovered in the classes will determine their order
	 * in the CSV file. If two fields say they come after the same field then you will get an undefined order. If there
	 * is an loop in the after columns then an exception will be thrown.
	 */
	public String afterColumn() default DEFAULT_VALUE;
}
