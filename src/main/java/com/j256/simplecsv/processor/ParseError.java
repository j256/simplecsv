package com.j256.simplecsv.processor;

import com.j256.simplecsv.converter.Converter;

/**
 * Used to set errors if necessary. To use this you should return null from
 * {@link Converter#stringToJava(String, int, FieldInfo, String, ParseError)} and set the error type to something other
 * than {@link ErrorType#NONE}. The message can be null if there is no additional information about the error.
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

	public void reset() {
		this.errorType = ErrorType.NONE;
		this.message = null;
		this.line = null;
		this.lineNumber = 0;
		this.linePos = 0;
	}

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
		NONE("none"),
		INVALID_FORMAT("invalid format"),
		TRUNCATED_VALUE("truncated value"),
		NO_HEADER("no header line"),
		INVALID_HEADER("no valid header line"),
		INVALID_NULL("null value is invalid"),
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
