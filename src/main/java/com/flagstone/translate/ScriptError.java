package com.flagstone.translate;


public class ScriptError {

	public enum Type {
		SCRIPT_NOT_FOUND,
		SCRIPT_READ_ERROR,
		SCRIPT_PARSE_ERROR;
	}

	private final Type type;
	private final String filename;
	private final int beginLine;
	private final int endLine;
	private final int beginColumn;
	private final int endColumn;

	public ScriptError(final Type kind, final String path) {
		type = kind;
		filename = path;
		beginLine = 0;
		endLine = 0;
		beginColumn = 0;
		endColumn = 0;
	}

	public ScriptError(final Type kind, final String path,
			final int firstLine, final int lastLine,
			final int firstCol, final int lastCol) {
		type = kind;
		filename = path;
		beginLine = firstLine;
		endLine = lastLine;
		beginColumn = firstCol;
		endColumn = lastCol;
	}

	public ScriptError.Type getType() {
		return type;
	}

	public String getFilename() {
		return filename;
	}

	public int getBeginLine() {
		return beginLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public int getBeginColumn() {
		return beginColumn;
	}

	public int getEndColumn() {
		return endColumn;
	}

}
