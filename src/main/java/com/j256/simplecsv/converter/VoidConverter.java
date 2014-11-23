package com.j256.simplecsv.converter;

import java.lang.reflect.Field;

import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;

/**
 * Place holder so we can configure the fields with a default converter.
 * 
 * @author graywatson
 */
public class VoidConverter implements Converter<Void> {

	@Override
	public void configure(boolean allowNull, String format, long flags, Field field) {
		// no op
	}

	@Override
	public void javaToString(FieldInfo fieldInfo, Void value, StringBuilder sb) {
		// no op
	}

	@Override
	public Void stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError) {
		return null;
	}
}
