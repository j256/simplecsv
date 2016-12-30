package com.j256.simplecsv.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.Test;

import com.j256.simplecsv.common.CsvColumn;
import com.j256.simplecsv.converter.BooleanConverter;
import com.j256.simplecsv.converter.Converter;
import com.j256.simplecsv.converter.IntegerConverter;
import com.j256.simplecsv.converter.LongConverter;
import com.j256.simplecsv.converter.StringConverter;

public class ColumnInfoTest {

	@Test
	public void testStuff() throws Exception {
		String fieldName = "field";
		Field field = MyClass.class.getDeclaredField(fieldName);
		@SuppressWarnings("unchecked")
		Class<Integer> castType = (Class<Integer>) field.getType();
		ColumnInfo<Integer> columnInfo = ColumnInfo.fromAnnotation(field.getAnnotation(CsvColumn.class), fieldName,
				castType, field, null, null, IntegerConverter.getSingleton());
		assertNull(columnInfo.getDefaultValue());
		assertEquals(fieldName, columnInfo.getColumnName());
		assertFalse(columnInfo.isMustNotBeBlank());
	}

	@Test
	public void testNameSet() throws Exception {
		String fieldName = "hasName";
		Field field = MyClass.class.getDeclaredField(fieldName);
		@SuppressWarnings("unchecked")
		Class<Integer> castType = (Class<Integer>) field.getType();
		ColumnInfo<Integer> columnInfo = ColumnInfo.fromAnnotation(field.getAnnotation(CsvColumn.class), fieldName,
				castType, field, null, null, IntegerConverter.getSingleton());
		assertEquals(MyClass.HAS_NAME_FIELD_NAME, columnInfo.getColumnName());
	}

	@Test
	public void testDefaultValue() throws Exception {
		String fieldName = "defaultValue";
		Field field = MyClass.class.getDeclaredField(fieldName);
		@SuppressWarnings("unchecked")
		Class<Integer> castType = (Class<Integer>) field.getType();
		ColumnInfo<Integer> columnInfo = ColumnInfo.fromAnnotation(field.getAnnotation(CsvColumn.class), fieldName,
				castType, field, null, null, IntegerConverter.getSingleton());
		assertEquals(fieldName, columnInfo.getDefaultValue());
	}

	@Test
	public void testCustomConverter() throws Exception {
		String fieldName = "specialString";
		Field field = MyClass.class.getDeclaredField(fieldName);
		@SuppressWarnings("unchecked")
		Class<String> castType = (Class<String>) field.getType();
		ColumnInfo<String> columnInfo = ColumnInfo.fromAnnotation(field.getAnnotation(CsvColumn.class), fieldName,
				castType, field, null, null, null);
		assertTrue(columnInfo.getConverter() instanceof MyConverter);
	}

