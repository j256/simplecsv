package com.j256.simplecsv.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.j256.simplecsv.common.CsvColumn;
import com.j256.simplecsv.processor.ColumnInfo;
import com.j256.simplecsv.processor.CsvProcessor;
import com.j256.simplecsv.processor.ParseError;

public class EnumConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		EnumConverter converter = EnumConverter.getSingleton();
		testConverter(converter, MyEnum.class, null, 0L, MyEnum.RED);
		testConverter(converter, MyEnum.class, null, 0, MyEnum.BLUE);
		testConverter(converter, MyEnum.class, null, 0, MyEnum.GREEN);
		testConverter(converter, MyEnum.class, null, 0, null);
	}

	@Test
	public void testUnknown() {
		EnumConverter converter = EnumConverter.getSingleton();
		MyEnum unknownValue = MyEnum.RED;
		ColumnInfo<Enum<?>> columnInfo = ColumnInfo.forTests(converter, MyEnum.class, unknownValue.name(),
				EnumConverter.FORMAT_IS_UNKNOWN_VALUE);
		ParseError parseError = new ParseError();
		Enum<?> converted = converter.stringToJava("line", 1, 2, columnInfo, "unknown-value", parseError);
		assertEquals(unknownValue, converted);
		assertFalse(parseError.isError());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNotEnum() {
		EnumConverter converter = EnumConverter.getSingleton();
		ColumnInfo.forTests(converter, MyEnum.class, null, EnumConverter.FORMAT_IS_UNKNOWN_VALUE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnknownUnknown() {
		EnumConverter converter = EnumConverter.getSingleton();
		ColumnInfo.forTests(converter, MyEnum.class, "unknownenumvalue", EnumConverter.FORMAT_IS_UNKNOWN_VALUE);
	}

	@Test
	public void testUnknownAndNoUnknown() {
		EnumConverter converter = EnumConverter.getSingleton();
		ColumnInfo<Enum<?>> columnInfo = ColumnInfo.forTests(converter, MyEnum.class, null, 0);
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
		@CsvColumn
		private MyEnum myEnum;
		@CsvColumn
		private String notEnum;

		public MyObject() {
			// for simplecsv
		}
	}
}
