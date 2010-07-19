package com.flagstone.translate;

public interface Generator {
	CodeGenerator getGenerator(NodeType type);
	void setGenerator(NodeType type, CodeGenerator generator);
}
