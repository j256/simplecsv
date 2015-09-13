package com.j256.simplecsv.converter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class VoidConverterTest {

	@Test
	public void testStuff() {
		VoidConverter converter = new VoidConverter();
		converter.configure(null, 0, null);
		assertNull(converter.javaToString(null, null));
		assertNull(converter.stringToJava(null, 0, null, null, null));
	}

	@Test
	public void testConverage() {
		VoidConverter converter = new VoidConverter();
		assertFalse(converter.isNeedsQuotes(null));
		assertFalse(converter.isAlwaysTrimInput());
	}
}
