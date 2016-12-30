package com.j256.simplecsv.converter;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

public class UuidConverterTest extends AbstractConverterTest {

	@Test
	public void testStuff() throws Exception {
		UuidConverter converter = UuidConverter.getSingleton();
		for (int i = 0; i < 10; i++) {
			testConverter(converter, UUID.class, null, 0, UUID.randomUUID());
		}
		testConverter(converter, UUID.class, null, 0, null);
	}

	@Test
	public void testConverage() {
		UuidConverter converter = UuidConverter.getSingleton();
		assertTrue(converter.isNeedsQuotes(null));
		assertTrue(converter.isAlwaysTrimInput());
	}
}
