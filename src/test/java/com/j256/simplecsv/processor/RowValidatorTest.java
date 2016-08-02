package com.j256.simplecsv.processor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.junit.Test;

import com.j256.simplecsv.common.CsvField;
import com.j256.simplecsv.processor.ParseError.ErrorType;

public class RowValidatorTest {

	@Test
	public void testBasic() throws ParseException {
		CsvProcessor<Basic> processor = new CsvProcessor<Basic>(Basic.class);
		processor.setRowValidator(new RowValidator<Basic>() {
			@Override
			public void validateRow(String line, int lineNumber, Basic entity, ParseError parseError) {
				if (entity.intValue >= 100) {
					parseError.setErrorType(ErrorType.INVALID_ENTITY);
				}
			}
		});
		ParseError error = new ParseError();
		assertNotNull(processor.processRow("99,wow", error));
		assertFalse(error.isError());

		assertNull(processor.processRow("100,wow", error));
		assertTrue(error.isError());
		assertTrue(error.getMessage(), error.getMessage().startsWith("entity validation"));
	}

	protected static class Basic {
		@CsvField
		private int intValue;
		@CsvField
		private String string;

		public Basic() {
			// for simplecsv
		}

		public int getIntValue() {
			return intValue;
		}

		public String getStringValue() {
			return string;
		}
	}
}
