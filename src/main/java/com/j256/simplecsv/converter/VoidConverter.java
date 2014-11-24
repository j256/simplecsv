package com.j256.simplecsv.converter;

import java.lang.reflect.Field;

import com.j256.simplecsv.FieldInfo;
import com.j256.simplecsv.ParseError;

/**
 * Place holder so we can configure the fields with a default converter.
 * 
 * @author graywatson
 */
public class VoidConverter implements Converter<Void, Void> {

	@Override
	public Void configure(String format, long flags, Field field) {
		// no op
		return null;
	}

	@Override
	public boolean isNeedsQuotes(Void configInfo) {
		return true;
	}

	@Override
	public String javaToString(FieldInfo fieldInfo, Void value) {
		return null;
	}

	@Override
	public Void stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError) {
		return null;
	}
}
