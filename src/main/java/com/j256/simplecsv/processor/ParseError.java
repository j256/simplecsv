package com.j256.simplecsv.processor;

import com.j256.simplecsv.converter.Converter;

/**
 * Used to report back with any parsing or internal errors.
 * 
 * <p>
 * To use in your {@link Converter} class, you should return null from
 * {@link Converter#stringToJava(String, int, FieldInfo, String, ParseError)} and set the error type to something other
 * than {@link ErrorType#NONE}. The message can be null if there is no additional information about the error.
 * </p>
 * 
 * @author graywatson
 */
public class ParseError {

	private ErrorType errorType = ErrorType.NONE;
	private String message;
	private String line;
	private int lineNumber;
	private int linePos;

	public ErrorType getErrorType() {
		return errorType;
	}

	public void setErrorType(ErrorType errorType) {
		this.errorType = errorType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String getLine() {
		return line;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLinePos(int linePos) {
		this.linePos = linePos;
	}

	public int getLinePos() {
		return linePos;
	}

	/**
	 * Resets all of the fields to non-error status. This is used internally so we can reuse an instance of this class.
	 */
	public void reset() {
		this.errorType = ErrorType.NONE;
		this.message = null;
		this.line = null;
		this.lineNumber = 0;
		this.linePos = 0;
	}

	/**
	 * Returns true if the error-type is not {@link ErrorType#NONE}.
	 */
	public boolean isError() {
		return (errorType != ErrorType.NONE);
	}

	@Override
	public String toString() {
		if (message == null) {
			return errorType.getTypeMessage();
		} else {
			return message + ", type " + errorType.getTypeMessage();
		}
	}

	/**
	 * The type of the error.
	 */
	public enum ErrorType {
		/** no error */
		NONE("none"),
		/** line is in an invalid format */
		INVALID_FORMAT("invalid format"),
		/** line seems to be truncated */
		TRUNCATED_VALUE("truncated value"),
		/** no header line read */
		NO_HEADER("no header line"),
		/** header line seems to be invalid */
		INVALID_HEADER("no valid header line"),
		/** null value for this field is invalid */
		INVALID_NULL("null value is invalid"),
		/** internal error was encountered */
		INTERNAL_ERROR("internal error"),
		// end
		;

		private String typeString;

		private ErrorType(String typeString) {
			this.typeString = typeString;
		}

		public String getTypeMessage() {
			return typeString;
		}
	}
}