	@Test
	public void testFormat() throws Exception {
		String fieldName = "number";
		Field field = MyClass.class.getDeclaredField(fieldName);
		@SuppressWarnings("unchecked")
		Class<Long> castType = (Class<Long>) field.getType();
		ColumnInfo<Long> columnInfo = ColumnInfo.fromAnnotation(field.getAnnotation(CsvColumn.class), fieldName,
				castType, field, null, null, LongConverter.getSingleton());
		assertNotNull(columnInfo.getConfigInfo());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoConverter() throws Exception {
		String fieldName = "defaultValue";
		Field field = MyClass.class.getDeclaredField(fieldName);
		@SuppressWarnings("unchecked")
		Class<Integer> castType = (Class<Integer>) field.getType();
		ColumnInfo.fromAnnotation(field.getAnnotation(CsvColumn.class), fieldName, castType, field, null, null, null);
	}

	@Test
	public void testTrimInput() throws Exception {
		String fieldName = "number";
		Field field = MyClass.class.getDeclaredField(fieldName);
		@SuppressWarnings("unchecked")
		Class<Long> castType = (Class<Long>) field.getType();
		ColumnInfo<Long> columnInfo = ColumnInfo.fromAnnotation(field.getAnnotation(CsvColumn.class), fieldName,
				castType, field, null, null, LongConverter.getSingleton());
		assertFalse(columnInfo.isTrimInput());

		fieldName = "trim";
		field = MyClass.class.getDeclaredField(fieldName);
		@SuppressWarnings("unchecked")
		Class<Boolean> castType2 = (Class<Boolean>) field.getType();
		ColumnInfo<Boolean> otherColumnInfo = ColumnInfo.fromAnnotation(field.getAnnotation(CsvColumn.class), fieldName,
				castType2, field, null, null, BooleanConverter.getSingleton());
		assertTrue(otherColumnInfo.isTrimInput());
	}

	@Test
	public void testNeedsQuotes() throws Exception {
		String fieldName = "field";
		Field field = MyClass.class.getDeclaredField(fieldName);
		@SuppressWarnings("unchecked")
		Class<Integer> castType = (Class<Integer>) field.getType();
		ColumnInfo<Integer> columnInfo = ColumnInfo.fromAnnotation(field.getAnnotation(CsvColumn.class), fieldName,
				castType, field, null, null, IntegerConverter.getSingleton());
		assertFalse(columnInfo.isNeedsQuotes());

		fieldName = "number";
		field = MyClass.class.getDeclaredField("defaultValue");
		@SuppressWarnings("unchecked")
		Class<String> castType2 = (Class<String>) field.getType();
		ColumnInfo<String> otherColumnInfo = ColumnInfo.fromAnnotation(field.getAnnotation(CsvColumn.class), fieldName,
				castType2, field, null, null, StringConverter.getSingleton());
		assertTrue(otherColumnInfo.isNeedsQuotes());
	}

	@Test
	public void testMustBeSupplied() throws Exception {
		String fieldName = "trim";
		Field field = MyClass.class.getDeclaredField(fieldName);
		@SuppressWarnings("unchecked")
		Class<Boolean> castType = (Class<Boolean>) field.getType();
		ColumnInfo<Boolean> columnInfo = ColumnInfo.fromAnnotation(field.getAnnotation(CsvColumn.class), fieldName,
				castType, field, null, null, BooleanConverter.getSingleton());
		assertTrue(columnInfo.isMustBeSupplied());

		fieldName = "mustBeSupplied";
		field = MyClass.class.getDeclaredField(fieldName);
		@SuppressWarnings("unchecked")
		Class<Boolean> castType2 = (Class<Boolean>) field.getType();
		columnInfo = ColumnInfo.fromAnnotation(field.getAnnotation(CsvColumn.class), fieldName, castType2, field, null,
				null, BooleanConverter.getSingleton());
		assertFalse(columnInfo.isMustBeSupplied());
	}

	@Test
	public void testPosition() throws Exception {
		String fieldName = "trim";
		Field field = MyClass.class.getDeclaredField(fieldName);
		@SuppressWarnings("unchecked")
		Class<Boolean> castType = (Class<Boolean>) field.getType();
		ColumnInfo<Boolean> columnInfo = ColumnInfo.fromAnnotation(field.getAnnotation(CsvColumn.class), fieldName,
				castType, field, null, null, BooleanConverter.getSingleton());
		assertEquals(0, columnInfo.getPosition());
		int position = 12312321;
		columnInfo.setPosition(position);
		assertEquals(position, columnInfo.getPosition());
	}

	private static class MyClass {
		public static final String HAS_NAME_FIELD_NAME = "not has Name";
		@CsvColumn
		private int field;
		@CsvColumn(columnName = HAS_NAME_FIELD_NAME)
		private int hasName;
		@CsvColumn(defaultValue = "defaultValue")
		private String defaultValue;
		@CsvColumn(converterClass = MyConverter.class)
		private String specialString;
		@CsvColumn(format = "###,##0")
		private long number;
		@CsvColumn(trimInput = true)
		private boolean trim;
		@CsvColumn(mustBeSupplied = false)
		private boolean mustBeSupplied;
		@SuppressWarnings("unused")
		private boolean noAnnotation;
	}

	public static class MyConverter implements Converter<String, Void> {
		@Override
		public Void configure(String format, long flags, ColumnInfo<String> field) {
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
		public String javaToString(ColumnInfo<String> columnInfo, String value) {
			return value;
		}

		@Override
		public String stringToJava(String line, int lineNumber, int linePos, ColumnInfo<String> columnInfo,
				String value, ParseError parseError) {
			return value;
		}
	}
}
