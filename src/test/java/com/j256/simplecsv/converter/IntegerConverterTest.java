package com.j256.simplecsv.converter;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.junit.Test;

import com.j256.simplecsv.processor.ColumnInfo;
import com.j256.simplecsv.processor.ParseError;

public class IntegerConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		IntegerConverter converter = IntegerConverter.getSingleton();
		testNumbers(converter, null);
	}

	@Test
	public void testFormat() throws Exception {
		IntegerConverter converter = IntegerConverter.getSingleton();
		testNumbers(converter, "###,##0");
	}

	@Test
	public void testInvalidFormat() throws Exception {
		IntegerConverter converter = IntegerConverter.getSingleton();
		ColumnInfo<Integer> columnInfo = ColumnInfo.forTests(converter, Integer.class, null, 0);
		ParseError parseError = new ParseError();
		assertNull(converter.stringToJava("line", 1, 2, columnInfo, "notanumber", parseError));
		assertTrue(parseError.isError());
	}

	private void testNumbers(IntegerConverter converter, String format) throws ParseException {
		testConverter(converter, Integer.class, format, 0, -1);
		testConverter(converter, Integer.class, format, 0, 0);
		testConverter(converter, Integer.class, format, 0, 1);
		testConverter(converter, Integer.class, format, 0, Integer.MIN_VALUE);
		testConverter(converter, Integer.class, format, 0, Integer.MAX_VALUE);
		testConverter(converter, Integer.class, format, 0, null);
	}
}
