package com.j256.simplecsv.processor;

import com.j256.simplecsv.common.CsvColumn;

/**
 * Definition of a class which compares column names to see if they match. The default is just to do a
 * String.equals(...) but you might want to ignore "*" or other characters at the end of the file.
 * 
 * @author graywatson
 */
public interface ColumnNameMatcher {

	/**
	 * Returns true if the definition from the {@link CsvColumn} annotation matches the name from the CSV file.
	 */
	public boolean matchesColumnName(String definitionName, String csvName);
}
