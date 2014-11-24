package com.j256.simplecsv;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.Test;

public class CsvProcessorTest {

	@Test
	public void testBasic() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		int intValue = 1;
		String str = "str";
		long longValue = 12321321321321312L;
		Basic basic = processor.readLine(intValue + "," + str + "," + longValue);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(str, basic.getString());
		assertEquals(longValue, basic.getLongValue());
	}

	@Test
	public void testQuoted() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		int intValue = 100;
		String str = "strwow";
		long longValue = 1232132131221321312L;
		Basic basic = processor.readLine("\"" + intValue + "\",\"" + str + "\"," + longValue);
		assertEquals(intValue, basic.getIntValue());
		assertEquals(str, basic.getString());
		assertEquals(longValue, basic.getLongValue());
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
		Basic basic = new Basic(intValue, str, longValue);
		String line = processor.writeLine(basic, false);
		assertEquals(intValue + ",\"" + str + "\"," + longValue, line);
	}

	private static class Basic {
		@CsvField
		private int intValue;
		@CsvField
		private String string;
		@CsvField
		private long longValue;

		@SuppressWarnings("unused")
		public Basic() {
			// for simplecsv
		}

		public Basic(int intValue, String string, long longValue) {
			this.intValue = intValue;
			this.string = string;
			this.longValue = longValue;
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
	}
}
