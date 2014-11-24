package com.j256.simplecsv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.Test;

import com.j256.simplecsv.converter.Converter;
import com.j256.simplecsv.converter.IntegerConverter;
import com.j256.simplecsv.converter.LongConverter;

public class FieldInfoTest {

	@Test
	public void testStuff() throws Exception {
		String fieldName = "field";
		Field field = MyClass.class.getDeclaredField(fieldName);
		FieldInfo fieldInfo = FieldInfo.fromField(field, IntegerConverter.getSingleton());
		assertSame(field, fieldInfo.getField());
		assertNull(fieldInfo.getDefaultValue());
		assertEquals(fieldName, fieldInfo.getCellName());
		assertFalse(fieldInfo.isRequired());
	}

	@Test
	public void testNameSet() throws Exception {
		String fieldName = "hasName";
		Field field = MyClass.class.getDeclaredField(fieldName);
		FieldInfo fieldInfo = FieldInfo.fromField(field, IntegerConverter.getSingleton());
		assertEquals(MyClass.HAS_NAME_FIELD_NAME, fieldInfo.getCellName());
	}

	@Test
	public void testDefaultValue() throws Exception {
		String fieldName = "defaultValue";
		Field field = MyClass.class.getDeclaredField(fieldName);
		FieldInfo fieldInfo = FieldInfo.fromField(field, IntegerConverter.getSingleton());
		assertEquals(fieldName, fieldInfo.getDefaultValue());
	}

	@Test
	public void testCustomConverter() throws Exception {
		String fieldName = "specialString";
		Field field = MyClass.class.getDeclaredField(fieldName);
		FieldInfo fieldInfo = FieldInfo.fromField(field, null);
		assertTrue(fieldInfo.getConverter() instanceof MyConverter);
	}

	@Test
	public void testFormat() throws Exception {
		String fieldName = "number";
		Field field = MyClass.class.getDeclaredField(fieldName);
		FieldInfo fieldInfo = FieldInfo.fromField(field, LongConverter.getSingleton());
		assertNotNull(fieldInfo.getConfigInfo());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoConverter() throws Exception {
		Field field = MyClass.class.getDeclaredField("defaultValue");
		FieldInfo.fromField(field, null);
	}

	private static class MyClass {
		public static final String HAS_NAME_FIELD_NAME = "not has Name";
		@CsvField
		private int field;
		@CsvField(cellName = HAS_NAME_FIELD_NAME)
		private int hasName;
		@CsvField(defaultValue = "defaultValue")
		private String defaultValue;
		@CsvField(converterClass = MyConverter.class)
		private String specialString;
		@CsvField(format = "###,##0")
		private long number;
	}

	public static class MyConverter implements Converter<String, Void> {
		@Override
		public Void configure(String format, long flags, Field field) {
			return null;
		}
		@Override
		public boolean isNeedsQuotes(Void configInfo) {
			return false;
		}
		@Override
		public String javaToString(FieldInfo fieldInfo, String value) {
			return value;
		}
		@Override
		public String stringToJava(String line, int lineNumber, FieldInfo fieldInfo, String value, ParseError parseError) {
			return value;
		}
	}
}
