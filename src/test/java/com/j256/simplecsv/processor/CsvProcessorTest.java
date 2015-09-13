package com.j256.simplecsv.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

import com.j256.simplecsv.common.CsvField;
import com.j256.simplecsv.converter.Converter;
import com.j256.simplecsv.converter.IntegerConverter;
import com.j256.simplecsv.processor.ParseError.ErrorType;

public class CsvProcessorTest {

	private static final String QUOTE_IN_HEADER_BEFORE = "has";
	private static final String QUOTE_IN_HEADER_AFTER = "quote";
	private static final String QUOTE_IN_HEADER = QUOTE_IN_HEADER_BEFORE + '\"' + QUOTE_IN_HEADER_AFTER;
	private static final String QUOTE_IN_HEADER_QUOTED = QUOTE_IN_HEADER_BEFORE + "\"\"" + QUOTE_IN_HEADER_AFTER;

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
		assertEquals(str, basic.getStringValue());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquotedValue());
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
		assertEquals("\"", basic.getStringValue());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquotedValue());
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
		assertEquals("\"wow\"", basic.getStringValue());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquotedValue());
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
		assertEquals("\"", basic.getStringValue());
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
		assertEquals(str, basic.getStringValue());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquotedValue());
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

		List<Basic> entities = processor.readAll(file, null);
		assertNotNull(entities);
		assertEquals(1, entities.size());
		basic = entities.get(0);

		assertEquals(intValue, basic.getIntValue());
		assertEquals(str, basic.getStringValue());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquotedValue());
	}

	@Test(expected = ParseException.class)
	public void testReadNoHeaderFile() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		StringReader reader = new StringReader("");
		List<Basic> entities = processor.readAll(reader, null);
		assertNull(entities);
	}

	@Test
	public void testReadBadHeaderFile() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		StringReader reader = new StringReader("");
		List<ParseError> parseErrors = new ArrayList<ParseError>();
		List<Basic> entities = processor.readAll(reader, parseErrors);
		assertNull(entities);
		assertEquals(1, parseErrors.size());

		reader = new StringReader("");
		List<ParseError> errors = new ArrayList<ParseError>();
		entities = processor.readAll(reader, errors);
		assertNull(entities);
		assertEquals(1, errors.size());
		assertEquals(ErrorType.NO_HEADER, errors.get(0).getErrorType());

		reader = new StringReader("bad header\n");
		errors.clear();
		entities = processor.readAll(reader, errors);
		assertNull(entities);
		assertEquals(1, errors.size());
		assertEquals(ErrorType.INVALID_HEADER, errors.get(0).getErrorType());
	}

	@Test
	public void testInvalidHeaderOrder() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		StringReader reader = new StringReader("string,intValue,longValue,unquoted");
		List<ParseError> parseErrors = new ArrayList<ParseError>();
		List<Basic> entities = processor.readAll(reader, parseErrors);
		assertNull(entities);
		assertEquals(1, parseErrors.size());
		assertEquals(ErrorType.INVALID_HEADER, parseErrors.get(0).getErrorType());

		processor.withFlexibleOrder(true);
		reader = new StringReader("string,intValue,longValue,unquoted");
		parseErrors.clear();
		entities = processor.readAll(reader, parseErrors);
		assertEquals(0, parseErrors.size());
		assertNotNull(entities);
		assertEquals(0, entities.size());
	}

	@Test
	public void testOptionalHeaders() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		StringReader reader = new StringReader("intValue,string,longValue");
		List<ParseError> parseErrors = new ArrayList<ParseError>();
		List<Basic> entities = processor.readAll(reader, parseErrors);
		assertNotNull(entities);
		assertEquals(0, entities.size());
		assertEquals(0, parseErrors.size());
	}

	@Test
	public void testFlexibleOrderEntities() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		processor.withFlexibleOrder(true);
		String strValue = "fjeofjewf";
		int intValue = 123131;
		long longValue = 123213213123L;
		String unquotedValue = "fewopjfpewfjw";

		// different order #1
		StringReader reader = new StringReader("string,intValue,longValue,unquoted\n" //
				+ strValue + "," + intValue + "," + longValue + "," + unquotedValue + "\n");
		List<ParseError> parseErrors = new ArrayList<ParseError>();
		List<Basic> entities = processor.readAll(reader, parseErrors);
		assertEquals(0, parseErrors.size());
		assertEquals(1, entities.size());
		Basic basic = entities.get(0);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(strValue, basic.getStringValue());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquotedValue, basic.getUnquotedValue());

		// different order #2
		reader = new StringReader("longValue,unquoted,intValue,string\n" //
				+ longValue + "," + unquotedValue + "," + intValue + "," + strValue + "\n");
		parseErrors.clear();
		entities = processor.readAll(reader, parseErrors);
		assertEquals(0, parseErrors.size());
		assertEquals(1, entities.size());
		basic = entities.get(0);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(strValue, basic.getStringValue());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquotedValue, basic.getUnquotedValue());
	}

	@Test
	public void testColumnNameMatcher() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		processor.withColumnNameMatcher(new ColumnNameMatcher() {
			@Override
			public boolean matchesColumnName(String definitionName, String csvName) {
				return csvName.startsWith(definitionName);
			}
		});
		String strValue = "fjeofjewf";
		int intValue = 123131;
		long longValue = 123213213123L;
		String unquotedValue = "fewopjfpewfjw";

		// column names with suffixes
		StringReader reader = new StringReader("intValue*,string1,longValue2,unquoted\n" //
				+ intValue + "," + strValue + "," + longValue + "," + unquotedValue + "\n");
		List<ParseError> parseErrors = new ArrayList<ParseError>();
		List<Basic> entities = processor.readAll(reader, parseErrors);
		assertEquals(0, parseErrors.size());
		assertEquals(1, entities.size());
		Basic basic = entities.get(0);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(strValue, basic.getStringValue());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquotedValue, basic.getUnquotedValue());
	}

	@Test
	public void testIgnoreUnknownColumns() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		processor.withIgnoreUnknownColumns(true);
		String strValue = "fjeofjewf";
		int intValue = 123131;
		long longValue = 123213213123L;
		String unquotedValue = "fewopjfpewfjw";

		// unknown field at the end
		StringReader reader = new StringReader("intValue,string,longValue,unquoted,unknown\n" //
				+ intValue + "," + strValue + "," + longValue + "," + unquotedValue + ",unknownValue\n");
		List<ParseError> parseErrors = new ArrayList<ParseError>();
		List<Basic> entities = processor.readAll(reader, parseErrors);
		assertEquals(0, parseErrors.size());
		assertEquals(1, entities.size());
		Basic basic = entities.get(0);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(strValue, basic.getStringValue());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquotedValue, basic.getUnquotedValue());

		// unknown field in the middle
		reader = new StringReader("intValue,string,unknown,longValue,unquoted\n" //
				+ intValue + "," + strValue + ",unknownValue," + longValue + "," + unquotedValue + "\n");
		parseErrors = new ArrayList<ParseError>();
		entities = processor.readAll(reader, parseErrors);
		assertEquals(0, parseErrors.size());
		assertEquals(1, entities.size());
		basic = entities.get(0);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(strValue, basic.getStringValue());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquotedValue, basic.getUnquotedValue());
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

	@Test
	public void testSubClass() throws Exception {
		CsvProcessor<BasicSubclass> processor = new CsvProcessor<BasicSubclass>(BasicSubclass.class);
		int intValue = 1;
		String str = "\"";
		long longValue = 2L;
		String unquoted = "u";
		String line = intValue + ",\"" + str + "\"," + longValue + "," + unquoted;
		BasicSubclass basicSub = processor.processRow(line, null);
		assertEquals(intValue, basicSub.getIntValue());
		assertEquals(str, basicSub.getStringValue());
		assertEquals(longValue, basicSub.getLongValue());
		assertEquals(unquoted, basicSub.getUnquotedValue());
	}

	@Test
	public void testSubClassDupField() throws Exception {
		CsvProcessor<BasicSubclassDupField> processor =
				new CsvProcessor<BasicSubclassDupField>(BasicSubclassDupField.class);
		int intValue = 1;
		String str = "\"";
		long longValue = 2L;
		String unquoted = "u";
		String line = intValue + ",\"" + str + "\"," + longValue + "," + unquoted;
		BasicSubclassDupField basicSub = processor.processRow(line, null);
		assertEquals(0, basicSub.getIntValue());
		assertEquals(intValue, basicSub.intValue);
		assertEquals(str, basicSub.getStringValue());
		assertEquals(longValue, basicSub.getLongValue());
		assertEquals(unquoted, basicSub.getUnquotedValue());
	}

	@Test
	public void testRegisterConverter() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		processor.registerConverter(int.class, new IntPlusOneConverter());
		int intValue = 1;
		String str = "\"";
		long longValue = 2L;
		String unquoted = "u";
		String line = intValue + ",\"" + str + "\"," + longValue + "," + unquoted;
		Basic basic = processor.processRow(line, null);
		// int value gets +1 in the [weird] converter
		assertEquals(intValue + 1, basic.getIntValue());
		assertEquals(str, basic.getStringValue());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquotedValue());

		processor = new CsvProcessor<Basic>(Basic.class);
		processor.registerConverter(int.class, new IntPlusOneConverter());
		// override it
		processor.withConverter(int.class, new IntegerConverter());
		basic = processor.processRow(line, null);
		assertEquals(intValue, basic.getIntValue());
	}

	@Test
	public void testDefaultValue() throws Exception {
		CsvProcessor<DefaultValue> processor = new CsvProcessor<DefaultValue>(DefaultValue.class);
		DefaultValue defaultValue = processor.processRow("", null);
		assertEquals(Integer.parseInt(DefaultValue.DEFAULT_VALUE), defaultValue.value);
	}

	@Test
	public void testBlankLastField() throws Exception {
		CsvProcessor<DefaultValue> processor = new CsvProcessor<DefaultValue>(DefaultValue.class);
		DefaultValue defaultValue = processor.processRow("", null);
		assertEquals(Integer.parseInt(DefaultValue.DEFAULT_VALUE), defaultValue.value);
	}

	@Test
	public void testCallableConstructor() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		Basic basic = processor.processRow(",str,,", null);
		// initially it is 0
		assertEquals(0, basic.intValue);
		processor = new CsvProcessor<Basic>(Basic.class);
		final int value = 123213;
		processor.setConstructorCallable(new Callable<CsvProcessorTest.Basic>() {
			@Override
			public Basic call() {
				Basic basic = new Basic();
				basic.intValue = value;
				return basic;
			}
		});
		basic = processor.processRow(",str,,", null);
		assertEquals(value, basic.intValue);

		processor = new CsvProcessor<Basic>(Basic.class);
		processor.setConstructorCallable(new Callable<CsvProcessorTest.Basic>() {
			@Override
			public Basic call() {
				return null;
			}
		});
		// make sure this resets the callable is reset
		processor.withConstructorCallable(null);
		basic = processor.processRow(",str,,", null);
		assertEquals(0, basic.intValue);
	}

	@Test(expected = ParseException.class)
	public void testCallableConstructorThrows() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		processor.setConstructorCallable(new Callable<CsvProcessorTest.Basic>() {
			@Override
			public Basic call() {
				throw new IllegalStateException("expected");
			}
		});
		processor.processRow(",,,", null);
	}

	@Test(expected = IllegalStateException.class)
	public void testNoClass() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>();
		processor.processRow("", null);
	}

	@Test(expected = ParseException.class)
	public void testMustBeSupplied() throws Exception {
		CsvProcessor<MustNotBeBlank> processor = new CsvProcessor<MustNotBeBlank>(MustNotBeBlank.class);
		processor.processRow("", null);
	}

	@Test(expected = ParseException.class)
	public void testInvalidMidFieldQuote() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		processor.processRow("0,\"str\"ing\",1,unquoted", null);
	}

	@Test(expected = ParseException.class)
	public void testNoEndQuote() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		processor.processRow("0,\"string,1,unquoted", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoFields() {
		CsvProcessor<Object> processor = new CsvProcessor<Object>(Object.class);
		processor.initialize();
	}

	@Test(expected = IllegalStateException.class)
	public void testNoConstructor() {
		CsvProcessor<NoConstructor> processor = new CsvProcessor<NoConstructor>(NoConstructor.class);
		processor.initialize();
	}

	@Test(expected = ParseException.class)
	public void testInvalidStringValue() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		processor.withConverter(int.class, new IntThrowsConverter());
		processor.processRow("notint,string,1,unquoted", null);
	}

	@Test(expected = ParseException.class)
	public void testConverterThrows() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		processor.withConverter(int.class, new IntThrowsConverter());
		processor.processRow("0,string,1,unquoted", null);
	}

	@Test
	public void testCoverage() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>();
		processor.setEntityClass(Basic.class);
		processor.withEntityClass(Basic.class);
		processor.setAllowPartialLines(true);
		processor.withAllowPartialLines(true);
		processor.setAlwaysTrimInput(true);
		processor.withAlwaysTrimInput(true);
		char quote = '\'';
		processor.setColumnQuote(quote);
		processor.withColumnQuote(quote);
		char sep = '|';
		processor.setColumnSeparator(sep);
		processor.withColumnSeparator(sep);
		String lineTerm = "\r\n";
		processor.setLineTermination(lineTerm);
		processor.withLineTermination(lineTerm);
		processor.initialize();
		int intValue = 1;
		String str = "\"";
		long longValue = 2L;
		String unquoted = "u";
		String line = "" + intValue + sep + quote + str + quote + sep + longValue + sep + unquoted + lineTerm;
		Basic basic = processor.processRow(line, null);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(str, basic.getStringValue());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquotedValue());
	}

	@Test
	public void testErrors() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);

		// column names with suffixes
		StringReader reader = new StringReader("intValue,string,bad,unquoted\n" //
				+ 1 + "," + "str" + "," + 2 + "," + "unq" + "\n" //
				+ "notint" + "," + "str" + "," + 2 + "," + "unq" + "\n" //
				+ 1 + "," + "str" + "," + "notlong" + "," + "unq" + "\n" //
		);
		List<ParseError> parseErrors = new ArrayList<ParseError>();
		processor.readAll(reader, parseErrors);
		assertEquals(1, parseErrors.size());
		ParseError error = parseErrors.get(0);
		assertEquals(1, error.getLineNumber());
		assertEquals(ErrorType.INVALID_HEADER, error.getErrorType());

		// column names with suffixes
		reader = new StringReader("intValue,string,longValue,unquoted\n" //
				+ 1 + "," + "str" + "," + 2 + "," + "unq" + "\n" //
				+ "notint" + "," + "str" + "," + 2 + "," + "unq" + "\n" //
				+ 1 + "," + "" + "," + "notlong" + "," + "unq" + "\n" //
		);
		parseErrors = new ArrayList<ParseError>();
		processor.readAll(reader, parseErrors);
		assertEquals(2, parseErrors.size());
		error = parseErrors.get(0);
		assertEquals(3, error.getLineNumber());
		assertEquals(0, error.getLinePos());
		assertEquals(ErrorType.INVALID_FORMAT, error.getErrorType());
		error = parseErrors.get(1);
		assertEquals(4, error.getLineNumber());
		assertEquals(2, error.getLinePos());
		assertEquals(ErrorType.MUST_NOT_BE_BLANK, error.getErrorType());
	}

	@Test
	public void testBadHeader() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		StringReader reader = new StringReader("\"bad header\n");
		ParseError parseError = new ParseError();
		assertNull(processor.readHeader(new BufferedReader(reader), parseError));
		assertTrue(parseError.isError());

		processor.withHeaderValidation(false);
		reader = new StringReader("bad header\n");
		assertNotNull(processor.readHeader(new BufferedReader(reader), parseError));
	}

	@Test
	public void testBlankHeader() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		StringReader reader = new StringReader("\"\n");
		ParseError parseError = new ParseError();
		assertNull(processor.readHeader(new BufferedReader(reader), parseError));
		assertTrue(parseError.isError());
	}

	@Test(expected = ParseException.class)
	public void testBadQuotedHeaderThrows() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		StringReader reader = new StringReader("\"bad header\n");
		assertNull(processor.readHeader(new BufferedReader(reader), null));
	}

	@Test(expected = ParseException.class)
	public void testBadHeaderThrows() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		StringReader reader = new StringReader("bad header\n");
		assertNull(processor.readHeader(new BufferedReader(reader), null));
	}

	@Test
	public void testValidateHeaderString() throws Exception {
		String header = "intValue,string,bad,unquoted";
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		processor.validateHeader(header, null);
	}

	@Test
	public void testValidateHeaderColumns() {
		String[] headers = new String[] { "intValue", "string", "bad", "unquoted" };
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		processor.validateHeaderColumns(headers, null);
	}

	@Test
	public void testNoHeader() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		processor.withFirstLineHeader(false);

		String strValue = "fjeofjewf";
		int intValue = 123131;
		long longValue = 123213213123L;
		String unquotedValue = "fewopjfpewfjw";

		// unknown field at the end
		StringReader reader =
				new StringReader(intValue + "," + strValue + "," + longValue + "," + unquotedValue + ",unknownValue\n");
		List<Basic> entities = processor.readAll(reader, null);
		assertEquals(1, entities.size());
		Basic basic = entities.get(0);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(strValue, basic.getStringValue());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquotedValue, basic.getUnquotedValue());
	}

	@Test
	public void testDontWriteHeader() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		processor.withFirstLineHeader(false);

		String strValue = "fjeofjewf";
		int intValue = 123131;
		long longValue = 123213213123L;
		String unquotedValue = "fewopjfpewfjw";
		Basic basic = new Basic();
		basic.intValue = intValue;
		basic.string = strValue;
		basic.longValue = longValue;
		basic.unquoted = unquotedValue;

		StringWriter writer = new StringWriter();
		processor.writeAll(writer, Collections.singletonList(basic), false);

		String expectedLine = intValue + ",\"" + strValue + "\"," + longValue + "," + unquotedValue + "\n";
		String line = writer.toString();
		assertEquals(expectedLine, line);
	}

	@Test
	public void testQuoteInHeader() throws Exception {
		CsvProcessor<QuoteInColumnHeader> processor = new CsvProcessor<QuoteInColumnHeader>(QuoteInColumnHeader.class);

		StringWriter writer = new StringWriter();
		processor.writeAll(writer, Collections.<QuoteInColumnHeader> emptyList(), true);

		String line = writer.toString();
		assertEquals("\"" + QUOTE_IN_HEADER_QUOTED + "\"\n", line);
	}

	/* ================================================================================================= */

	private void testReadLine(CsvProcessor<Basic> processor, int intValue, String str, long longValue, String unquoted)
			throws ParseException {
		String line = intValue + ",\"" + str + "\"," + longValue + "," + unquoted;
		Basic basic = processor.processRow(line, null);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(str, basic.getStringValue());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquotedValue());

		String written = processor.buildLine(basic, false);
		basic = processor.processRow(written, null);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(str, basic.getStringValue());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquotedValue());
	}

	/* ================================================================================================= */

	private static class Basic {
		@CsvField
		private int intValue;
		@CsvField(mustNotBeBlank = true)
		private String string;
		@CsvField
		private long longValue;
		@CsvField(converterClass = UnquotedStringConverter.class, mustBeSupplied = false)
		private String unquoted;

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

		public String getStringValue() {
			return string;
		}

		public long getLongValue() {
			return longValue;
		}

		public String getUnquotedValue() {
			return unquoted;
		}
	}

	private static class BasicSubclass extends Basic {
		@SuppressWarnings("unused")
		public BasicSubclass() {
			// for simplecsv
		}
	}

	private static class BasicSubclassDupField extends Basic {
		@CsvField
		private int intValue;
		@SuppressWarnings("unused")
		public BasicSubclassDupField() {
			// for simplecsv
		}
	}

	private static class DefaultValue {
		public static final String DEFAULT_VALUE = "1";
		@CsvField(defaultValue = DEFAULT_VALUE)
		private int value;
		@SuppressWarnings("unused")
		public DefaultValue() {
			// for simplecsv
		}
	}

	private static class MustNotBeBlank {
		@CsvField(mustNotBeBlank = true)
		private int value;
		@SuppressWarnings("unused")
		public MustNotBeBlank() {
			// for simplecsv
		}
	}

	private static class NoConstructor {
		@CsvField
		private int value;
	}

	private static class QuoteInColumnHeader {
		@CsvField(columnName = QUOTE_IN_HEADER)
		private int value;
		@SuppressWarnings("unused")
		public QuoteInColumnHeader() {
			// for simplecsv
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

	public static class IntPlusOneConverter implements Converter<Integer, Void> {
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
		public String javaToString(ColumnInfo columnInfo, Integer value) {
			return value.toString();
		}
		@Override
		public Integer stringToJava(String line, int lineNumber, ColumnInfo columnInfo, String value,
				ParseError parseError) {
			return Integer.parseInt(value) + 1;
		}
	}

	public static class IntThrowsConverter implements Converter<Integer, Void> {
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
		public String javaToString(ColumnInfo columnInfo, Integer value) {
			return value.toString();
		}
		@Override
		public Integer stringToJava(String line, int lineNumber, ColumnInfo columnInfo, String value,
				ParseError parseError) throws ParseException {
			// this could throw a runtime exception
			Integer.parseInt(value);
			throw new ParseException("value should be an invalid int", 0);
		}
	}
}
