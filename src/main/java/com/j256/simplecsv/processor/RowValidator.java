package com.j256.simplecsv.processor;

import java.text.ParseException;

import com.j256.simplecsv.processor.ParseError.ErrorType;

/**
 * Row validator callback that can be registered with the {@link CsvProcessor#setRowValidator(RowValidator)} that
 * validates the entity _after_ it has been parsed from the CSV line.
 * 
 * @author graywatson
 */
public interface RowValidator<T> {

	/**
	 * Validate an entity that was parsed from the line at line-number. If there is a problem with the entity then set
	 * the parseError with error information, using at least the
	 * {@link ParseError#setErrorType(com.j256.simplecsv.processor.ParseError.ErrorType)} (probably with
	 * {@link ErrorType#INVALID_ENTITY}) and optimally a message. You can also throw a ParseException from this method
	 * which will be caught to set the ParseError if necessary.
	 */
	public void validateRow(String line, int lineNumber, T entity, ParseError parseError) throws ParseException;
}
