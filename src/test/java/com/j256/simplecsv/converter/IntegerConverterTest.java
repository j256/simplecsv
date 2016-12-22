package com.j256.simplecsv.converter;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.junit.Test;

import com.j256.simplecsv.processor.ColumnInfo;
import com.j256.simplecsv.processor.ParseError;

public class IntegerConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		IntegerConverter converter = IntegerConverter.getSingleton();
		DecimalFormat configInfo = converter.configure(null, 0, null);
		testNumbers(converter, configInfo);
	}

	@Test
	public void testFormat() throws Exception {
		IntegerConverter converter = IntegerConverter.getSingleton();
		DecimalFormat configInfo = converter.configure("###,##0", 0, null);
		testNumbers(converter, configInfo);
	}

	@Test
	public void testInvalidFormat() throws Exception {
		IntegerConverter converter = IntegerConverter.getSingleton();
		DecimalFormat configInfo = converter.configure(null, 0, null);
		ColumnInfo<Integer> columnInfo = ColumnInfo.forTests(converter, configInfo);
		ParseError parseError = new ParseError();
		assertNull(converter.stringToJava("line", 1, 2, columnInfo, "notanumber", parseError));
		assertTrue(parseError.isError());
	}

	private void testNumbers(IntegerConverter converter, DecimalFormat configInfo) throws ParseException {
		testConverter(converter, configInfo, -1);
		testConverter(converter, configInfo, 0);
		testConverter(converter, configInfo, 1);
		testConverter(converter, configInfo, Integer.MIN_VALUE);
		testConverter(converter, configInfo, Integer.MAX_VALUE);
		testConverter(converter, configInfo, null);
	}
}
