package com.j256.simplecsv;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.Test;

public class CsvProcessorTest {

	@Test
	public void testBasic() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		int value = 1;
		String str = "str";
		Basic basic = processor.readLine(value + "," + str);
		assertEquals(value, basic.getValue());
		assertEquals(str, basic.getString());
	}

	@Test
	public void testQuoted() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		int value = 100;
		String str = "strwow";
		Basic basic = processor.readLine("\"" + value + "\",\"" + str + "\"");
		assertEquals(value, basic.getValue());
		assertEquals(str, basic.getString());
	}

	private static class Basic {
		@CsvField
		private int value;
		@CsvField
		private String string;

		@SuppressWarnings("unused")
		public Basic() {
			// for simplecsv
		}

		public Basic(int value, String string) {
			this.value = value;
			this.string = string;
		}

		public int getValue() {
			return value;
		}

		public String getString() {
			return string;
		}
	}
}
