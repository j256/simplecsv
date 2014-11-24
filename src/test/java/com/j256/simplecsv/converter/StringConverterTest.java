package com.j256.simplecsv.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.j256.simplecsv.converter.StringConverter.ConfigInfo;
import com.j256.simplecsv.processor.FieldInfo;
import com.j256.simplecsv.processor.ParseError;

public class StringConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		StringConverter converter = new StringConverter();
		ConfigInfo configInfo = converter.configure(null, 0, null);
		testConverter(converter, configInfo, "");
		testConverter(converter, configInfo, "one");
		testConverter(converter, configInfo, "two");
	}

	@Test
	public void testBlankNull() throws Exception {
		StringConverter converter = new StringConverter();
		ConfigInfo configInfo = converter.configure(null, 0, null);
		FieldInfo fieldInfo = FieldInfo.forTests(converter, configInfo);

		ParseError parseError = new ParseError();
		assertEquals("", converter.stringToJava("line", 1, fieldInfo, "", parseError));
		assertFalse(parseError.isError());

		configInfo = converter.configure(null, StringConverter.BLANK_IS_NULL, null);
		fieldInfo = FieldInfo.forTests(converter, configInfo);
		assertNull(converter.stringToJava("line", 1, fieldInfo, "", parseError));
		assertFalse(parseError.isError());

		testConverter(converter, configInfo, null);
	}

	@Test
	public void testTrimOutput() {
		StringConverter converter = new StringConverter();
		ConfigInfo configInfo = converter.configure(null, 0, null);
		FieldInfo fieldInfo = FieldInfo.forTests(converter, configInfo);

		String ok = "ok";
		String spacedOk = " " + ok + " ";
		assertEquals(spacedOk, converter.javaToString(fieldInfo, spacedOk));

		configInfo = converter.configure(null, StringConverter.TRIM_OUTPUT, null);
		fieldInfo = FieldInfo.forTests(converter, configInfo);
		assertEquals(ok, converter.javaToString(fieldInfo, spacedOk));
	}
}
