package com.j256.simplecsv.converter;

import org.junit.Test;

public class ConverterUtilsTest {

	@Test(expected = IllegalArgumentException.class)
	public void testPrivateConstructor() {
		ConverterUtils.constructConverter(PrivateConstructor.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorThrows() {
		ConverterUtils.constructConverter(ConstructorThrows.class);
	}

	@Test
	public void testCoverage() {
		new ConverterUtils();
	}

	private static class PrivateConstructor extends StringConverter {
		private PrivateConstructor() {
			// constructor not available
		}
	}

	private static class ConstructorThrows extends StringConverter {
		@SuppressWarnings("unused")
		public ConstructorThrows() {
			throw new RuntimeException("constructor throws");
		}
	}
}
