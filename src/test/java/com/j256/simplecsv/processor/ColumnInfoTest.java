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
		FieldInfo<Integer> fieldInfo = FieldInfo.fromfield(field);
		ColumnInfo<Integer> columnInfo = ColumnInfo.fromFieldInfo(field.getAnnotation(CsvColumn.class), fieldInfo,
				IntegerConverter.getSingleton());
		assertNull(columnInfo.getDefaultValue());
		assertEquals(fieldName, columnInfo.getColumnName());
		assertFalse(columnInfo.isMustNotBeBlank());
	}

	@Test
	public void testNameSet() throws Exception {
		String fieldName = "hasName";
		Field field = MyClass.class.getDeclaredField(fieldName);
		FieldInfo<Integer> fieldInfo = FieldInfo.fromfield(field);
		ColumnInfo<Integer> columnInfo = ColumnInfo.fromFieldInfo(field.getAnnotation(CsvColumn.class), fieldInfo,
				IntegerConverter.getSingleton());
		assertEquals(MyClass.HAS_NAME_FIELD_NAME, columnInfo.getColumnName());
	}

	@Test
	public void testDefaultValue() throws Exception {
		String fieldName = "defaultValue";
		Field field = MyClass.class.getDeclaredField(fieldName);
		FieldInfo<Integer> fieldInfo = FieldInfo.fromfield(field);
		ColumnInfo<Integer> columnInfo = ColumnInfo.fromFieldInfo(field.getAnnotation(CsvColumn.class), fieldInfo,
				IntegerConverter.getSingleton());
		assertEquals(fieldName, columnInfo.getDefaultValue());
	}

	@Test
	public void testCustomConverter() throws Exception {
		String fieldName = "specialString";
		Field field = MyClass.class.getDeclaredField(fieldName);
		FieldInfo<String> fieldInfo = FieldInfo.fromfield(field);
		ColumnInfo<String> columnInfo = ColumnInfo.fromFieldInfo(field.getAnnotation(CsvColumn.class), fieldInfo, null);
		assertTrue(columnInfo.getConverter() instanceof MyConverter);
	}

	@Test
	public void testFormat() throws Exception {
		String fieldName = "number";
		Field field = MyClass.class.getDeclaredField(fieldName);
		FieldInfo<Long> fieldInfo = FieldInfo.fromfield(field);
		ColumnInfo<Long> columnInfo =
				ColumnInfo.fromFieldInfo(field.getAnnotation(CsvColumn.class), fieldInfo, LongConverter.getSingleton());
		assertNotNull(columnInfo.getConfigInfo());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoConverter() throws Exception {
		Field field = MyClass.class.getDeclaredField("defaultValue");
		FieldInfo<String> fieldInfo = FieldInfo.fromfield(field);
		ColumnInfo.fromFieldInfo(field.getAnnotation(CsvColumn.class), fieldInfo, null);
	}

	@Test
	public void testTrimInput() throws Exception {
		Field field = MyClass.class.getDeclaredField("number");
		FieldInfo<Long> fieldInfo = FieldInfo.fromfield(field);
		ColumnInfo<Long> columnInfo =
				ColumnInfo.fromFieldInfo(field.getAnnotation(CsvColumn.class), fieldInfo, LongConverter.getSingleton());
		assertFalse(columnInfo.isTrimInput());

		field = MyClass.class.getDeclaredField("trim");
		FieldInfo<Boolean> otherFieldInfo = FieldInfo.fromfield(field);
		ColumnInfo<Boolean> otherColumnInfo = ColumnInfo.fromFieldInfo(field.getAnnotation(CsvColumn.class),
				otherFieldInfo, BooleanConverter.getSingleton());
		assertTrue(otherColumnInfo.isTrimInput());
	}

	@Test
	public void testNeedsQuotes() throws Exception {
		Field field = MyClass.class.getDeclaredField("field");
		FieldInfo<Integer> fieldInfo = FieldInfo.fromfield(field);
		ColumnInfo<Integer> columnInfo = ColumnInfo.fromFieldInfo(field.getAnnotation(CsvColumn.class), fieldInfo,
				IntegerConverter.getSingleton());
		assertFalse(columnInfo.isNeedsQuotes());

		field = MyClass.class.getDeclaredField("defaultValue");
		FieldInfo<String> otherFieldInfo = FieldInfo.fromfield(field);
		ColumnInfo<String> otherColumnInfo = ColumnInfo.fromFieldInfo(field.getAnnotation(CsvColumn.class),
				otherFieldInfo, StringConverter.getSingleton());
		assertTrue(otherColumnInfo.isNeedsQuotes());
	}

	@Test
	public void testMustBeSupplied() throws Exception {
		Field field = MyClass.class.getDeclaredField("trim");
		FieldInfo<Boolean> fieldInfo = FieldInfo.fromfield(field);
		ColumnInfo<Boolean> columnInfo = ColumnInfo.fromFieldInfo(field.getAnnotation(CsvColumn.class), fieldInfo,
				BooleanConverter.getSingleton());
		assertTrue(columnInfo.isMustBeSupplied());

		field = MyClass.class.getDeclaredField("mustBeSupplied");
		FieldInfo<Boolean> otherFieldInfo = FieldInfo.fromfield(field);
		columnInfo = ColumnInfo.fromFieldInfo(field.getAnnotation(CsvColumn.class), otherFieldInfo,
				BooleanConverter.getSingleton());
		assertFalse(columnInfo.isMustBeSupplied());
	}

	@Test
	public void testPosition() throws Exception {
		Field field = MyClass.class.getDeclaredField("trim");
		FieldInfo<Boolean> fieldInfo = FieldInfo.fromfield(field);
		ColumnInfo<Boolean> columnInfo = ColumnInfo.fromFieldInfo(field.getAnnotation(CsvColumn.class), fieldInfo,
				BooleanConverter.getSingleton());
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
		public Void configure(String format, long flags, FieldInfo<String> field) {
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
