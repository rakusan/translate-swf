package com.flagstone.translate;

import java.io.InputStream;
import java.util.List;

public interface Parser {
	void setPath(final String path);
	void setErrors(final List<ScriptError> list);
	Node parse(final InputStream stream);
}
