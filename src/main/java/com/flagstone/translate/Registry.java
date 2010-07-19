package com.flagstone.translate;

public interface Registry {
	CodeGenerator getGenerator(NodeType type);
	void setGenerator(NodeType type, CodeGenerator generator);
}
