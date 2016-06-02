package com.j256.simplecsv.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Buffered reader that can wrap another reader and count the lines read.
 * 
 * @author graywatson
 */
public class BufferedReaderLineCounter extends BufferedReader {

	private int lineCount;

	public BufferedReaderLineCounter(Reader reader) {
		super(reader);
	}

	@Override
	public String readLine() throws IOException {
		String line = super.readLine();
		lineCount++;
		return line;
	}

	/**
	 * Return how many times the {@link #readLine()} method was called.
	 */
	public int getLineCount() {
		return lineCount;
	}
}
