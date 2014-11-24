package com.j256.simplecsv.converter;

import java.util.UUID;

import org.junit.Test;

public class UuidConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		UuidConverter converter = new UuidConverter();
		converter.configure(null, 0, null);
		for (int i = 0; i < 100; i++) {
			testConverter(converter, null, UUID.randomUUID());
		}
		testConverter(converter, null, null);
	}
}
