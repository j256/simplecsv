package com.j256.simplecsv;

import java.text.ParseException;

import org.junit.Test;

public class CsvProcessorTest {

	@Test
	public void testBasic() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		Basic basic = processor.readLine("1,str");
	}

	private static class Basic {
		@CsvField
		private int value;
		@CsvField
		private String string;

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
