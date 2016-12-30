package com.j256.simplecsv.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.j256.simplecsv.processor.ColumnInfo;
import com.j256.simplecsv.processor.ParseError;

public class StringConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		StringConverter converter = StringConverter.getSingleton();
		testConverter(converter, String.class, null, 0, "");
		testConverter(converter, String.class, null, 0, "one");
		testConverter(converter, String.class, null, 0, "two");
	}

	@Test
	public void testBlankNull() throws Exception {
		StringConverter converter = StringConverter.getSingleton();
		ColumnInfo<String> columnInfo = ColumnInfo.forTests(converter, String.class, null, 0);

		ParseError parseError = new ParseError();
		assertEquals("", converter.stringToJava("line", 1, 2, columnInfo, "", parseError));
		assertFalse(parseError.isError());

		columnInfo = ColumnInfo.forTests(converter, String.class, null, StringConverter.BLANK_IS_NULL);
		assertNull(converter.stringToJava("line", 1, 2, columnInfo, "", parseError));
		assertFalse(parseError.isError());

		testConverter(converter, String.class, null, StringConverter.BLANK_IS_NULL, null);
	}

	@Test
	public void testTrimOutput() {
		StringConverter converter = StringConverter.getSingleton();
		ColumnInfo<String> columnInfo = ColumnInfo.forTests(converter, String.class, null, 0);

		String ok = "ok";
		String spacedOk = " " + ok + " ";
		assertEquals(spacedOk, converter.javaToString(columnInfo, spacedOk));

		columnInfo = ColumnInfo.forTests(converter, String.class, null, StringConverter.TRIM_OUTPUT);
		assertEquals(ok, converter.javaToString(columnInfo, spacedOk));
	}
}
