package com.j256.simplecsv.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.Test;

import com.j256.simplecsv.common.CsvField;
import com.j256.simplecsv.converter.Converter;
import com.j256.simplecsv.converter.IntegerConverter;
import com.j256.simplecsv.converter.LongConverter;

public class FieldInfoTest {

	@Test
	public void testStuff() throws Exception {
		String fieldName = "field";
		Field field = MyClass.class.getDeclaredField(fieldName);
		ColumnInfo columnInfo = ColumnInfo.fromField(field, IntegerConverter.getSingleton(), 0);
		assertSame(field, columnInfo.getField());
		assertNull(columnInfo.getDefaultValue());
		assertEquals(fieldName, columnInfo.getColumnName());
		assertFalse(columnInfo.isRequired());
	}

	@Test
	public void testNameSet() throws Exception {
		String fieldName = "hasName";
		Field field = MyClass.class.getDeclaredField(fieldName);
		ColumnInfo columnInfo = ColumnInfo.fromField(field, IntegerConverter.getSingleton(), 0);
		assertEquals(MyClass.HAS_NAME_FIELD_NAME, columnInfo.getColumnName());
	}

	@Test
	public void testDefaultValue() throws Exception {
		String fieldName = "defaultValue";
		Field field = MyClass.class.getDeclaredField(fieldName);
		ColumnInfo columnInfo = ColumnInfo.fromField(field, IntegerConverter.getSingleton(), 0);
		assertEquals(fieldName, columnInfo.getDefaultValue());
	}

	@Test
	public void testCustomConverter() throws Exception {
		String fieldName = "specialString";
		Field field = MyClass.class.getDeclaredField(fieldName);
		ColumnInfo columnInfo = ColumnInfo.fromField(field, null, 0);
		assertTrue(columnInfo.getConverter() instanceof MyConverter);
	}

	@Test
	public void testFormat() throws Exception {
		String fieldName = "number";
		Field field = MyClass.class.getDeclaredField(fieldName);
		ColumnInfo columnInfo = ColumnInfo.fromField(field, LongConverter.getSingleton(), 0);
		assertNotNull(columnInfo.getConfigInfo());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoConverter() throws Exception {
		Field field = MyClass.class.getDeclaredField("defaultValue");
		ColumnInfo.fromField(field, null, 0);
	}

	private static class MyClass {
		public static final String HAS_NAME_FIELD_NAME = "not has Name";
		@CsvField
		private int field;
		@CsvField(columnName = HAS_NAME_FIELD_NAME)
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
		public boolean isAlwaysTrimInput() {
			return false;
		}
		@Override
		public String javaToString(ColumnInfo columnInfo, String value) {
			return value;
		}
		@Override
		public String stringToJava(String line, int lineNumber, ColumnInfo columnInfo, String value,
				ParseError parseError) {
			return value;
		}
	}
}
