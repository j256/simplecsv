package com.j256.simplecsv;

import java.text.ParseException;

import org.junit.Test;

public class CsvProcessorTest {

	@Test
	public void testBasic() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		
		processor.readLine("hello");
	}

	private static class Basic {
		@CsvField
		private int value;
		@CsvField
		private int string;

		public Basic() {
			// for simplecsv
		}
		
		public Basic(int value, int string) {
			this.value = value;
			this.string = string;
		}

		public int getValue() {
			return value;
		}

		public int getString() {
			return string;
		}
	}
}
