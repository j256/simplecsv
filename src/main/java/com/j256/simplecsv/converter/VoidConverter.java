package com.j256.simplecsv.converter;

import com.j256.simplecsv.processor.ColumnInfo;
import com.j256.simplecsv.processor.FieldInfo;
import com.j256.simplecsv.processor.ParseError;

/**
 * Place holder so we can configure the fields with a default converter.
 * 
 * @author graywatson
 */
public class VoidConverter implements Converter<Void, Void> {

	@Override
	public Void configure(String format, long flags, FieldInfo<Void> fieldInfo) {
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
	public String javaToString(ColumnInfo<Void> columnInfo, Void value) {
		return null;
	}

	@Override
	public Void stringToJava(String line, int lineNumber, int linePos, ColumnInfo<Void> columnInfo, String value,
			ParseError parseError) {
		return null;
	}
}
