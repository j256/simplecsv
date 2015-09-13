package com.j256.simplecsv.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.Test;

import com.j256.simplecsv.common.CsvField;
import com.j256.simplecsv.converter.EnumConverter.ConfigInfo;
import com.j256.simplecsv.processor.ColumnInfo;
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
		Enum<?> converted = converter.stringToJava("line", 1, columnInfo, "unknown-value", parseError);
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
		assertNull(converter.stringToJava("line", 1, columnInfo, "unknown-value", parseError));
		assertTrue(parseError.isError());
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

	private static class MyObject {
		@CsvField
		private MyEnum myEnum;
		@CsvField
		private String notEnum;
	}
}
