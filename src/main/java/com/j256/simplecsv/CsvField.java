package com.j256.simplecsv;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.j256.simplecsv.converter.Converter;
import com.j256.simplecsv.converter.StringConverter;
import com.j256.simplecsv.converter.VoidConverter;

/**
 * Field to be exported in CSV output.
 * 
 * @author graywatson
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvField {

	public static final String DEFAULT_VALUE = "__simplecsv__ default";

	/**
	 * Sets the name of the cell used in the header and for logging. If not specified, then the field name will be used.
	 */
	public String cellName() default DEFAULT_VALUE;

	/**
	 * Set to true if a value in the field is required -- i.e. the field cannot be empty.
	 */
	public boolean required() default false;

	/**
	 * Allows null values. If a null is used then an empty string is written to the CSV fields.
	 * 
	 * <p>
	 * WARNING: When this is set to true, for certain types, the read value will be different from the written one. For
	 * example, if this is true and you write a null String as "", when you read it in you will get "" and _not_ null.
	 * If this is not what you want then you will need to write a custom converter class. See
	 * {@link StringConverter#BLANK_IS_NULL}.
	 * </p>
	 */
	public boolean allowNull() default true;

	/**
	 * Set to true if you want the field read from the line to be trimmed before it is converted to Java. This may not
	 * apply to all field types.
	 */
	public boolean trimInput() default false;

	/**
	 * Sets the format for this field. Not all types use the format specifier. Take a look at the particular converter
	 * class for more particulars. The default tends to be the toString() and often use of the java.text.Format classes
	 * are used to override.
	 */
	public String format() default DEFAULT_VALUE;

	/**
	 * Flags for the converter which adjust the output. Depending on the converter, these flags may not exist. These
	 * need to be constants that are added together. For example,
	 * 
	 * <pre>
	 * &#064;CsvField(converterFlags = XxxConverter.FLAG1 + XxxConverter.FLAG2)
	 * private Xxx dollarAmount;
	 * </pre>
	 */
	public long converterFlags() default 0;

	/**
	 * What converter class to use. By default it will use the appropriate internal class.
	 */
	public Class<? extends Converter<?, ?>> converterClass() default VoidConverter.class;

	/**
	 * Set this to a default string that if the cell is empty when read, the value will be used instead. Default is the
	 * empty string.
	 */
	public String defaultValue() default DEFAULT_VALUE;
}
