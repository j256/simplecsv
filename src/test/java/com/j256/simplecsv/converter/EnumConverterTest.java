package com.j256.simplecsv.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.Test;

import com.j256.simplecsv.CsvField;
import com.j256.simplecsv.ParseError;

public class EnumConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		EnumConverter converter = new EnumConverter();
		Field field = MyObject.class.getDeclaredField("myEnum");
		converter.configure(null, 0, field);

		testConverter(converter, MyEnum.RED);
		testConverter(converter, MyEnum.BLUE);
		testConverter(converter, MyEnum.GREEN);
		testConverter(converter, null);
	}

	@Test
	public void testUnknown() throws Exception {
		EnumConverter converter = new EnumConverter();
		Field field = MyObject.class.getDeclaredField("myEnum");
		MyEnum unknownValue = MyEnum.RED;
		converter.configure(unknownValue.name(), EnumConverter.FORMAT_IS_UNKNOWN_VALUE, field);

		ParseError parseError = new ParseError();
		Enum<?> converted = converter.stringToJava("line", 1, null, "unknown-value", parseError);
		assertEquals(unknownValue, converted);
		assertFalse(parseError.isError());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNotEnum() throws Exception {
		EnumConverter converter = new EnumConverter();
		Field field = MyObject.class.getDeclaredField("notEnum");
		converter.configure(null, 0, field);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnknownUnknown() throws Exception {
		EnumConverter converter = new EnumConverter();
		Field field = MyObject.class.getDeclaredField("myEnum");
		converter.configure("unknownenumvalue", EnumConverter.FORMAT_IS_UNKNOWN_VALUE, field);
	}

	@Test
	public void testUnknownAndNoUnknown() throws Exception {
		EnumConverter converter = new EnumConverter();
		Field field = MyObject.class.getDeclaredField("myEnum");
		converter.configure(null, 0, field);
		ParseError parseError = new ParseError();
		assertNull(converter.stringToJava("line", 1, null, "unknown-value", parseError));
		assertTrue(parseError.isError());
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
