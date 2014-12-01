package com.j256.simplecsv.converter;

import java.lang.reflect.Field;

import com.j256.simplecsv.processor.ColumnInfo;
import com.j256.simplecsv.processor.ParseError;

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
		return false;
	}

	@Override
	public boolean isAlwaysTrimInput() {
		return false;
	}

	@Override
	public String javaToString(ColumnInfo columnInfo, Void value) {
		return null;
	}

	@Override
	public Void stringToJava(String line, int lineNumber, ColumnInfo columnInfo, String value, ParseError parseError) {
		return null;
	}
}
