package com.j256.simplecsv.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

import com.j256.simplecsv.common.CsvColumn;
import com.j256.simplecsv.converter.BooleanConverter;
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
		boolean bool = true;
		Basic basic = new Basic(intValue, str, longValue, unquoted, bool);
		String line = processor.buildLine(basic, false);
		assertEquals(intValue + ",\"" + str + "\"," + longValue + "," + unquoted + "," + bool, line);
	}

	@Test
	public void testQuotedStringOutput() {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		int intValue = 200;
		String beforeQuote = "str";
		String afterQuote = "wow";
		long longValue = 3452L;
		String unquoted = "fewdqwpofjwe";
		boolean bool = false;
		Basic basic = new Basic(intValue, beforeQuote + "\"" + afterQuote, longValue, unquoted, bool);
		String line = processor.buildLine(basic, false);
		assertEquals(
				intValue + ",\"" + beforeQuote + "\"\"" + afterQuote + "\"," + longValue + "," + unquoted + "," + bool,
				line);
	}

	@Test
	public void testSeparatorStringOutput() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		int intValue = 200;
		String str = "has,comma";
		long longValue = 3452L;
		String unquoted = "u,q";
		boolean bool = true;
		Basic basic = new Basic(intValue, str, longValue, unquoted, true);
		String written = processor.buildLine(basic, false);
		basic = processor.processRow(written, null);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(str, basic.getStringValue());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquotedValue());
		assertEquals(bool, basic.isBool());
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
		boolean bool = false;
		Basic basic = new Basic(intValue, str, longValue, unquoted, bool);

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
		assertEquals(bool, basic.isBool());
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
		reader = new StringReader("string,intValue,longValue,unquoted,bool");
		parseErrors.clear();
		entities = processor.readAll(reader, parseErrors);
		assertEquals(0, parseErrors.size());
		assertNotNull(entities);
		assertEquals(0, entities.size());
	}

	@Test
	public void testOptionalHeaders() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		StringReader reader = new StringReader("intValue,string,longValue,bool");
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
		boolean bool = true;

		// different order #1
		StringReader reader = new StringReader("string,intValue,longValue,unquoted,bool\n" //
				+ strValue + "," + intValue + "," + longValue + "," + unquotedValue + "," + bool + "\n");
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
		reader = new StringReader("longValue,unquoted,intValue,string,bool\n" //
				+ longValue + "," + unquotedValue + "," + intValue + "," + strValue + "," + bool + "\n");
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
		boolean bool = false;

		// column names with suffixes
		StringReader reader = new StringReader("intValue*,string1,longValue2,unquoted,bool\n" //
				+ intValue + "," + strValue + "," + longValue + "," + unquotedValue + "," + bool + "\n");
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
		boolean bool = true;

		// unknown field at the end
		StringReader reader = new StringReader("intValue,string,longValue,unquoted,bool,unknown\n" //
				+ intValue + "," + strValue + "," + longValue + "," + unquotedValue + "," + bool + ",unknownValue\n");
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
		reader = new StringReader("intValue,string,unknown,longValue,unquoted,bool\n" //
				+ intValue + "," + strValue + ",unknownValue," + longValue + "," + unquotedValue + "," + bool + "\n");
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
		Basic basic = new Basic(1, null, 2, null, false);
		StringWriter writer = new StringWriter();
		BufferedWriter bufferedWriter = new BufferedWriter(writer);
		processor.writeRow(bufferedWriter, basic, false);
		bufferedWriter.flush();
		assertEquals("1,\"\",2,,false", writer.toString());
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
		Basic basic = processor.processRow(",str,,,", null);
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
		basic = processor.processRow(",str,,,", null);
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
		basic = processor.processRow(",str,,,", null);
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
		StringReader reader = new StringReader("intValue,string,bad,unquoted,bool\n" //
				+ 1 + "," + "str" + "," + 2 + "," + "unq" + "," + "true" + "\n" //
				+ "notint" + "," + "str" + "," + 2 + "," + "unq" + "," + "true" + "\n" //
				+ 1 + "," + "str" + "," + "notlong" + "," + "unq" + "," + "true" + "\n" //
		);
		List<ParseError> parseErrors = new ArrayList<ParseError>();
		processor.readAll(reader, parseErrors);
		assertEquals(1, parseErrors.size());
		ParseError error = parseErrors.get(0);
		assertEquals(1, error.getLineNumber());
		assertEquals(ErrorType.INVALID_HEADER, error.getErrorType());

		// column names with suffixes
		reader = new StringReader("intValue,string,longValue,unquoted,bool\n" //
				+ 1 + "," + "str" + "," + 2 + "," + "unq" + "," + "true" + "\n" //
				+ "notint" + "," + "str" + "," + 2 + "," + "unq" + "," + "true" + "\n" //
				+ 1 + "," + "" + "," + "notlong" + "," + "unq" + "," + "true" + "\n" //
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
	public void testBooleanLinePos() throws Exception {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);

		// column names with suffixes
		StringReader reader = new StringReader("intValue,string,longValue,unquoted,bool\n" //
				+ 1 + "," + "str" + "," + 2 + "," + "unq" + "," + "somestrangevalue" + "\n" //
		);
		List<ParseError> parseErrors = new ArrayList<ParseError>();
		processor.readAll(reader, parseErrors);
		assertEquals(1, parseErrors.size());
		ParseError error = parseErrors.get(0);
		assertEquals(ErrorType.INVALID_FORMAT, error.getErrorType());
		assertEquals(2, error.getLineNumber());
		assertEquals(12, error.getLinePos());
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
		boolean bool = true;

		StringReader reader =
				new StringReader(intValue + "," + strValue + "," + longValue + "," + unquotedValue + "," + bool + "\n");
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
		boolean bool = true;

		Basic basic = new Basic();
		basic.intValue = intValue;
		basic.string = strValue;
		basic.longValue = longValue;
		basic.unquoted = unquotedValue;
		basic.bool = bool;

		StringWriter writer = new StringWriter();
		processor.writeAll(writer, Collections.singletonList(basic), false);

		String expectedLine = intValue + ",\"" + strValue + "\"," + longValue + "," + unquotedValue + "," + bool + "\n";
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

	@Test(expected = ParseException.class)
	public void testTruncatedLine() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		String line = "1,\"hello\",2\n";
		processor.processRow(line, null);
	}

	@Test
	public void testTruncatedLineError() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		String line = "1,\"hello\",2\n";
		ParseError error = new ParseError();
		assertNull(processor.processRow(line, error));
		assertTrue(error.isError());
		assertTrue(error.getMessage(), error.getMessage().startsWith("Line does not have"));
	}

	@Test(expected = ParseException.class)
	public void testExtraStuff() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		String line = "1,\"hello\",2,wow,true,extra\n";
		processor.processRow(line, null);
	}

	@Test
	public void testExtraStuffError() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		String line = "1,\"hello\",2,wow,true,extra\n";
		ParseError error = new ParseError();
		assertNull(processor.processRow(line, error));
		assertTrue(error.isError());
		assertTrue(error.getMessage(), error.getMessage().startsWith("Line has extra information"));
	}

	@Test
	public void testGetSetMethods() throws ParseException, IOException {
		CsvProcessor<GetSetMethod> processor = new CsvProcessor<GetSetMethod>(GetSetMethod.class);
		int value = 1312321321;
		String line = value + "\n";
		ParseError error = new ParseError();
		GetSetMethod getSetMethod = processor.processRow(line, error);
		assertEquals(value, getSetMethod.value);
		StringWriter writer = new StringWriter();
		processor.writeAll(writer, Collections.singletonList(getSetMethod), false);
		assertEquals("\"value\"", processor.buildHeaderLine(false));
		assertEquals(line, writer.toString());
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingSetMethod() {
		new CsvProcessor<MissingSetMethod>(MissingSetMethod.class).initialize();
	}

	@Test(expected = IllegalStateException.class)
	public void testGetMethodReturnsVoid() {
		new CsvProcessor<GetMethodReturnsVoid>(GetMethodReturnsVoid.class).initialize();
	}

	@Test(expected = IllegalStateException.class)
	public void testSetMethodWrongNumParams() {
		new CsvProcessor<SetMethodWrongNumParams>(SetMethodWrongNumParams.class).initialize();
	}

	@Test(expected = IllegalStateException.class)
	public void testGetSetMethodTypeNoMatch() {
		new CsvProcessor<GetSetMethodTypeNoMatch>(GetSetMethodTypeNoMatch.class).initialize();
	}

	@Test
	public void testAfterColumn() {
		CsvProcessor<AfterColumn> processor = new CsvProcessor<AfterColumn>(AfterColumn.class);
		AfterColumn afterColumn = new AfterColumn();
		afterColumn.value1 = 1;
		afterColumn.value2 = 2;
		afterColumn.value3 = 3;
		assertEquals("\"value1\",\"value3\",\"value2\"", processor.buildHeaderLine(false));
		assertEquals("1,3,2", processor.buildLine(afterColumn, false));
	}

	@Test
	public void testAfterColumnComplex() {
		CsvProcessor<AfterColumnComplex> processor = new CsvProcessor<AfterColumnComplex>(AfterColumnComplex.class);
		AfterColumnComplex afterColumnComplex = new AfterColumnComplex();
		afterColumnComplex.value1 = 1;
		afterColumnComplex.value2 = 2;
		afterColumnComplex.value3 = 3;
		afterColumnComplex.value4 = 4;
		afterColumnComplex.value5 = 5;
		afterColumnComplex.value6 = 6;
		afterColumnComplex.value7 = 7;
		assertEquals("\"value1\",\"value7\",\"value4\",\"value6\",\"value3\",\"value2\",\"value5\"",
				processor.buildHeaderLine(false));
		assertEquals("1,7,4,6,3,2,5", processor.buildLine(afterColumnComplex, false));
	}

	@Test(expected = IllegalStateException.class)
	public void testNoFirstField() {
		new CsvProcessor<NoFirstField>(NoFirstField.class).initialize();
	}

	@Test(expected = IllegalStateException.class)
	public void testAfterColumnLoop() {
		new CsvProcessor<AfterColumnLoop>(AfterColumnLoop.class).initialize();
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
		@CsvColumn
		private int intValue;
		@CsvColumn(mustNotBeBlank = true)
		private String string;
		@CsvColumn
		private long longValue;
		@CsvColumn(converterClass = UnquotedStringConverter.class, mustBeSupplied = false)
		private String unquoted;
		@CsvColumn(converterFlags = BooleanConverter.PARSE_ERROR_ON_INVALID_VALUE)
		private boolean bool;

		public Basic() {
			// for simplecsv
		}

		public Basic(int intValue, String string, long longValue, String specialString, boolean bool) {
			this.intValue = intValue;
			this.string = string;
			this.longValue = longValue;
			this.unquoted = specialString;
			this.bool = bool;
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

		public boolean isBool() {
			return bool;
		}
	}

	public static class BasicSubclass extends Basic {
	}

	public static class BasicSubclassDupField extends Basic {
		@CsvColumn
		private int intValue;
	}

	public static class DefaultValue {
		public static final String DEFAULT_VALUE = "1";
		@CsvColumn(defaultValue = DEFAULT_VALUE)
		private int value;
	}

	public static class MustNotBeBlank {
		@CsvColumn(mustNotBeBlank = true)
		private int value;
	}

	private static class NoConstructor {
		@CsvColumn
		private int value;
	}

	public static class QuoteInColumnHeader {
		@CsvColumn(columnName = QUOTE_IN_HEADER)
		private int value;
	}

	public static class GetSetMethod {
		private int value;

		@CsvColumn
		public int getValue() {
			return value;
		}

		@CsvColumn
		public void setValue(int value) {
			this.value = value;
		}
	}

	public static class MissingSetMethod {
		@CsvColumn
		public int getValue() {
			return 0;
		}
	}

	public static class GetMethodReturnsVoid {
		@CsvColumn
		public void getValue() {
		}

		@CsvColumn
		public void setValue(int value) {
		}
	}

	public static class SetMethodWrongNumParams {
		@CsvColumn
		public int getValue() {
			return 0;
		}

		@CsvColumn
		public void setValue(int value, int value2) {
		}
	}

	public static class GetSetMethodTypeNoMatch {
		@CsvColumn
		public int getValue() {
			return 0;
		}

		@CsvColumn
		public void setValue(Integer value) {
		}
	}

	public static class AfterColumn {
		@CsvColumn
		int value1;
		@CsvColumn(afterColumn = "value3")
		int value2;
		@CsvColumn
		int value3;
	}

	public static class AfterColumnComplex {
		@CsvColumn
		int value1;
		@CsvColumn(afterColumn = "value3")
		int value2;
		@CsvColumn
		int value3;
		@CsvColumn(afterColumn = "value7")
		int value4;
		@CsvColumn
		int value5;
		@CsvColumn(afterColumn = "value4")
		int value6;
		// this should come before value4
		@CsvColumn(afterColumn = "value1")
		int value7;
	}

	public static class NoFirstField {
		@CsvColumn(afterColumn = "value2")
		private int value1;
		@CsvColumn(afterColumn = "value1")
		private int value2;
	}

	public static class AfterColumnLoop {
		@CsvColumn
		private int value1;
		@CsvColumn(afterColumn = "value3")
		private int value2;
		@CsvColumn(afterColumn = "value2")
		private int value3;
	}

	/* ================================================================================================= */

	public static class UnquotedStringConverter implements Converter<String, Void> {
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

	public static class IntPlusOneConverter implements Converter<Integer, Void> {
		@Override
		public Void configure(String format, long flags, ColumnInfo<Integer> field) {
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
		public String javaToString(ColumnInfo<Integer> columnInfo, Integer value) {
			return value.toString();
		}

		@Override
		public Integer stringToJava(String line, int lineNumber, int linePos, ColumnInfo<Integer> columnInfo,
				String value, ParseError parseError) {
			return Integer.parseInt(value) + 1;
		}
	}

	public static class IntThrowsConverter implements Converter<Integer, Void> {
		@Override
		public Void configure(String format, long flags, ColumnInfo<Integer> field) {
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
		public String javaToString(ColumnInfo<Integer> columnInfo, Integer value) {
			return value.toString();
		}

		@Override
		public Integer stringToJava(String line, int lineNumber, int linePos, ColumnInfo<Integer> columnInfo,
				String value, ParseError parseError) throws ParseException {
			// this could throw a runtime exception
			Integer.parseInt(value);
			throw new ParseException("value should be an invalid int", 0);
		}
	}
}
