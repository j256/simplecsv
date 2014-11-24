package com.j256.simplecsv;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.Test;

import com.j256.simplecsv.converter.StringConverter;

public class CsvProcessorTest {

	@Test
	public void testBasic() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		testReadLine(processor, 1, "str", 12321321321321312L, "wqopdkq");
	}

	@Test
	public void testSingleQuotes() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		testReadLine(processor, 1, "\"", 2, "");
	}

	@Test
	public void testTwoQuotes() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		testReadLine(processor, 1, "\"\"", 2, "");
	}

	@Test
	public void testTwoQuotesPlus() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		testReadLine(processor, 1, "\"\"wow\"", 2, "");
	}

	@Test(expected = ParseException.class)
	public void testNotEnoughCells() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		processor.readLine("1,2");
	}

	@Test
	public void testOutput() {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		int intValue = 100;
		String str = "strwow";
		long longValue = 341442323234552L;
		String unquoted = "fewpofjwe";
		Basic basic = new Basic(intValue, str, longValue, unquoted);
		String line = processor.writeLine(basic, false);
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
		String line = processor.writeLine(basic, false);
		assertEquals(intValue + ",\"" + beforeQuote + "\"\"" + afterQuote + "\"," + longValue + "," + unquoted, line);
	}

	@Test
	public void testSeparatorStringOutput() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		int intValue = 200;
		String str = "has,comma";
		long longValue = 3452L;
		String unquoted = "u,q";
		Basic basic = new Basic(200, "has,comma", longValue, unquoted);
		String written = processor.writeLine(basic, false);
		basic = processor.readLine(written);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(str, basic.getString());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquotedString());
	}

	private void testReadLine(CsvProcessor<Basic> processor, int intValue, String str, long longValue, String unquoted)
			throws ParseException {
		String line = intValue + ",\"" + str + "\"," + longValue + "," + unquoted;
		Basic basic = processor.readLine(line);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(str, basic.getString());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquotedString());

		String written = processor.writeLine(basic, false);
		basic = processor.readLine(written);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(str, basic.getString());
		assertEquals(longValue, basic.getLongValue());
		assertEquals(unquoted, basic.getUnquotedString());
	}

	private static class Basic {
		@CsvField
		private int intValue;
		@CsvField
		private String string;
		@CsvField
		private long longValue;
		@CsvField(converterClass = UnquotedStringConverter.class)
		private String unquotedString;

		@SuppressWarnings("unused")
		public Basic() {
			// for simplecsv
		}

		public Basic(int intValue, String string, long longValue, String specialString) {
			this.intValue = intValue;
			this.string = string;
			this.longValue = longValue;
			this.unquotedString = specialString;
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

		public String getUnquotedString() {
			return unquotedString;
		}
	}

	public static class UnquotedStringConverter extends StringConverter {
		@Override
		public boolean isNeedsQuotes(ConfigInfo configInfo) {
			return false;
		}
	}
}
