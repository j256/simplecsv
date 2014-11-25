package com.j256.simplecsv.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.j256.simplecsv.common.CsvField;
import com.j256.simplecsv.converter.Converter;
import com.j256.simplecsv.processor.ParseError.ErrorType;

public class CsvProcessorTest {

	@Test
	public void testBasic() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		testReadLine(processor, 1, "str", 12321321321321312L, "wqopdkq");
	}

	@Test
	public void testSingleQuotes() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		int intValue = 1;
		String str = "\"";
		long longValue = 2L;
		String unquoted = "u";
		String line = intValue + ",\"" + str + "\"," + longValue + "," + unquoted;
		Basic basic = processor.processRow(line, null);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(str, basic.getString());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquoted());
	}

	@Test
	public void testTwoQuotes() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		int intValue = 1;
		String str = "\"\"";
		long longValue = 2L;
		String unquoted = "u";
		String line = intValue + ",\"" + str + "\"," + longValue + "," + unquoted;
		Basic basic = processor.processRow(line, null);
		assertEquals(intValue, basic.getIntValue());
		// NOTE: this seems to be right
		assertEquals("\"", basic.getString());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquoted());
	}

	@Test
	public void testTwoQuotesPlus() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		int intValue = 1;
		String str = "\"\"wow\"\"";
		long longValue = 2L;
		String unquoted = "u";
		String line = intValue + ",\"" + str + "\"," + longValue + "," + unquoted;
		Basic basic = processor.processRow(line, null);
		assertEquals(intValue, basic.getIntValue());
		// NOTE: this seems to be right
		assertEquals("\"wow\"", basic.getString());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquoted());
	}

	@Test
	public void testPartialLine() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		processor.setAllowPartialLines(true);
		int intValue = 1;
		String str = "\"";
		String line = intValue + ",\"" + str + "\"";
		Basic basic = processor.processRow(line, null);
		assertEquals(intValue, basic.getIntValue());
		// NOTE: this seems to be right
		assertEquals("\"", basic.getString());
	}

	@Test(expected = ParseException.class)
	public void testNotEnoughCells() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		processor.processRow("1,2", null);
	}

	@Test
	public void testOutput() {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		int intValue = 100;
		String str = "strwow";
		long longValue = 341442323234552L;
		String unquoted = "fewpofjwe";
		Basic basic = new Basic(intValue, str, longValue, unquoted);
		String line = processor.buildLine(basic, false);
		assertEquals(intValue + ",\"" + str + "\"," + longValue + "," + unquoted, line);
	}

	@Test
	public void testQuotedStringOutput() {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		int intValue = 200;
		String beforeQuote = "str";
		String afterQuote = "wow";
		long longValue = 3452L;
		String unquoted = "fewdqwpofjwe";
		Basic basic = new Basic(intValue, beforeQuote + "\"" + afterQuote, longValue, unquoted);
		String line = processor.buildLine(basic, false);
		assertEquals(intValue + ",\"" + beforeQuote + "\"\"" + afterQuote + "\"," + longValue + "," + unquoted, line);
	}

	@Test
	public void testSeparatorStringOutput() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		int intValue = 200;
		String str = "has,comma";
		long longValue = 3452L;
		String unquoted = "u,q";
		Basic basic = new Basic(intValue, str, longValue, unquoted);
		String written = processor.buildLine(basic, false);
		basic = processor.processRow(written, null);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(str, basic.getString());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquoted());
	}

	@Test
	public void testHeader() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		String intColumn = "int";
		String strColumn = "string here";
		String longColumn = "long";
		String unquotedColumn = "unquoted stuff";
		String header = intColumn + ",\"" + strColumn + "\"," + longColumn + "," + unquotedColumn;
		String[] columnNames = processor.processHeader(header, null);
		assertEquals(4, columnNames.length);
		assertEquals(intColumn, columnNames[0]);
		assertEquals(strColumn, columnNames[1]);
		assertEquals(longColumn, columnNames[2]);
		assertEquals(unquotedColumn, columnNames[3]);
	}

	@Test
	public void testWriteReadFile() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		int intValue = 200;
		String str = "has,comma";
		long longValue = 3452L;
		String unquoted = "u,q";
		Basic basic = new Basic(intValue, str, longValue, unquoted);

		File file = new File("target/" + getClass().getSimpleName());
		file.delete();
		processor.writeAll(file, Collections.singletonList(basic), true);

		List<Basic> entities = processor.readAll(file, true, true, null);
		assertNotNull(entities);
		assertEquals(1, entities.size());
		basic = entities.get(0);

		assertEquals(intValue, basic.getIntValue());
		assertEquals(str, basic.getString());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquoted());
	}

	@Test(expected = ParseException.class)
	public void testReadNoHeaderFile() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		StringReader reader = new StringReader("");
		List<Basic> entities = processor.readAll(reader, true, true, null);
		assertNull(entities);
	}

	@Test
	public void testReadBadHeaderFile() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		StringReader reader = new StringReader("");
		List<ParseError> parseErrors = new ArrayList<ParseError>();
		List<Basic> entities = processor.readAll(reader, true, true, parseErrors);
		assertNull(entities);
		assertEquals(1, parseErrors.size());

		reader = new StringReader("");
		List<ParseError> errors = new ArrayList<ParseError>();
		entities = processor.readAll(reader, true, true, errors);
		assertNull(entities);
		assertEquals(1, errors.size());
		assertEquals(ErrorType.NO_HEADER, errors.get(0).getErrorType());

		reader = new StringReader("bad header\n");
		errors.clear();
		entities = processor.readAll(reader, true, true, errors);
		assertNull(entities);
		assertEquals(1, errors.size());
		assertEquals(ErrorType.INVALID_HEADER, errors.get(0).getErrorType());
	}

	@Test
	public void testNulls() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		Basic basic = new Basic(1, null, 2, null);
		StringWriter writer = new StringWriter();
		BufferedWriter bufferedWriter = new BufferedWriter(writer);
		processor.writeRow(bufferedWriter, basic, false);
		bufferedWriter.flush();
		assertEquals("1,\"\",2,", writer.toString());
	}

	private void testReadLine(CsvProcessor<Basic> processor, int intValue, String str, long longValue, String unquoted)
			throws ParseException {
		String line = intValue + ",\"" + str + "\"," + longValue + "," + unquoted;
		Basic basic = processor.processRow(line, null);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(str, basic.getString());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquoted());

		String written = processor.buildLine(basic, false);
		basic = processor.processRow(written, null);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(str, basic.getString());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquoted());
	}

	private static class Basic {
		@CsvField
		private int intValue;
		@CsvField
		private String string;
		@CsvField
		private long longValue;
		@CsvField(converterClass = UnquotedStringConverter.class)
		private String unquoted;

		@SuppressWarnings("unused")
		public Basic() {
			// for simplecsv
		}

		public Basic(int intValue, String string, long longValue, String specialString) {
			this.intValue = intValue;
			this.string = string;
			this.longValue = longValue;
			this.unquoted = specialString;
		}

		public int getIntValue() {
			return intValue;
		}

		public String getString() {
			return string;
		}

		public long getLongValue() {
			return longValue;
		}

		public String getUnquoted() {
			return unquoted;
		}
	}

	public static class UnquotedStringConverter implements Converter<String, Void> {
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
