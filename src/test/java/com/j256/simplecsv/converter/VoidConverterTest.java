package com.j256.simplecsv.converter;

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
}
