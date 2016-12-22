package com.j256.simplecsv.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.j256.simplecsv.converter.StringConverter.ConfigInfo;
import com.j256.simplecsv.processor.ColumnInfo;
import com.j256.simplecsv.processor.ParseError;

public class StringConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		StringConverter converter = StringConverter.getSingleton();
		ConfigInfo configInfo = converter.configure(null, 0, null);
		testConverter(converter, configInfo, "");
		testConverter(converter, configInfo, "one");
		testConverter(converter, configInfo, "two");
	}

	@Test
	public void testBlankNull() throws Exception {
		StringConverter converter = StringConverter.getSingleton();
		ConfigInfo configInfo = converter.configure(null, 0, null);
		ColumnInfo<String> columnInfo = ColumnInfo.forTests(converter, configInfo);

		ParseError parseError = new ParseError();
		assertEquals("", converter.stringToJava("line", 1, 2, columnInfo, "", parseError));
		assertFalse(parseError.isError());

		configInfo = converter.configure(null, StringConverter.BLANK_IS_NULL, null);
		columnInfo = ColumnInfo.forTests(converter, configInfo);
		assertNull(converter.stringToJava("line", 1, 2, columnInfo, "", parseError));
		assertFalse(parseError.isError());

		testConverter(converter, configInfo, null);
	}

	@Test
	public void testTrimOutput() {
		StringConverter converter = StringConverter.getSingleton();
		ConfigInfo configInfo = converter.configure(null, 0, null);
		ColumnInfo<String> columnInfo = ColumnInfo.forTests(converter, configInfo);

		String ok = "ok";
		String spacedOk = " " + ok + " ";
		assertEquals(spacedOk, converter.javaToString(columnInfo, spacedOk));

		configInfo = converter.configure(null, StringConverter.TRIM_OUTPUT, null);
		columnInfo = ColumnInfo.forTests(converter, configInfo);
		assertEquals(ok, converter.javaToString(columnInfo, spacedOk));
	}
}
