package com.j256.simplecsv.converter;

import static org.junit.Assert.*;

import org.junit.Test;

public class VoidConverterTest {

	@Test
	public void testStuff() {
		VoidConverter converter = new VoidConverter();
		converter.configure(null, 0, null);
		converter.javaToString(null, null, null);
		assertNull(converter.stringToJava(null, 0, null, null, null));
	}
}
