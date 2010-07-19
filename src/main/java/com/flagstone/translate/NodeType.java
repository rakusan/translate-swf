package com.flagstone.translate;

public enum NodeType {
	/** Include directive. */
	INCLUDE("#include"),
	/** Define directive. */
	DEFINE("#define"),
	/** Conditional directive. */
	IFDEF("#ifdef"),
	/** An array of nodes. */
	ARRAY("Array"),
	/** A Button node. */
	BUTTON("Button"),
	/** A movie clip. */
	MOVIE_CLIP("MovieClip"),
	/** A list of statements. */
	STATEMENT_LIST("Statements"),
	/** A list of nodes. */
	LIST("List"),
	/** No operation. */
	NO_OP("NoOp"),
	/** An if statement. */
	IF("if"),
	/** A for loop. */
	FOR("for"),
	/** A for..in loop. */
	FORIN("for..in"),
	/** A while loops. */
	WHILE("while"),
	/** A do..while loop. */
	DO("do..while"),
	/** A with statement */
	WITH("with"),
	/** A movie clip event handler. */
	ONCLIPEVENT("onClipEvent()"),
	/** A button event handler. */
	ON("on()"),
	/** A break statement. */
	BREAK("break"),
	/** A return statement. */
	RETURN("return"),
	/** A continue statement. */
	CONTINUE("continue"),
	/** A miscellaneous value. */
	VALUE("Value"),
	/** A boolean value. */
	BOOLEAN("Boolean"),
	/** An integer value. */
	INTEGER("Integer"),
	/** A floating point value. */
	DOUBLE("Double"),
	/** A string value. */
	STRING("String"),
	/** A null literal. */
	NULL("Null"),
	/** An identifier. */
	IDENTIFIER("Identifier"),
	/** An attribute of an object. */
	ATTRIBUTE("Attribute"),
	/** The name of a method */
	METHOD("Method"),
	/** A built-in function. */
	FUNCTION("Function"),
	/** Creating an instance of an object. */
	NEW_OBJECT("NewObject"),
	/** An array subscript. */
	SUBSCRIPT("[]"),
	/** A user defined function. */
	DEFINE_FUNCTION("DefineFunction"),
	/** An anonymous array. */
	DEFINE_ARRAY("DefineArray"),
	/** A user defined object. */
	DEFINE_OBJECT("DefineObject"),
	/** A method on a user defined object. */
	DEFINE_METHOD("DefineMethod"),
	/** An attribute on a user defined object. */
	DEFINE_ATTRIBUTE("DefineAttribute"),
	/** A variable definition. */
	DEFINE_VARIABLE("var"),
	/** Add. */
	ADD("+"),
	/** Subtract. */
	SUB("-"),
	/** Multiply. */
	MUL("*"),
	/** Divide. */
	DIV("/"),
	/** Modulo. */
	MOD("%"),
	/** Logical shift left. */
	LSL("<<"),
	/** Arithmetic shift right. */
	ASR(">>"),
	/** Logical shift right. */
	LSR(">>>"),
	/** Bitwise AND. */
	BIT_AND("&"),
	/** Bitwise OR. */
	BIT_OR("|"),
	/** Bitwise Exclusive-OR. */
	BIT_XOR("^"),
	/** Logical AND. */
	LOGICAL_AND("&&"),
	/** Logical OR. */
	LOGICAL_OR("||"),
	/** Equal. */
	EQUAL("=="),
	/** Not equal. */
	NOT_EQUAL("!="),
	/** Greater than. */
	GREATER_THAN(">"),
	/** Less than. */
	LESS_THAN("<"),
	/** Greater than or equal. */
	GREATER_THAN_EQUAL(">="),
	/** Less than or equal. */
	LESS_THAN_EQUAL("<="),
	/** Select. */
	SELECT("?"),
	/** Unary not. */
	NOT("!"),
	/** Unary bitwise not. */
	BIT_NOT("~"),
	/** Unary plus. */
	PLUS("+"),
	/** Unary minus. */
	MINUS("-"),
	/** Pre-increment. */
	PRE_INC("++"),
	/** Pre-decrement. */
	PRE_DEC("--"),
	/** Post-increment. */
	POST_INC("++"),
	/** Post-decrement. */
	POST_DEC("--"),
	/** Assign. */
	ASSIGN("="),
	/** Assign add. */
	ASSIGN_ADD("+="),
	/** Assign subtract. */
	ASSIGN_SUB("-="),
	/** Assign multiply. */
	ASSIGN_MUL("*="),
	/** Assign divide. */
	ASSIGN_DIV("/="),
	/** Assign modulo. */
	ASSIGN_MOD("%="),
	/** Assign logical shift left. */
	ASSIGN_LSL("<<="),
	/** Assign arithmetic shift right.*/
	ASSIGN_ASR(">>="),
	/** Assign logical shift right. */
	ASSIGN_LSR(">>>="),
	/** Assign bitwise AND(. */
	ASSIGN_BIT_AND("&="),
	/** Assign bitwise OR. */
	ASSIGN_BIT_OR("|="),
	/** Assign bitwise exclusive-OR. */
	ASSIGN_BIT_XOR("^="),
	/** Object identity. */
	INSTANCEOF("instanceof"),
	/** Object reclamation. */
	DELETE("delete"),
	/** Strict equal. */
	STRICT_EQUAL("==="),
	/** Strict not equal. */
	STRICT_NOT_EQUAL("!=="),
	/** String concatenate. */
	STRING_ADD("add"),
	/** String equal. */
	STRING_EQUAL("eq"),
	/** String not equal. */
	STRING_NOT_EQUAL("ne"),
	/** String less than or equal. */
	STRING_LESS_THAN_EQUAL("le"),
	/** String greater than. */
	STRING_GREATER_THAN("gr"),
	/** String greater than or equal. */
	STRING_GREATER_THAN_EQUAL("ge"),
	/** Exception object. */
	EXCEPTION("exception"),
	/** Exception try block. */
	TRY("try"),
	/** Exception catch block. */
	CATCH("catch"),
	/** Exception final block. */
	FINALLY("finally"),
	/** Switch statement. */
	SWITCH("switch"),
	/** Throw an exception. */
	THROW("throw"),
	/** Switch case label. */
	LABEL("label"),
	/** Initialise movie clip. */
	INITCLIP("#initclip"),
	/** End movie clip initialisation. */
	ENDINITCLIP("#endinitclip"),
	/** Logical AND, before Flash 5. */
	AND("and"),
	/** Logical OR, before Flash 5. */
	OR("or");

	private final String name;

	private NodeType(final String str) {
		name = str;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}

