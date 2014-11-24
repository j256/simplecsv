package com.j256.simplecsv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.Test;

import com.j256.simplecsv.converter.IntegerConverter;

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
		assertTrue(fieldInfo.isAllowNull());
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
	}
}
