package com.j256.simplecsv.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.j256.simplecsv.common.CsvField;
import com.j256.simplecsv.converter.EnumConverter.ConfigInfo;
import com.j256.simplecsv.processor.ColumnInfo;
import com.j256.simplecsv.processor.CsvProcessor;
import com.j256.simplecsv.processor.ParseError;

public class EnumConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		EnumConverter converter = EnumConverter.getSingleton();
		Field field = MyObject.class.getDeclaredField("myEnum");
		ConfigInfo configInfo = converter.configure(null, 0, field);
		testConverter(converter, configInfo, MyEnum.RED);
		testConverter(converter, configInfo, MyEnum.BLUE);
		testConverter(converter, configInfo, MyEnum.GREEN);
		testConverter(converter, configInfo, null);
	}

	@Test
	public void testUnknown() throws Exception {
		EnumConverter converter = EnumConverter.getSingleton();
		Field field = MyObject.class.getDeclaredField("myEnum");
		MyEnum unknownValue = MyEnum.RED;
		ConfigInfo configInfo = converter.configure(unknownValue.name(), EnumConverter.FORMAT_IS_UNKNOWN_VALUE, field);
		ColumnInfo columnInfo = ColumnInfo.forTests(converter, configInfo);
		ParseError parseError = new ParseError();
		Enum<?> converted = converter.stringToJava("line", 1, 2, columnInfo, "unknown-value", parseError);
		assertEquals(unknownValue, converted);
		assertFalse(parseError.isError());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNotEnum() throws Exception {
		EnumConverter converter = EnumConverter.getSingleton();
		Field field = MyObject.class.getDeclaredField("notEnum");
		converter.configure(null, 0, field);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnknownUnknown() throws Exception {
		EnumConverter converter = EnumConverter.getSingleton();
		Field field = MyObject.class.getDeclaredField("myEnum");
		converter.configure("unknownenumvalue", EnumConverter.FORMAT_IS_UNKNOWN_VALUE, field);
	}

	@Test
	public void testUnknownAndNoUnknown() throws Exception {
		EnumConverter converter = EnumConverter.getSingleton();
		Field field = MyObject.class.getDeclaredField("myEnum");
		ConfigInfo configInfo = converter.configure(null, 0, field);
		ColumnInfo columnInfo = ColumnInfo.forTests(converter, configInfo);
		ParseError parseError = new ParseError();
		assertNull(converter.stringToJava("line", 1, 2, columnInfo, "unknown-value", parseError));
		assertTrue(parseError.isError());
	}

	@Test
	public void testEnumDiscovery() throws Exception {
		CsvProcessor<MyObject> processor = new CsvProcessor<MyObject>(MyObject.class).withFirstLineHeader(false);
		// column names with suffixes
		MyEnum myEnum = MyEnum.RED;
		String notEnum = "eqdepwd";
		StringReader reader = new StringReader(myEnum + "," + notEnum + "\n");
		List<ParseError> parseErrors = new ArrayList<ParseError>();
		List<MyObject> results = processor.readAll(reader, parseErrors);
		assertEquals(0, parseErrors.size());
		assertEquals(1, results.size());
		assertEquals(myEnum, results.get(0).myEnum);
		assertEquals(notEnum, results.get(0).notEnum);
	}

	@Test
	public void testConverage() {
		EnumConverter converter = EnumConverter.getSingleton();
		assertTrue(converter.isNeedsQuotes(null));
		assertTrue(converter.isAlwaysTrimInput());
	}

	private enum MyEnum {
		RED,
		BLUE,
		GREEN,
		// end
		;
	}

	protected static class MyObject {
		@CsvField
		private MyEnum myEnum;
		@CsvField
		private String notEnum;

		public MyObject() {
			// for simplecsv
		}
	}
}
