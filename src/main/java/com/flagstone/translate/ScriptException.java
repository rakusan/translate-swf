package com.flagstone.translate;

import java.util.ArrayList;
import java.util.List;

public class ScriptException extends Exception {

    /** Serial number identifying the version of the object. */
    private static final long serialVersionUID = 1;

	private final transient List<ScriptError>errors;

	public ScriptException(List<ScriptError> list) {
		errors = new ArrayList<ScriptError>(list);
	}

	public List<ScriptError> getErrors() {
		return new ArrayList<ScriptError>(errors);
	}
}
