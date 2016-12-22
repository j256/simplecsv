package com.j256.simplecsv.common;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.Test;

import com.j256.simplecsv.converter.BooleanConverter;
import com.j256.simplecsv.processor.CsvProcessor;
import com.j256.simplecsv.processor.CsvProcessorTest.UnquotedStringConverter;

public class CsvFieldTest {

	@Test
	public void testBasic() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		testReadLine(processor, 1, "str", 12321321321321312L, "wqopdkq");
	}

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

	@SuppressWarnings("deprecation")
	public static class Basic {
		@CsvField
		private int intValue;
		@CsvField(mustNotBeBlank = true)
		private String string;
		@CsvField
		private long longValue;
		@CsvField(converterClass = UnquotedStringConverter.class, mustBeSupplied = false)
		private String unquoted;
		@CsvField(converterFlags = BooleanConverter.PARSE_ERROR_ON_INVALID_VALUE)
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

}
