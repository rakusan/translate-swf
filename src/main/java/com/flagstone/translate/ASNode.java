/*
 * ASNode.java
 * Translate
 *
 * Copyright (c) 2003-2010 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.flagstone.translate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.flagstone.transform.action.Action;
import com.flagstone.transform.action.ActionObject;
import com.flagstone.transform.action.ActionTypes;
import com.flagstone.transform.action.BasicAction;
import com.flagstone.transform.action.Call;
import com.flagstone.transform.action.ExceptionHandler;
import com.flagstone.transform.action.GetUrl;
import com.flagstone.transform.action.GetUrl2;
import com.flagstone.transform.action.GotoFrame;
import com.flagstone.transform.action.GotoFrame2;
import com.flagstone.transform.action.GotoLabel;
import com.flagstone.transform.action.If;
import com.flagstone.transform.action.Jump;
import com.flagstone.transform.action.NewFunction;
import com.flagstone.transform.action.Null;
import com.flagstone.transform.action.Property;
import com.flagstone.transform.action.Push;
import com.flagstone.transform.action.RegisterCopy;
import com.flagstone.transform.action.RegisterIndex;
import com.flagstone.transform.action.SetTarget;
import com.flagstone.transform.action.Table;
import com.flagstone.transform.action.TableIndex;
import com.flagstone.transform.action.Void;
import com.flagstone.transform.action.WaitForFrame2;
import com.flagstone.transform.action.With;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFEncodeable;
import com.flagstone.translate.as.ParseException;
import com.flagstone.translate.as.Token;

/**
 * ASNode is the class used by the parser to construct a tree representation of
 * an ActionScript file based on the parser grammar.
 *
 * Node trees can also be constructed 'manually' and then encoded to give the
 * binary representation of the byte-codes and actions that will be executed by
 * the Flash Player.
 *
 * For example, the node tree for the ActionScript statement:
 *
 * <pre>
 * c = a + b;
 * </pre>
 *
 * Can be represented using the following code to build the tree.
 *
 * <pre>
 * ASNode a = new ASNode(ASNode.Identifier, &quot;a&quot;);
 * ASNode b = new ASNode(ASNode.Identifier, &quot;b&quot;);
 * ASNode c = new ASNode(ASNode.Identifier, &quot;c&quot;);
 *
 * ASNode add = new ASNode(ASNode.Add, a, b);
 * ASNode assign = new ASNode(a, add);
 * </pre>
 *
 * The ASNode class defines a full range of node types ranging from specifying
 * literals through to complex structures such as iterative and conditional
 * constructs such as for loops and if/else blocks.
 *
 * The simplest method for determining the structure of the trees that represent
 * different structure in ActionScript is to use the Interpreter class provided
 * in the framework and dump out the structure of the parsed code.
 *
 */
public class ASNode extends Object {
    /**
     * Array nodes are used to represent any list of ActionScript statements.
     * Use this type of node when constructing trees to represent sequences of
     * actions for FSDoAction, FSClipEvent or FSButtonEvent objects.
     */
    public static final int Array = 1;
    /**
     * Button nodes are used to represent the on() block statement in
     * ActionScript. Use this type of node when constructing trees that will be
     * encoded and added to FSDefineButton2 objects.
     */
    public static final int Button = 2;
    /**
     * MovieClip nodes are used to represent the onClipEvent() block statement
     * in ActionScript. Use this type of node when constructing trees that will
     * be encoded and added to FSPlaceObject2 objects.
     */
    public static final int MovieClip = 3;
    /**
     * List nodes are used to represent groups of one or more statements. They
     * are used to represent statements included in any block structure such as
     * an if statement or for loop.
     *
     * Lists are also used to simplify the construction of complex statements
     * such as for loops. Using Lists, a for loop contains a maximum of four
     * child nodes with lists used to group the statements forming the
     * initialisation and iteration part of the for statement and body of the
     * loop.
     */
    public static final int StatementList = 4;
    public static final int List = 5;
    /**
     * NoOp is used as a place-holder for child nodes with resorting to using
     * null. No actions will be generated when the node is translated.
     */
    public static final int NoOp = 6;

    /** Use to represent if statements */
    public static final int If = 7;
    /** Use to represent for loops */
    public static final int For = 8;
    /** Use to represent for..in statements */
    public static final int ForIn = 9;
    /** Use to represent while loops */
    public static final int While = 10;
    /** Use to represent do..while loops */
    public static final int Do = 11;
    /** Use to represent with statements */
    public static final int With = 12;
    /** Use to represent onClipEvent statements */
    public static final int OnClipEvent = 13;
    /** Use to represent on statements */
    public static final int On = 14;
    /** Use to represent break statements */
    public static final int Break = 15;
    /** Use to represent return statements */
    public static final int Return = 16;
    /** < Use to represent continue statements */
    public static final int Continue = 17;
    /**
     * Value is an abstract node type used to group together nodes that will
     * result in a value being generated such as subscripting an array variable
     * or dereferencing an object's attribute.
     */
    public static final int Value = 18;

    /** Use to represent a boolean value */
    public static final int BooleanLiteral = 20;
    /** Use to represent an integer value */
    public static final int IntegerLiteral = 21;
    /** Use to represent an double-precision floating point value */
    public static final int DoubleLiteral = 22;
    /** Use to represent a string value */
    public static final int StringLiteral = 23;
    /** Use to represent a null literal */
    public static final int NullLiteral = 24;

    /** Use to represent a variable */
    public static final int Identifier = 30;
    /** Use to represent an attribute of an object */
    public static final int Attribute = 31;
    /** Use to represent the name of a method */
    public static final int Method = 32;
    /** Use to represent the name of one of ActionScript's built-in functions. */
    public static final int Function = 33;
    /** Use to represent new statements for creating instances of objects. */
    public static final int NewObject = 34;
    /**
     * Use to represent subscript operation when accessing the elements of an
     * array.
     */
    public static final int Subscript = 35;

    /** Use to represent a user defined function. */
    public static final int DefineFunction = 36;
    /** Use to represent an anonyomus array. */
    public static final int DefineArray = 37;
    /** Use to represent a user defined object. */
    public static final int DefineObject = 38;
    /** Use to represent a method on a user defined object. */
    public static final int DefineMethod = 39;
    /** Use to represent an attribute on a user defined object. */
    public static final int DefineAttribute = 40;
    /** Use to represent a var statement */
    public static final int DefineVariable = 41;
    /** Add operation */
    public static final int Add = 42;
    /** Subtract operation */
    public static final int Sub = 43;
    /** Multiply operation */
    public static final int Mul = 44;
    /** Divide operation */
    public static final int Div = 45;
    /** Modulo operation */
    public static final int Mod = 46;
    /** Logical Shift Left operation */
    public static final int LSL = 47;
    /** Arithmetic Shift Right operation */
    public static final int ASR = 48;
    /** Logical Shift Right operation */
    public static final int LSR = 49;
    /** Bitwise AND operation */
    public static final int BitAnd = 50;
    /** Bitwise OR operation */
    public static final int BitOr = 51;
    /** Bitwise Exclusive-OR operation */
    public static final int BitXOr = 52;
    /** Logical AND operation */
    public static final int LogicalAnd = 53;
    /** Logical OR operation */
    public static final int LogicalOr = 54;
    /** Equal comparison */
    public static final int Equal = 55;
    /** Not Equal comparison */
    public static final int NotEqual = 56;
    /** Greater Than comparison */
    public static final int GreaterThan = 57;
    /** Less Than comparison */
    public static final int LessThan = 58;
    /** Greater Than or Equal comparison */
    public static final int GreaterThanEqual = 59;
    /** Less Than or Equal comparison */
    public static final int LessThanEqual = 60;
    /** ternary operator. */
    public static final int Select = 61;
    /** Unary not */
    public static final int Not = 62;
    /** Unary bit-not */
    public static final int BitNot = 63;
    /** Unary plus */
    public static final int Plus = 64;
    /** Unary minus */
    public static final int Minus = 65;
    /** Pre-increment */
    public static final int PreInc = 66;
    /** Pre-decrement */
    public static final int PreDec = 67;
    /** Post-increment */
    public static final int PostInc = 68;
    /** Post-decrement */
    public static final int PostDec = 69;
    /** Assign, = */
    public static final int Assign = 70;
    /** Assign add, += */
    public static final int AssignAdd = 71;
    /** Assign subtract, -= */
    public static final int AssignSub = 72;
    /** Assign multiply, *= */
    public static final int AssignMul = 73;
    /** Assign divide, /= */
    public static final int AssignDiv = 74;
    /** Assign modulo, %= */
    public static final int AssignMod = 75;
    /** Assign logical shift left, <<= */
    public static final int AssignLSL = 76;
    /** Assign arithmetic shift right, >>= */
    public static final int AssignASR = 77;
    /** Assign logical shift right, >>>= */
    public static final int AssignLSR = 78;
    /** Assign bitwise-AND, &= */
    public static final int AssignBitAnd = 79;
    /** Assign bitwise-OR, |= */
    public static final int AssignBitOr = 80;
    /** Assign bitwise-exclusive-OR, ^= */
    public static final int AssignBitXOr = 81;
    /** Object identity */
    public static final int InstanceOf = 82;
    /** Object reclamation */
    public static final int Delete = 83;
    /** Strict Equal comparison */
    public static final int StrictEqual = 84;
    /** Strict Not Equal comparison */
    public static final int StrictNotEqual = 85;
    /** Strict Not Equal comparison */
    public static final int StringAdd = 86;
    public static final int StringEqual = 87;
    public static final int StringNotEqual = 88;
    public static final int StringLessThanEqual = 89;
    public static final int StringGreaterThan = 90;
    public static final int StringGreaterThanEqual = 91;
    public static final int Exception = 92;
    public static final int Try = 93;
    public static final int Catch = 94;
    public static final int Finally = 95;
    public static final int Switch = 96;
    public static final int Throw = 97;
    public static final int Label = 98;
    public static final int InitClip = 99;
    public static final int EndInitClip = 100;
    public static final int And = 101;
    public static final int Or = 102;

    /*
     * Names for each of the different types of node. Names are used in the
     * toString() method.
     */
    private static String[] nodeNames = { "", "Frame", "Button", "MovieClip",
            "Statements", "List", "NoOp", "if", "for", "for..in", "while",
            "do..while", "With", "OnClipEvent", "On", "Break", "Return",
            "Continue", "Value", "", "Boolean", "Integer", "Double", "String",
            "Null", "", "", "", "", "", "Identifier", "Attribute", "Method",
            "Function", "NewObject", "Subscript", "Define Function",
            "Define Array", "Define Object", "Define Method",
            "Define Attribute", "Define Variable", "+", "-", "*", "/", "%",
            "<<", ">>", ">>>", "&", "|", "^", "&&", "||", "==", "!=", ">", "<",
            ">=", "<=", "?", "!", "~", "+x", "-x", "++x", "--x", "x++", "x--",
            "=", "+=", "-=", "*=", "/=", "%=", "<<=", ">>=", ">>>=", "&=",
            "|=", "^=", "intanceof", "delete", "===", "!==", "add", "eq", "ne",
            "le", "gt", "ge", "exception", "try", "catch", "finally", "switch",
            "throw", "label", "#initclip", "#endinitclip", "and", "or", };

    /*
     * Table for the different types of events that buttons respond to. The
     * table is accessible in the package as it is used in the ASParser to
     * convert the identifiers representing the different events into the codes
     * indicating which flags are set.
     */
    static Map<String, Integer> buttonEvents = new HashMap<String, Integer>();

    /*
     * Table for the different types of events that movie clips respond to. The
     * table is package accessible as it is used in the ASParser to convert the
     * identifiers representing the different events into the codes indicating
     * which flags are set.
     */
    static Map<String, Integer> clipEvents = new HashMap<String, Integer>();

    // Table for constants defined in Flash.
    private static Map<String, Object> constants = new HashMap<String, Object>();
    // Table for properties defined in Flash.
    private static Map<String, Integer> propertyNames = new HashMap<String, Integer>();
    // Table for properties defined in Flash 4 or earlier.
    private static Map<String, Integer> earlyPropertyNames = new HashMap<String, Integer>();
    // Table for the functions built into Flash.
    private static Map<String, Boolean> functions = new HashMap<String, Boolean>();
    // Table for the functions built into Flash that return a value.
    private static Map<String, Object> valueFunctions = new HashMap<String, Object>();
    // Table for the classes built into Flash that return a value.
    private static Map<String, Object> classes = new HashMap<String, Object>();

    static {
        /*
         * Button events identifies the values that represents the bit flags
         * that are set in the encoded event field as well as code values for
         * special keyboard keys.
         */
        buttonEvents.put("rollOver", new Integer(1));
        buttonEvents.put("rollOut", new Integer(2));
        buttonEvents.put("press", new Integer(4));
        buttonEvents.put("release", new Integer(8));
        buttonEvents.put("dragOut", new Integer(16));
        buttonEvents.put("dragOver", new Integer(160));
        buttonEvents.put("releaseOutside", new Integer(64));
        buttonEvents.put("menuDragOver", new Integer(160));
        buttonEvents.put("menuDragOut", new Integer(256));
        buttonEvents.put("<left>", new Integer(512));
        buttonEvents.put("<right>", new Integer(1024));
        buttonEvents.put("<home>", new Integer(1536));
        buttonEvents.put("<end>", new Integer(2048));
        buttonEvents.put("<insert>", new Integer(2560));
        buttonEvents.put("<delete>", new Integer(3072));
        buttonEvents.put("<backspace>", new Integer(4096));
        buttonEvents.put("<enter>", new Integer(6656));
        buttonEvents.put("<up>", new Integer(7168));
        buttonEvents.put("<down>", new Integer(7680));
        buttonEvents.put("<pageUp>", new Integer(8192));
        buttonEvents.put("<pageDown>", new Integer(8704));
        buttonEvents.put("<tab>", new Integer(9216));
        buttonEvents.put("<escape>", new Integer(9728));
        buttonEvents.put("<space>", new Integer(16384));

        /*
         * Button events identifies the values that represents the bit flags
         * that are set in the encoded event field.
         */
        clipEvents.put("load", new Integer(1));
        clipEvents.put("enterFrame", new Integer(2));
        clipEvents.put("unload", new Integer(4));
        clipEvents.put("mouseMove", new Integer(8));
        clipEvents.put("mouseDown", new Integer(16));
        clipEvents.put("mouseUp", new Integer(32));
        clipEvents.put("keyDown", new Integer(64));
        clipEvents.put("keyUp", new Integer(128));
        clipEvents.put("data", new Integer(256));

        constants.put("Math.E", new Double(Math.E));
        constants.put("Math.LN2", new Double(Math.log(2)));
        constants.put("Math.LOG2E", new Double(Math.log(Math.E) / Math.log(2)));
        constants.put("Math.LN10", new Double(Math.log(10)));
        constants.put("Math.LOG10E",
                new Double(Math.log(Math.E) / Math.log(10)));
        constants.put("Math.PI", new Double(Math.PI));
        constants.put("Math.SQRT1_2", new Double(Math.sqrt(0.5)));
        constants.put("Math.SQRT2", new Double(Math.sqrt(2)));
        constants.put("Number.MAX_VALUE", new Double(Double.MAX_VALUE));
        constants.put("Number.MIN_VALUE", new Double(Double.MIN_VALUE));
        constants.put("Number.NaN", new Double(Double.NaN));
        constants.put("Number.NEGATIVE_INFINITY", new Double(
                Double.NEGATIVE_INFINITY));
        constants.put("Number.POSITIVE_INFINITY", new Double(
                Double.POSITIVE_INFINITY));
        constants.put("Key.BACKSPACE", new Integer(8));
        constants.put("Key.CAPSLOCK", new Integer(20));
        constants.put("Key.CONTROL", new Integer(17));
        constants.put("Key.DELETEKEY", new Integer(46));
        constants.put("Key.DOWN", new Integer(40));
        constants.put("Key.END", new Integer(35));
        constants.put("Key.ENTER", new Integer(13));
        constants.put("Key.ESCAPE", new Integer(27));
        constants.put("Key.HOME", new Integer(36));
        constants.put("Key.INSERT", new Integer(45));
        constants.put("Key.LEFT", new Integer(37));
        constants.put("Key.PGDN", new Integer(34));
        constants.put("Key.PGUP", new Integer(33));
        constants.put("Key.RIGHT", new Integer(39));
        constants.put("Key.SHIFT", new Integer(16));
        constants.put("Key.SPACE", new Integer(32));
        constants.put("Key.TAB", new Integer(9));
        constants.put("Key.UP", new Integer(38));
        constants.put("newline", new String("\n"));
        constants.put("undefined", null);

        earlyPropertyNames.put("_x", new Integer(0));
        earlyPropertyNames.put("_y", new Integer(0x3f800000));
        earlyPropertyNames.put("_xscale", new Integer(0x40000000));
        earlyPropertyNames.put("_yscale", new Integer(0x40400000));
        earlyPropertyNames.put("_currentframe", new Integer(0x40800000));
        earlyPropertyNames.put("_totalframes", new Integer(0x40a00000));
        earlyPropertyNames.put("_alpha", new Integer(0x40c00000));
        earlyPropertyNames.put("_visible", new Integer(0x40e00000));
        earlyPropertyNames.put("_width", new Integer(0x41000000));
        earlyPropertyNames.put("_height", new Integer(0x41100000));
        earlyPropertyNames.put("_rotation", new Integer(0x41200000));
        earlyPropertyNames.put("_target", new Integer(0x41300000));
        earlyPropertyNames.put("_framesloaded", new Integer(0x41400000));
        earlyPropertyNames.put("_name", new Integer(0x41500000));
        earlyPropertyNames.put("_droptarget", new Integer(0x41600000));
        earlyPropertyNames.put("_url", new Integer(0x41700000));
        earlyPropertyNames.put("_highquality", new Integer(16));
        earlyPropertyNames.put("_focusrect", new Integer(17));
        earlyPropertyNames.put("_soundbuftime", new Integer(18));
        earlyPropertyNames.put("_quality", new Integer(19));
        earlyPropertyNames.put("_xmouse", new Integer(20));
        earlyPropertyNames.put("_ymouse", new Integer(21));

        propertyNames.put("_x", new Integer(0));
        propertyNames.put("_y", new Integer(1));
        propertyNames.put("_xscale", new Integer(2));
        propertyNames.put("_yscale", new Integer(3));
        propertyNames.put("_currentframe", new Integer(4));
        propertyNames.put("_totalframes", new Integer(5));
        propertyNames.put("_alpha", new Integer(6));
        propertyNames.put("_visible", new Integer(7));
        propertyNames.put("_width", new Integer(8));
        propertyNames.put("_height", new Integer(9));
        propertyNames.put("_rotation", new Integer(10));
        propertyNames.put("_target", new Integer(11));
        propertyNames.put("_framesloaded", new Integer(12));
        propertyNames.put("_name", new Integer(13));
        propertyNames.put("_droptarget", new Integer(14));
        propertyNames.put("_url", new Integer(15));
        propertyNames.put("_highquality", new Integer(16));
        propertyNames.put("_focusrect", new Integer(17));
        propertyNames.put("_soundbuftime", new Integer(18));
        propertyNames.put("_quality", new Integer(19));
        propertyNames.put("_xmouse", new Integer(20));
        propertyNames.put("_ymouse", new Integer(21));

        /*
         * The functions table is only used to identify built-in functions so no
         * value is associated with each name.
         */
        functions.put("call", new Boolean(false));
        functions.put("chr", new Boolean(true));
        functions.put("delete", new Boolean(false));
        functions.put("duplicatemovieclip", new Boolean(false));
        functions.put("eval", new Boolean(true));
        functions.put("fscommand", new Boolean(false));
        functions.put("getproperty", new Boolean(true));
        functions.put("gettimer", new Boolean(true));
        functions.put("geturl", new Boolean(false));
        functions.put("getversion", new Boolean(true));
        functions.put("gotoandplay", new Boolean(false));
        functions.put("gotoandstop", new Boolean(false));
        functions.put("ifframeloaded", new Boolean(false));
        functions.put("int", new Boolean(true));
        functions.put("loadmovie", new Boolean(false));
        functions.put("loadmovienum", new Boolean(false));
        functions.put("loadvariables", new Boolean(false));
        functions.put("length", new Boolean(true));
        functions.put("mbchr", new Boolean(true));
        functions.put("mbord", new Boolean(true));
        functions.put("mbsubstring", new Boolean(true));
        functions.put("nextframe", new Boolean(false));
        functions.put("nextscene", new Boolean(false));
        functions.put("number", new Boolean(false));
        functions.put("ord", new Boolean(true));
        functions.put("play", new Boolean(false));
        functions.put("prevframe", new Boolean(false));
        functions.put("prevscene", new Boolean(false));
        functions.put("print", new Boolean(false));
        functions.put("printnum", new Boolean(false));
        functions.put("printasbitmap", new Boolean(false));
        functions.put("printasbitmapnum", new Boolean(false));
        functions.put("random", new Boolean(true));
        functions.put("removemovieclip", new Boolean(false));
        functions.put("set", new Boolean(false));
        functions.put("setproperty", new Boolean(false));
        functions.put("startdrag", new Boolean(false));
        functions.put("stop", new Boolean(false));
        functions.put("stopallsounds", new Boolean(false));
        functions.put("stopdrag", new Boolean(false));
        functions.put("string", new Boolean(false));
        functions.put("substring", new Boolean(true));
        functions.put("targetpath", new Boolean(false));
        functions.put("telltarget", new Boolean(false));
        functions.put("togglehighquality", new Boolean(false));
        functions.put("trace", new Boolean(false));
        functions.put("typeof", new Boolean(true));
        functions.put("unloadmovie", new Boolean(false));
        functions.put("unloadmovienum", new Boolean(false));
        functions.put("void", new Boolean(true));

        /*
         * The functions table is only used to identify built-in functions that
         * return a value to determine whether a pop action should be generated
         * if the value returned by the function is not assigned to a variable.
         */
        valueFunctions.put("attachaudio", null);
        valueFunctions.put("attachmovie", null);
        valueFunctions.put("escape", null);
        valueFunctions.put("getbounds", null);
        valueFunctions.put("getbytesloaded", null);
        valueFunctions.put("getbytestotal", null);
        valueFunctions.put("getversion", null);
        valueFunctions.put("globaltolocal", null);
        valueFunctions.put("hittest", null);
        valueFunctions.put("isfinite", null);
        valueFunctions.put("isnan", null);
        valueFunctions.put("localtoglobal", null);
        valueFunctions.put("parsefloat", null);
        valueFunctions.put("parseint", null);
        valueFunctions.put("swapdepths", null);
        valueFunctions.put("targetpath", null);
        valueFunctions.put("unescape", null);
        valueFunctions.put("updateafterevent", null);

        classes.put("Math", null);
        classes.put("Clip", null);
    }

    private int type = 0;

    /*
     * Nodes may store integer, floating-point literals or the names of
     * functions, identifiers or string literals. Separate attributes are used
     * rather than an Object to avoid repeated class casting, improve
     * readability of code and increase performance.
     */
    private int iValue = 0;
    private double dValue = Double.NaN;
    private String sValue = null;
    private boolean bValue = false;

    /*
     * the discardValue flag is used to signal to a node that the value it
     * returns is not used by the parent and so a pop action should be added
     * when the node is translated into action objects. The discardValue flag is
     * set by the parent node through the discardValues() method.
     */
    private boolean discardValue = false;

    /*
     * insertIndex is used when reordering nodes so that function definitions
     * defined within a block are placed at the start - mirroring the behaviour
     * of the Flash authoring application. The index is used to preserve the
     * order in which functions are defined making regression testing easier
     * when comparing the code generated by a node against the code generated by
     * the Flash authoring application.
     */
    private int insertIndex = 0;

    private ASNode parent = null;
    private ASNode[] children = null;

    /*
     * The number attribute is used either to store the line number in a script
     * where the node was generated or an identifier for the node if a node tree
     * was created manually. The number is used when reporting errors while
     * validating the nodes.
     */
    private int number = 0;

    /**
     * Constructs an ASNode with the specified type.
     *
     * @param nodeType
     *            the type of node being constructed.
     */
    public ASNode(int nodeType) {
        type = nodeType;
    }

    /**
     * Constructs an ASNode with the specified type and integer value. This
     * constructor is primarily used to create nodes representing integer
     * literals.
     *
     * @param nodeType
     *            the type of node being constructed.
     * @param value
     *            the integer value assigned to the node.
     */
    public ASNode(int nodeType, int value) {
        type = nodeType;
        iValue = value;
    }

    /**
     * Constructs an ASNode with the specified type and floating-point value.
     * This constructor is primarily used to create nodes representing literals.
     *
     * @param nodeType
     *            the type of node being constructed.
     * @param value
     *            the floating-point value assigned to the node.
     */
    public ASNode(int nodeType, double value) {
        type = nodeType;
        dValue = value;
    }

    /**
     * Constructs an ASNode with the specified type and string value. This
     * constructor is primarily used to create string literals and identifiers.
     *
     * @param nodeType
     *            the type of node being constructed.
     * @param value
     *            the string assigned to the node.
     */
    public ASNode(int nodeType, String value) {
        type = nodeType;
        sValue = value;
    }

    /**
     * Constructs an ASNode with the specified type and adds the child node.
     *
     * @param nodeType
     *            the type of node being constructed.
     * @param node
     *            a child node which will be added to the new node.
     */
    public ASNode(int nodeType, ASNode node) {
        type = nodeType;

        add(node);
    }

    /**
     * Constructs an ASNode with the specified type and adds the child nodes.
     *
     * @param nodeType
     *            the type of node being constructed.
     * @param node1
     *            a child node which will be added to the new node.
     * @param node2
     *            a child node which will be added to the new node.
     */
    public ASNode(int nodeType, ASNode node1, ASNode node2) {
        type = nodeType;

        add(node1);
        add(node2);
    }

    /**
     * Gets the type of the node.
     *
     * @return the type assigned to the node.
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the type of the node.
     *
     * @param type
     *            the type assigned to the node.
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Get the boolean value assigned to a node.
     *
     * @return the boolean value assigned to a node.
     */
    public boolean getBoolValue() {
        return bValue;
    }

    /**
     * Set the boolean value assigned to a node.
     *
     * @param value
     *            a value that will be assigned to the node.
     */
    public void setBoolValue(boolean value) {
        bValue = value;
        iValue = 0;
        dValue = Double.NaN;
        sValue = null;
    }

    /**
     * Get the integer value assigned to a node.
     *
     * @return the integer value assigned to a node.
     */
    public int getIntValue() {
        return iValue;
    }

    /**
     * Set the integer value assigned to a node.
     *
     * @param value
     *            a value that will be assigned to the node.
     */
    public void setIntValue(int value) {
        bValue = false;
        iValue = value;
        dValue = Double.NaN;
        sValue = null;
    }

    /**
     * Get the floating-point value assigned to a node.
     *
     * @return the floating-point value assigned to a node.
     */
    public double getDoubleValue() {
        return dValue;
    }

    /**
     * Set the floating-point value assigned to a node.
     *
     * @param value
     *            a floating-point value that will be assigned to the node.
     */
    public void setDoubleValue(double value) {
        bValue = false;
        iValue = 0;
        dValue = value;
        sValue = null;
    }

    /**
     * Get the string value assigned to a node.
     *
     * @return the string value assigned to a node.
     */
    public String getStringValue() {
        return sValue;
    }

    /**
     * Set the number assigned to a node.
     *
     * @param value
     *            a unique number that will be assigned to the node.
     */
    public void setNumber(int value) {
        number = value;
    }

    /**
     * Get the number assigned to a node.
     *
     * @return the number assigned to a node.
     */
    public int getNumber() {
        return number;
    }

    /**
     * Set the string value assigned to a node.
     *
     * @param value
     *            a string that will be assigned to the node.
     */
    public void setStringValue(String value) {
        bValue = false;
        iValue = 0;
        dValue = Double.NaN;
        sValue = value;
    }

    /**
     * Returns the node at the specified index from the array of child nodes. If
     * the index is outside the range of the array then an ArrayIndexOutOfBounds
     * exception is thrown.
     *
     * @param index
     *            the index of the child node to return.
     * @return the ith node in the array of children.
     * @throws ArrayIndexOutOfBoundsException
     *             if (index < 0 || index >= length).
     */
    public ASNode get(int index) {
        if (children == null || index < 0 || index >= children.length)
            throw new ArrayIndexOutOfBoundsException(index);

        return children[index];
    }

    /**
     * Replaces the node at position i in the array of children. If the position
     * is outside the range of the array (i< 0 || i >= length) then an
     * ArrayIndexOutOfBoundsException is thrown.
     *
     * @param i
     *            the index of the child node to replace.
     * @param aNode
     *            the node to replace the ith node.
     * @throws ArrayIndexOutOfBoundsException
     *             if (index < 0 || index >= length).
     */
    public void set(int i, ASNode aNode) {
        if (aNode != null && children != null) {
            if (i < 0 || i >= children.length)
                throw new ArrayIndexOutOfBoundsException(i);

            aNode.parent = this;
            children[i] = aNode;
        }
    }

    /**
     * Adds a node to the array of children. If the node is null then it is
     * ignored.
     *
     * @param aNode
     *            the node to be added.
     */
    public void add(ASNode aNode) {
        if (aNode != null) {
            aNode.parent = this;

            if (children == null) {
                children = new ASNode[1];
            } else {
                ASNode c[] = new ASNode[children.length + 1];
                System.arraycopy(children, 0, c, 0, children.length);
                children = c;
            }
            children[children.length - 1] = aNode;
        }
    }

    /**
     * Inserts a node at position i in the array of children. The size of the
     * array is increased by one and the nodes from the insertion point onwards
     * are moved to the right.
     *
     * If the position is outside the range of the array (i< 0 || i >= length)
     * then an ArrayIndexOutOfBoundsException is thrown.
     *
     * @param index
     *            the index of the child node to replace.
     * @param aNode
     *            the node to replace the ith node.
     * @throws ArrayIndexOutOfBoundsException
     *             if (index < 0 || index >= length).
     */
    public void insert(int index, ASNode aNode) {
        if (children == null || index < 0 || index >= children.length)
            throw new ArrayIndexOutOfBoundsException(index);

        aNode.parent = this;

        ASNode c[] = new ASNode[children.length + 1];

        for (int i = 0; i < index; i++)
            c[i] = children[i];

        c[index] = aNode;

        for (int i = index; i < children.length; i++)
            c[i + 1] = children[i];

        children = c;
    }

    /**
     * Removes the node at position i in the array of children. The size of the
     * array is decreased by one and the nodes from the insertion point onwards
     * are moved to the left.
     *
     * If the position is outside the range of the array (i< 0 || i >= length)
     * then an ArrayIndexOutOfBoundsException is thrown.
     *
     * @param index
     *            the index of the child node to remove.
     * @throws ArrayIndexOutOfBoundsException
     *             if (index < 0 || index >= length).
     */
    public void remove(int index) {
        if (children == null || index < 0 || index >= children.length)
            throw new ArrayIndexOutOfBoundsException(index);

        children[index].parent = null;
        children[index] = null;

        ASNode c[] = new ASNode[children.length - 1];

        for (int i = 0, j = 0; i < children.length; i++) {
            if (children[i] != null)
                c[j++] = children[i];
        }
        children = c;
    }

    /**
     * Returns the index position of a node in the array of child nodes. If the
     * node is not one of the current nodes children then -1 is returned.
     *
     * @param aNode
     *            the node to search the array of children for.
     *
     * @return the index of the node in the array of children, -1 if the node is
     *         not a child of this node.
     */
    public int indexOf(ASNode aNode) {
        int index = -1;

        for (int i = 0; i < children.length; i++)
            if (children[i].equals(aNode))
                index = i;

        return index;
    }

    /**
     * Gets the parent node of this one. If no parent is define then null is
     * returned.
     *
     * @return the parent node of this one.
     */
    public ASNode getParent() {
        return parent;
    }

    /**
     * Return the number of child nodes contained by this node.
     *
     * @return the number of child nodes.
     */
    public int count() {
        return (children == null) ? 0 : children.length;
    }

    /**
     * Returns a string containing the type of node, any associated value and
     * the number of children.
     *
     * @return the string representation of the node.
     */
    @Override
    public String toString() {
        String str = nodeNames[type];

        if (type == BooleanLiteral) {
            str = str + " = " + (bValue ? "true" : "false") + "; ";
        } else if (type == IntegerLiteral) {
            str = str + " = " + iValue + "; ";
        } else if (type == DoubleLiteral) {
            str = str + " = " + dValue + "; ";
        } else if (type == StringLiteral) {
            str = str + " = " + sValue + "; ";
        } else if (type == NullLiteral) {
            str = str + " = null; ";
        } else if (sValue != null) {
            str = str + " = " + sValue + "; ";
        }
        return str;
    }

    /**
     * displayTree is used to display the structure of the node tree, with the
     * root starting at the current node. The prefix argument is used to indent
     * the text displayed. The level of indent is increased by appending the
     * string "  " before calling the displayTree method on each child node.
     * This illustrates the tree structure with nodes at the same level in the
     * tree displayed with the same level of indent.
     *
     * @param prefix
     *            the string prepended to the text representation for this node.
     */
    public void displayTree(String prefix) {
        int count = count();

        System.out.println(prefix + toString());

        for (int i = 0; i < count; i++)
            children[i].displayTree(prefix + "  ");
    }

    /*
     * Translates the array of nodes into the actions that will be executed by
     * the Flash Player. The version of Flash for which the actions are
     * generated is specified to ensure compatibility with future release of
     * Flash. IMPORTANT: The programming model changed with Flash version 5 to
     * support stack-based actions. Earlier versions of Flash are not support.
     * An IllegalArgumentException will be thrown if the version is earlier than
     * 5.
     * @param version the version of Flash that control the actions that are
     * generated.
     * @param encoding the character set used to represent the strings parsed in
     * the script.
     */
    public List<Action> compile(ASContext info) {
        ArrayList<Action> array = new ArrayList<Action>();

        reorder(info);
        findStrings(info);

        if (type == Array || type == Button || type == MovieClip) {
            generateScript(info, array);
        }

        return array;
    }

    private void generateScript(ASContext info, List<Action> list) {
        int count = count();

        switch (type) {
        case Array:
            if (type == Array && info.useStrings)
                list.add(new Table(info.strings));

            for (int i = 0; i < count; i++)
                children[i].discardValues();

            for (int i = 0; i < count; i++)
                children[i].generate(info, list);

            list.add(BasicAction.END);
            break;
        case Button:
        case MovieClip:
            for (int i = 0; i < count; i++)
                ((ASEventNode)children[i]).generateEvent(info, list);
            break;
        default:
            break;
        }
    }

    /*
     * reorder is used to restructure the node tree and individual nodes to
     * simplify the generation of the action objects that represent the
     * 'compiled' version of an ActionScript program.
     */
    private void reorder(ASContext info) {
        switch (type) {
        case Array:
        case Button:
        case MovieClip:
            info.nodes.push(this);
            break;
        case Identifier:
            if (constants.containsKey(sValue)) {
                type = Identifier;
                Object value = constants.get(sValue);

                if (value != null && value instanceof String) {
                    type = StringLiteral;
                    sValue = value.toString();
                }
            }
            break;
        case Value:
            if (children[0].type == Identifier && children[1].type == Attribute) {
                String name = children[0].sValue + "." + children[1].sValue;

                if (constants.containsKey(name)) {
                    type = Identifier;
                    sValue = name;

                    remove(0);
                    remove(0);
                }
            }
            break;
        case Assign:
            if (parent != null && parent.type == List && parent.count() > 0) {
                if (parent.children[0].count() > 0) {
                    if (parent.children[0].children[0].type == DefineVariable) {
                        if (parent.indexOf(this) != 0)
                            children[0].type = DefineVariable;
                    }
                }
            }
            break;
        case DefineFunction:
            ASNode node = info.nodes.peek();

            node.insert(node.insertIndex++, this);

            int index = parent.indexOf(this);

            if (index != -1)
                parent.remove(index);

            info.nodes.push(this);
            break;
        case Function:
            if (sValue.equals("fscommand")) {
                if (children[0].type == StringLiteral)
                    children[0].sValue = "FSCommand:" + children[0].sValue;
            } else if (sValue.equals("print")) {
                ASNode c0 = children[0];
                ASNode c1 = children[1];

                if (children[1].sValue.equals("bmovie"))
                    children[1].sValue = "print:";
                else
                    children[1].sValue = "print:#" + children[1].sValue;

                children[0] = c1;
                children[1] = c0;
            } else if (sValue.equals("printNum")) {
                if (children[0].type == IntegerLiteral) {
                    children[0].type = StringLiteral;
                    children[0].setStringValue("_level" + children[0].iValue);
                }
                if (children[1].sValue.equals("bmovie")) {
                    children[1].sValue = "print:";
                } else {
                    children[1].sValue = "print:#" + children[1].sValue;
                }
            } else if (sValue.equals("printAsBitmap")) {
                ASNode c0 = children[0];
                ASNode c1 = children[1];

                if (children[1].sValue.equals("bmovie"))
                    children[1].sValue = "printasbitmap:";
                else
                    children[1].sValue = "printasbitmap:#" + children[1].sValue;

                children[0] = c1;
                children[1] = c0;
            } else if (sValue.equals("printAsBitmapNum")) {
                if (children[0].type == IntegerLiteral) {
                    children[0].type = StringLiteral;
                    children[0].setStringValue("_level" + children[0].iValue);
                }
                if (children[1].sValue.equals("bmovie"))
                    children[1].sValue = "printasbitmap:";
                else
                    children[1].sValue = "printasbitmap:#" + children[1].sValue;
            }
            break;
        }

        /*
         * reorder any child nodes before reordering any binary operators to
         * ensure any interger literals are evaluated first.
         */
        int count = count();

        for (int i = 0; i < count; i++)
            children[i].reorder(info);

        switch (type) {
        case Array:
        case Add:
        case Sub:
        case Mul:
        case Div:
        case Mod:
            if (count() == 2) {
                if (children[0].getType() == IntegerLiteral
                        && children[1].getType() == IntegerLiteral) {
                    switch (type) {
                    case Add:
                        type = IntegerLiteral;
                        iValue = children[0].iValue + children[1].iValue;
                        break;
                    case Sub:
                        type = IntegerLiteral;
                        iValue = children[0].iValue - children[1].iValue;
                        break;
                    case Mul:
                        type = IntegerLiteral;
                        iValue = children[0].iValue * children[1].iValue;
                        break;
                    case Div:
                        if (children[0].iValue / children[1].iValue == 0) {
                            type = DoubleLiteral;
                            dValue = ((double) children[0].iValue)
                                    / ((double) children[1].iValue);
                        } else if (children[0].iValue % children[1].iValue != 0) {
                            type = DoubleLiteral;
                            dValue = ((double) children[0].iValue)
                                    / ((double) children[1].iValue);
                        } else {
                            type = IntegerLiteral;
                            iValue = children[0].iValue / children[1].iValue;
                        }
                        break;
                    case Mod:
                        type = IntegerLiteral;
                        iValue = children[0].iValue % children[1].iValue;
                        break;
                    }
                    remove(0);
                    remove(0);
                } else if (children[0].getType() == DoubleLiteral
                        && children[1].getType() == IntegerLiteral) {
                    switch (type) {
                    case Add:
                        dValue = children[0].dValue + children[1].iValue;
                        break;
                    case Sub:
                        dValue = children[0].dValue - children[1].iValue;
                        break;
                    case Mul:
                        dValue = children[0].dValue * children[1].iValue;
                        break;
                    case Div:
                        dValue = children[0].dValue / children[1].iValue;
                        break;
                    case Mod:
                        dValue = children[0].dValue % children[1].iValue;
                        break;
                    }
                    type = DoubleLiteral;
                    remove(0);
                    remove(0);
                } else if (children[0].getType() == IntegerLiteral
                        && children[1].getType() == DoubleLiteral) {
                    switch (type) {
                    case Add:
                        dValue = children[0].iValue + children[1].dValue;
                        break;
                    case Sub:
                        dValue = children[0].iValue - children[1].dValue;
                        break;
                    case Mul:
                        dValue = children[0].iValue * children[1].dValue;
                        break;
                    case Div:
                        dValue = children[0].iValue / children[1].dValue;
                        break;
                    case Mod:
                        dValue = children[0].iValue % children[1].dValue;
                        break;
                    }
                    type = DoubleLiteral;
                    remove(0);
                    remove(0);
                } else if (children[0].getType() == DoubleLiteral
                        && children[1].getType() == DoubleLiteral) {
                    switch (type) {
                    case Add:
                        dValue = children[0].dValue + children[1].dValue;
                        break;
                    case Sub:
                        dValue = children[0].dValue - children[1].dValue;
                        break;
                    case Mul:
                        dValue = children[0].dValue * children[1].dValue;
                        break;
                    case Div:
                        dValue = children[0].dValue / children[1].dValue;
                        break;
                    case Mod:
                        dValue = children[0].dValue % children[1].dValue;
                        break;
                    }
                    type = DoubleLiteral;
                    remove(0);
                    remove(0);
                } else if (children[0].getType() == StringLiteral
                        || children[1].getType() == StringLiteral) {
                    String aValue = null;
                    String bValue = null;
                    switch (type) {
                    case Add:
                        if (children[0].getType() == StringLiteral) {
                            aValue = children[0].sValue;
                        } else if (children[0].getType() == IntegerLiteral) {
                            aValue = String.valueOf(children[0].iValue);
                        }
                        if (children[1].getType() == StringLiteral) {
                            bValue = children[1].sValue;
                        } else if (children[1].getType() == IntegerLiteral) {
                            bValue = String.valueOf(children[1].iValue);
                        }
                        break;
                    }
                    if (aValue != null && bValue != null) {
                        sValue = aValue + bValue;
                        type = StringLiteral;
                        remove(0);
                        remove(0);
                    }
                }
            }
            break;
        case ASR:
        case LSL:
        case LSR:
        case BitAnd:
        case BitOr:
        case BitXOr:
            if (count() == 2) {
                if (children[0].getType() == IntegerLiteral
                        && children[1].getType() == IntegerLiteral) {
                    switch (type) {
                    case ASR:
                        iValue = children[0].iValue >> children[1].iValue;
                        break;
                    case LSL:
                        iValue = children[0].iValue << children[1].iValue;
                        break;
                    case LSR:
                        iValue = children[0].iValue >>> children[1].iValue;
                        break;
                    case BitAnd:
                        iValue = children[0].iValue & children[1].iValue;
                        break;
                    case BitOr:
                        iValue = children[0].iValue | children[1].iValue;
                        break;
                    case BitXOr:
                        iValue = children[0].iValue ^ children[1].iValue;
                        break;
                    }
                    type = IntegerLiteral;
                    remove(0);
                    remove(0);
                }
            }
            break;

        case And:
        case LogicalAnd:
            if (count() == 2) {
//                if (children[0].getType() == IntegerLiteral) {
//                    children[0].type = BooleanLiteral;
//                    children[0].bValue = children[0].iValue != 0;
//                    children[0].iValue = 0;
//                }
//                if (children[1].getType() == IntegerLiteral) {
//                    children[1].type = BooleanLiteral;
//                    children[1].bValue = children[1].iValue != 0;
//                    children[1].iValue = 0;
//                }
//                if (children[0].getType() == DoubleLiteral) {
//                    children[0].type = BooleanLiteral;
//                    children[0].bValue = children[0].dValue != 0.0;
//                    children[0].dValue = 0.0;
//                }
//                if (children[1].getType() == DoubleLiteral) {
//                    children[1].type = BooleanLiteral;
//                    children[1].bValue = children[1].dValue != 0;
//                    children[1].dValue = 0.0;
//                }

                if (children[0].getType() == BooleanLiteral
                        && children[1].getType() == BooleanLiteral) {
                    switch (type) {
                    case LogicalAnd:
                    case And:
                        type = BooleanLiteral;
                        bValue = children[0].bValue && children[1].bValue;
                        break;
                    }
                    remove(0);
                    remove(0);
                } else if (children[0].getType() == BooleanLiteral
                        && children[1].getType() == IntegerLiteral) {
                    switch (type) {
                    case And:
                    case LogicalAnd:
                        type = BooleanLiteral;
                        bValue = children[0].bValue
                                && (children[1].iValue != 0);
                        break;
                    }
                    remove(0);
                    remove(0);
                } else if (children[0].getType() == IntegerLiteral
                        && children[1].getType() == BooleanLiteral) {
                    switch (type) {
                    case LogicalAnd:
                    case And:
                        type = BooleanLiteral;
                        bValue = (children[0].iValue != 0)
                                && children[1].bValue;
                        break;
                    }
                    remove(0);
                    remove(0);
                } else if (children[0].getType() == IntegerLiteral
                        && children[1].getType() == IntegerLiteral) {
                    boolean a = children[0].iValue != 0;
                    boolean b = children[1].iValue != 0;

                    switch (type) {
                    case LogicalAnd:
                    case And:
                        type = IntegerLiteral;
                        iValue = a ? children[1].iValue : 0;
                        break;
                    }
                    remove(0);
                    remove(0);
                }
            }
            break;
        case Or:
        case LogicalOr:
            if (count() == 2) {
                if (children[0].getType() == BooleanLiteral
                        && children[1].getType() == BooleanLiteral) {
                    switch (type) {
                    case LogicalOr:
                    case Or:
                        type = BooleanLiteral;
                        bValue = children[0].bValue || children[1].bValue;
                        break;
                    }
                    remove(0);
                    remove(0);
                } else if (children[0].getType() == BooleanLiteral
                        && children[1].getType() == IntegerLiteral) {
                    switch (type) {
                    case LogicalOr:
                    case Or:
                        type = IntegerLiteral;
                        iValue = children[1].iValue;
                        break;
                    }
                    remove(0);
                    remove(0);
                } else if (children[0].getType() == IntegerLiteral
                        && children[1].getType() == BooleanLiteral) {
                    switch (type) {
                    case LogicalOr:
                    case Or:
                        type = IntegerLiteral;
                        iValue = ((children[0].iValue != 0) || children[1].bValue) ? 1
                                : 0;
                        break;
                    }
                    remove(0);
                    remove(0);
                } else if (children[0].getType() == IntegerLiteral
                        && children[1].getType() == IntegerLiteral) {
                    boolean a = children[0].iValue != 0;
                    boolean b = children[1].iValue != 0;

                    switch (type) {
                    case LogicalOr:
                    case Or:
                        type = IntegerLiteral;
                        iValue = a || b ? 1 : 0;
                        break;
                    }
                    remove(0);
                    remove(0);
                }
            }
            break;
        case Not:
            if (count() == 1) {
                if (children[0].getType() == BooleanLiteral) {
                    type = BooleanLiteral;
                    bValue = !children[0].bValue;
                    remove(0);
                } else if (children[0].getType() == IntegerLiteral) {
                    type = BooleanLiteral;
                    bValue = children[0].iValue == 0;
                    remove(0);
                }
            }
            break;
        case BitNot:
            if (count() == 1) {
                if (children[0].getType() == IntegerLiteral) {
                    type = IntegerLiteral;
                    iValue = ~children[0].iValue;
                    remove(0);
                }
            }
            break;
        default:
            break;
        }

        switch (type) {
        case Array:
        case Button:
        case MovieClip:
        case DefineFunction:
            info.nodes.pop();
            break;
        default:
            break;
        }
    }

    /*
     * findStrings is used to generate a table of string literals so rather than
     * pushing a string directly onto the Flash Player's stack an index into the
     * table is used instead. This reduces the encoded file size if the string
     * is used more than once.
     * @param info is an ASContext object that passes context information between
     * nodes.
     */
    private void findStrings(ASContext info) {
        int count = count();

        if (type == Function)
            info.context.push(sValue);
        else
            info.context.push(nodeNames[type]);

        switch (type) {
//        case On:
//        case OnClipEvent:
//            info.clearStrings();
//            for (int i = 0; i < count; i++)
//                children[i].findStrings(info);
//            break;
        case StringLiteral:
            sValue = sValue.replaceAll("\\\\n", "\n");
            sValue = sValue.replaceAll("\\\\t", "\t");
            sValue = sValue.replaceAll("\\\\b", "\b");
            sValue = sValue.replaceAll("\\\\r", "\r");
            sValue = sValue.replaceAll("\\\\f", "\f");

            info.addString(sValue);
            break;
        case Identifier:
            if (constants.containsKey(sValue))
                break;
            else if (propertyNames.containsKey(sValue)) {
                if (info.context.contains("getProperty"))
                    break;
                else if (info.context.contains("setProperty"))
                    break;
                else if (info.context.contains("With"))
                    info.addString(sValue);
                else if (info.context.contains("Define Object"))
                    info.addString(sValue);
                else
                    info.addString("");
                break;
            }
            info.addString(sValue);
            break;
        case DefineVariable:
            info.addString(sValue);
            break;
        case Attribute:
        case Method:
        case NewObject:

            for (int i = count - 1; i >= 0; i--)
                children[i].findStrings(info);

            if (sValue.length() > 0)
                info.addString(sValue);
            break;
        case Function:
            if (sValue != null && functions.containsKey(sValue.toLowerCase()) == false) {
                for (int i = 0; i < count; i++)
                    children[i].findStrings(info);

                if (sValue.length() > 0)
                    info.addString(sValue);
            } else {
                if (sValue != null && sValue.toLowerCase().equals("fscommand")) {
                    info.addString("FSCommand:");

                    for (int i = 0; i < count; i++)
                        children[i].findStrings(info);
                } else if (sValue != null && sValue.toLowerCase().equals("getURL")) {
                    if (count > 0)
                        children[0].findStrings(info);

                    if (count > 1)
                        children[1].findStrings(info);

                    if (count == 1 && children[0].type != StringLiteral)
                        info.addString("");

                    break;
                } else if (sValue != null && sValue.toLowerCase().equals("gotoAndPlay")) {
                    if (count == 1)
                        children[0].findStrings(info);
                    else if (count == 2)
                        children[1].findStrings(info);

                    break;
                } else if (sValue != null && sValue.toLowerCase().equals("gotoAndStop")) {
                    if (count == 1)
                        children[0].findStrings(info);
                    else if (count == 2)
                        children[1].findStrings(info);

                    break;
                } else if (sValue != null && sValue.toLowerCase().equals("loadMovie")) {
                    if (count > 0)
                        children[0].findStrings(info);

                    if (count > 1)
                        children[1].findStrings(info);

                    if (count == 1)
                        info.addString("");

                    break;
                } else if (sValue != null && sValue.toLowerCase().equals("loadVariables")) {
                    if (count > 0)
                        children[0].findStrings(info);

                    if (count > 1)
                        children[1].findStrings(info);

                    if (count == 1)
                        info.addString("");

                    break;
                } else if (sValue != null && sValue.toLowerCase().equals("printNum")) {
                    children[1].findStrings(info);

                    if (children[0].type == Identifier)
                        info.addString("_level");

                    children[0].findStrings(info);
                } else if (sValue != null && sValue.toLowerCase().equals("printAsBitmapNum")) {
                    children[1].findStrings(info);

                    if (children[0].type == Identifier)
                        info.addString("_level");

                    children[0].findStrings(info);
                } else {
                    for (int i = 0; i < count; i++)
                        children[i].findStrings(info);
                }
            }
            break;
        case DefineMethod:
            children[count - 1].findStrings(info);
            break;
        case DefineFunction:
            if (sValue != null && sValue.equals("ifFrameLoaded")) {
                if (children[0].count() == 0) {
                    children[0].findStrings(info);
                } else if (children[0].count() == 2) {
                    children[0].children[1].findStrings(info);
                }
            }
            children[count - 1].findStrings(info);
            break;
        case DefineArray:
            for (int i = count - 1; i >= 0; i--)
                children[i].findStrings(info);
            break;
        case Value:
            if (count > 0) {
                if (children[0].sValue != null
                        && classes.containsKey(children[0].sValue)) {
                    boolean containsClass = false;

                    for (Iterator<String> i = info.strings.iterator(); i
                            .hasNext();) {
                        if (i.next().toString().equals(children[0].sValue)) {
                            containsClass = true;
                            break;
                        }
                    }
                    // Swap the name of the function and the class to
                    // simplify verification during testing.

                    if (containsClass == false) {
                        int index = info.strings.size();

                        for (int i = 0; i < count; i++)
                            children[i].findStrings(info);

                        info.strings.set(index, info.strings.get(index + 1));
                        info.strings.set(index + 1, children[0].sValue);
                    } else {
                        for (int i = 0; i < count; i++)
                            children[i].findStrings(info);
                    }
                } else {
                    for (int i = 0; i < count; i++)
                        children[i].findStrings(info);
                }
            }
            break;
        default:
            for (int i = 0; i < count; i++)
                children[i].findStrings(info);
            break;
        }
        info.context.pop();
    }

    /*
     * generate 'compiles' ActionScript statements that this node and all child
     * nodes represent into the set of actions that will be executed by the
     * Flash Player.
     * @param info an ASContext object that is used to pass context and context
     * information between nodes. This should be the same object used when
     * preprocessing modes.
     * @param actions an array that the compiled actions will be added to.
     */
    protected void generate(ASContext info, List<Action> actions) {
        if (type == Function)
            info.context.push(sValue);
        else
            info.context.push(nodeNames[type]);

        switch (type) {
        case Array:
        case Button:
        case MovieClip:
            //generateScript(info, actions);
            break;
        case StatementList:
        case List:
            generateList(info, actions);
            break;
        case If:
            generateIf(info, actions);
            break;
        case Do:
            generateDo(info, actions);
            break;
        case While:
            generateWhile(info, actions);
            break;
        case For:
            generateFor(info, actions);
            break;
        case ForIn:
            generateForIn(info, actions);
            break;
        case With:
            generateWith(info, actions);
            break;
        case Switch:
            generateSwitch(info, actions);
            break;
        case Label:
            generateLabel(info, actions);
            break;
        case Exception:
            generateException(info, actions);
            break;
        case Try:
        case Catch:
        case Finally:
            generateClauses(info, actions);
            break;
//        case OnClipEvent:
//            generateOnClipEvent(info, actions);
//            break;
//        case On:
//            generateOn(info, actions);
//            break;
        case Break:
        case Continue:
        case Return:
            generateReturn(info, actions);
            break;
        case Value:
        case BooleanLiteral:
        case IntegerLiteral:
        case DoubleLiteral:
        case StringLiteral:
        case NullLiteral:
        case Identifier:
        case Attribute:
        case Method:
        case NewObject:
        case Subscript:
            generateValue(info, actions);
            break;
        case Function:
            generateFunction(actions, info, sValue);
            break;
        case DefineArray:
        case DefineObject:
        case DefineFunction:
        case DefineMethod:
        case DefineAttribute:
        case DefineVariable:
            generateDefinition(info, actions);
            break;
        case PreInc:
        case PreDec:
        case PostInc:
        case PostDec:
        case Plus:
        case Minus:
        case Not:
        case BitNot:
        case Delete:
        case Throw:
            generateUnary(info, actions);
            break;
        case StringAdd:
        case Add:
        case Sub:
        case Mul:
        case Div:
        case Mod:
        case BitAnd:
        case BitOr:
        case BitXOr:
        case LSL:
        case LSR:
        case ASR:
        case Equal:
        case NotEqual:
        case LessThan:
        case GreaterThan:
        case LessThanEqual:
        case GreaterThanEqual:
        case StringEqual:
        case StringNotEqual:
        case StringGreaterThan:
        case StringLessThanEqual:
        case StringGreaterThanEqual:
        case And:
        case Or:
        case LogicalAnd:
        case LogicalOr:
        case InstanceOf:
        case StrictEqual:
        case StrictNotEqual:
            generateBinary(info, actions);
            break;
        case Select:
            generateSelect(info, actions);
            break;
        case Assign:
        case AssignAdd:
        case AssignSub:
        case AssignMul:
        case AssignDiv:
        case AssignMod:
        case AssignBitAnd:
        case AssignBitOr:
        case AssignBitXOr:
        case AssignLSL:
        case AssignLSR:
        case AssignASR:
            generateAssignment(info, actions);
            break;
        default:
            break;
        }
        info.context.pop();
    }

    private void generateList(ASContext info, List<Action> actions) {
        int count = count();

        for (int i = 0; i < count; i++)
            children[i].generate(info, actions);
    }

    private void generateIf(ASContext info, List<Action> actions) {
        int count = count();
        boolean addJump = false;

        List<Action> trueActions = new ArrayList<Action>();
        int offsetToNext = 0;

        List<Action> falseActions = new ArrayList<Action>();
        int offsetToEnd = 0;

        if (count > 1) {
            children[1].discardValues();
            children[1].generate(info, trueActions);
            offsetToNext = actionLength(trueActions, info);
        }

        if (count == 3) {
            children[2].discardValues();
            children[2].generate(info, falseActions);
            offsetToEnd = actionLength(falseActions, info);

            addJump = offsetToEnd != 0;
        }

        if (trueActions.size() > 0) {
            SWFEncodeable action = (trueActions.get(trueActions.size() - 1));

            if (action instanceof ActionObject) {
                ActionObject actionObj = (ActionObject) action;
                if (actionObj.getType() == 256 || actionObj.getType() == 257) {
                    addJump = true;

                    if (falseActions.size() == 0)
                        offsetToNext -= 5;
                }
            }
        }

        // Special case
        if (count == 3 && trueActions.isEmpty() && falseActions.isEmpty()) {
            trueActions.add(new Jump(0));
            offsetToNext = 0;
            addJump = true;
        }

        if (addJump)
            offsetToNext += 5; // Length of jump tag

        children[0].generate(info, actions);

        actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));
        actions.add(new If(offsetToNext));

        actions.addAll(trueActions);

        if (addJump == true && offsetToEnd > 0)
            actions.add(new Jump(offsetToEnd));

        actions.addAll(falseActions);
    }

    private void generateDo(ASContext info, List<Action> actions) {
        ArrayList<Action> blockActions = new ArrayList<Action>();
        int blockLength = 0;

        ArrayList<Action> conditionActions = new ArrayList<Action>();
        int conditionLength = 0;

        children[0].discardValues();
        children[0].generate(info, blockActions);

        children[1].generate(info, conditionActions);

        blockLength = actionLength(blockActions, info);
        conditionLength = actionLength(conditionActions, info);

        conditionLength += 5; // include following if statement

        conditionActions.add(new If(-(blockLength + conditionLength))); // includes
                                                                        // if

        int currentLength = 0;

        // Replace any break and continue place holders with jump statements.

        for (int i = 0; i < blockActions.size(); i++) {
            Action currentAction = blockActions.get(i);

            currentLength += currentAction.prepareToEncode(info);

            if (currentAction instanceof ActionObject) {
                ActionObject actionObj = (ActionObject) currentAction;
                if (actionObj.getType() == 256) {
                    blockActions.set(i, new Jump(blockLength - currentLength
                            + conditionLength));
                }

                if (actionObj.getType() == 257) {
                    blockActions.set(i, new Jump(blockLength - currentLength));
                }
            }
        }
        actions.addAll(blockActions);
        actions.addAll(conditionActions);
    }

    private void generateWhile(ASContext info, List<Action> actions) {
        int count = (children != null) ? children.length : 0;

        ArrayList<Action> blockActions = new ArrayList<Action>();
        int blockLength = 0;

        ArrayList<Action> conditionActions = new ArrayList<Action>();
        int conditionLength = 0;

        if (count == 2) {
            children[1].discardValues();
            children[1].generate(info, blockActions);
        }
        blockLength = actionLength(blockActions, info);

        children[0].generate(info, conditionActions);
        conditionActions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));
        conditionActions.add(new If(blockLength + 5)); // includes loop jump
        conditionLength = actionLength(conditionActions, info);

        blockActions.add(new Jump(-(conditionLength + blockLength + 5)));
        blockLength += 5;

        int currentLength = conditionLength;

        // Replace any break and continue place holders with jump statements.

        for (int i = 0; i < blockActions.size(); i++) {
            Action currentAction = blockActions.get(i);

            currentLength += currentAction.prepareToEncode(info);

            if (currentAction instanceof ActionObject) {
                ActionObject actionObject = (ActionObject) currentAction;
                if (actionObject.getType() == 256)
                    blockActions.set(i, new Jump(
                            (blockLength + conditionLength) - currentLength));

                if (actionObject.getType() == 257)
                    blockActions.set(i, new Jump(-currentLength));
            }
        }
        actions.addAll(conditionActions);
        actions.addAll(blockActions);
    }

    private void generateFor(ASContext info, List<Action> actions) {
        ArrayList<Action> initializeActions = new ArrayList<Action>();
        ArrayList<Action> conditionActions = new ArrayList<Action>();
        ArrayList<Action> iteratorActions = new ArrayList<Action>();
        ArrayList<Action> blockActions = new ArrayList<Action>();

        //int initializeLength = 0;
        int conditionLength = 0;
        int blockLength = 0;
        int iteratorLength = 0;

        if (children[0].type != NoOp) {
            children[0].generate(info, initializeActions);
            //initializeLength = actionLength(initializeActions, info);
        }
        if (children[1].type != NoOp) {
            children[1].generate(info, conditionActions);
            conditionLength = actionLength(conditionActions, info);
        }
        if (children[2].type != NoOp) {
            children[2].discardValues();
            children[2].generate(info, iteratorActions);
            iteratorLength = actionLength(iteratorActions, info);
        }
        if (children[3].type != NoOp) {
            children[3].discardValues();
            children[3].generate(info, blockActions);
            blockLength = actionLength(blockActions, info);
        }

        // Add the if test with jump to end if false. Jump include block and
        // iterator actions plus a jump at the end to go back to the condition
        // actions

        if (conditionActions.size() > 0) {
            Action lastAction = conditionActions
                    .get(conditionActions.size() - 1);

            if (lastAction instanceof Push) {
                List<Object> values = ((Push) lastAction).getValues();
                int lastIndex = values.size() - 1;
                Object lastValue = values.get(lastIndex);

                if (lastValue instanceof Boolean) {
                    if (((Boolean) lastValue).booleanValue()) {
                        values.set(lastIndex, new Boolean(false));
                        conditionActions.add(new If(blockLength
                                + iteratorLength + 5));
                        conditionLength += 5;
                    }
                } else if (lastValue instanceof Integer) {
                    if (((Integer) lastValue).intValue() > 0) {
                        values.set(lastIndex, new Integer(0));
                        conditionActions.add(new If(blockLength
                                + iteratorLength + 5));
                        conditionLength += 5;
                    }
                } else if (lastValue instanceof Double) {
                    if (((Double) lastValue).doubleValue() > 0.0) {
                        values.set(lastIndex, new Double(0));
                        conditionActions.add(new If(blockLength
                                + iteratorLength + 5));
                        conditionLength += 5;
                    }
                } else if (lastValue instanceof String) {
                    if (((String) lastValue).equals("0") == false) {
                        values.set(lastIndex, "0");
                        conditionActions.add(new If(blockLength
                                + iteratorLength + 5));
                        conditionLength += 5;
                    }
                }
                conditionActions.set(lastIndex, new Push(values));
            } else {
                conditionActions.add(BasicAction
                        .fromInt(ActionTypes.LOGICAL_NOT));
                conditionActions.add(new If(blockLength + iteratorLength + 5));
                conditionLength += 6;
            }
        }

        // Add the jump to the start of the condition block

        iteratorLength += 5;
        iteratorActions.add(new Jump(
                -(conditionLength + blockLength + iteratorLength)));

        // Replace any break and continue place holders with jump statements.

        int currentLength = conditionLength;

        for (int i = 0; i < blockActions.size(); i++) {
            Action currentAction = blockActions.get(i);

            currentLength += currentAction.prepareToEncode(info);

            if (currentAction instanceof ActionObject) {
                ActionObject actionObject = (ActionObject) currentAction;
                if (actionObject.getType() == 256)
                    blockActions.set(i, new Jump(
                            (blockLength + conditionLength) - currentLength
                                    + iteratorLength));

                if (actionObject.getType() == 257)
                    blockActions.set(i, new Jump(
                            (blockLength + conditionLength) - currentLength));
            }
        }

        actions.addAll(initializeActions);
        actions.addAll(conditionActions);
        actions.addAll(blockActions);
        actions.addAll(iteratorActions);
    }

    private void generateForIn(ASContext info, List<Action> actions) {
        int count = count();

        ArrayList<Action> conditionActions = new ArrayList<Action>();
        ArrayList<Action> blockActions = new ArrayList<Action>();

        int conditionLength = 0;
        int blockLength = 0;

        // Push all the attributes of the specified object onto the stack

        switch (info.get(Context.VERSION)) {
        case 5:
            children[1].generate(info, actions);
            actions.remove(actions.size() - 1);
            actions.add(BasicAction.fromInt(ActionTypes.ENUMERATE));
            break;
        case 6:
        case 7:
            children[1].generate(info, actions);
            actions.add(BasicAction.fromInt(ActionTypes.ENUMERATE_OBJECT));
            break;
        }
        // Set the enumerator variable with the current attribute

        addReference(blockActions, info, children[0].sValue);
        addLiteral(blockActions, new RegisterIndex(0));
        blockActions.add(BasicAction.fromInt(ActionTypes.SET_VARIABLE));

        // Translate the body of the for..in statement

        if (count == 3) {
            children[2].discardValues();
            children[2].generate(info, blockActions);
        }

        // Calculate the length of the block in bytes

        blockLength = actionLength(blockActions, info);

        // Translate the clause of the for..in statement

        conditionActions.add(new RegisterCopy(0));
        addLiteral(conditionActions, Null.getInstance());
        conditionActions.add(BasicAction.fromInt(ActionTypes.EQUALS));
        conditionActions.add(new If(blockLength + 5)); // includes loop jump

        // Calculate the length of the condition actions in bytes

        conditionLength = actionLength(conditionActions, info);

        // Add the jump to the start of the condition block

        blockActions.add(new Jump(-(conditionLength + blockLength + 5)));
        blockLength += 5;

        // Replace any break and continue place holders with jump statements.

        int currentLength = conditionLength;

        for (int i = 0; i < blockActions.size(); i++) {
            Action currentAction = blockActions.get(i);

            currentLength += currentAction.prepareToEncode(info);

            if (currentAction instanceof ActionObject) {
                ActionObject actionObject = (ActionObject) currentAction;
                if (actionObject.getType() == 256)
                    blockActions.set(i, new Jump(blockLength - currentLength));

                if (actionObject.getType() == 257)
                    blockActions.set(i, new Jump(
                            -(conditionLength + currentLength)));
            }
        }

        actions.addAll(conditionActions);
        actions.addAll(blockActions);
    }

    private void generateWith(ASContext info, List<Action> actions) {
        ArrayList<Action> array = new ArrayList<Action>();
        int count = count();

        for (int i = 1; i < count; i++)
            children[i].discardValues();

        for (int i = 1; i < count; i++)
            children[i].generate(info, array);

        children[0].generate(info, actions);

        actions.add(new With(array));
    }

    @SuppressWarnings("unchecked")
    private void generateSwitch(ASContext info, List<Action> actions) {
        int count = count();

        int listCount = 0;
        int labelCount = 0;

        int defaultIndex = -1;
        int defaultTarget = -1;

        for (int i = 0; i < count; i++) {
            if (children[i].type == ASNode.List) {
                listCount += 1;
            } else if (children[i].type == ASNode.Label) {
                if (children[i].children == null) {
                    defaultIndex = labelCount;
                    defaultTarget = listCount;
                }
                labelCount += 1;
            }
        }

        ArrayList<Action> labelArray[] = new ArrayList[labelCount];
        int labelLength[] = new int[labelCount];
        int labelTarget[] = new int[labelCount];

        ArrayList<Action> listArray[] = new ArrayList[listCount];
        int listLength[] = new int[listCount];

        int offsetToEnd[] = new int[listCount];
        int offsetFromStart[] = new int[listCount];

        for (int i = 0; i < labelCount; i++) {
            labelArray[i] = new ArrayList<Action>();
        }

        for (int i = 0; i < listCount; i++) {
            listArray[i] = new ArrayList<Action>();

            offsetToEnd[i] = 0;
            offsetFromStart[i] = 0;
        }

        int listIndex = 0;
        int labelIndex = 0;

        for (int i = 0; i < count; i++) {
            if (children[i].type == ASNode.Label) {
                if (children[i].children != null) {
                    if (labelIndex == 0)
                        labelArray[labelIndex].add(new RegisterCopy(0));
                    else
                        addLiteral(labelArray[labelIndex], new RegisterIndex(0));
                }

                children[i].generate(info, labelArray[labelIndex]);
                labelLength[labelIndex] = actionLength(labelArray[labelIndex],
                        info);
                labelTarget[labelIndex] = listIndex;
                labelIndex += 1;
            } else if (children[i].type == ASNode.List) {
                children[i].generate(info, listArray[listIndex]);
                listLength[listIndex] = actionLength(listArray[listIndex], info);
                listIndex += 1;
            }
        }

        for (int i = listCount - 2; i >= 0; i--) {
            offsetToEnd[i] = offsetToEnd[i + 1] + listLength[i + 1];
        }

        for (int i = 1; i < listCount; i++) {
            offsetFromStart[i] = offsetFromStart[i - 1] + listLength[i - 1];
        }

        for (int i = 0; i < labelCount; i++) {
            labelLength[i] += (i == defaultIndex) ? 0 : 6;
        }

        if (defaultIndex != -1)
            labelLength[defaultIndex] = 5;

        int index = 0;

        children[0].generate(info, actions);

        for (int i = 1; i < count; i++) {
            if (children[i].type == ASNode.Label) {
                if (children[i].children != null) {
                    int offset = 0;

                    for (int j = index + 1; j < labelCount; j++)
                        offset += labelLength[j];

                    actions.addAll(labelArray[index]);
                    actions.add(BasicAction.fromInt(ActionTypes.STRICT_EQUALS));
                    actions.add(new If(offsetFromStart[labelTarget[index]]
                            + offset));
                } else {
                    actions.add(new Jump(offsetFromStart[defaultTarget]));
                }
                index += 1;
            }
        }

        for (int i = 0; i < listCount; i++) {
            Action action = (listArray[i].get(listArray[i].size() - 1));

            if (action instanceof ActionObject) {
                if (((ActionObject) action).getType() == 256) {
                    listArray[i].remove(listArray[i].size() - 1);
                    listArray[i].add(new Jump(offsetToEnd[i]));
                }
            }

            actions.addAll(listArray[i]);
        }
    }

    private void generateLabel(ASContext info, List<Action> actions) {
        int count = count();

        for (int i = 0; i < count; i++) {
            children[i].generate(info, actions);
        }
    }

    @SuppressWarnings("unchecked")
    private void generateException(ASContext info, List<Action> actions) {
        int count = count();

        ArrayList<Action> actionArray[] = new ArrayList[count];

        for (int i = 0; i < count; i++)
            children[i].discardValues();

        for (int i = 0; i < count; i++) {
            actionArray[i] = new ArrayList<Action>();

            children[i].generate(info, actionArray[i]);
        }

        actions.add(new ExceptionHandler(101, actionArray[0], actionArray[1],
                actionArray[2]));
    }

    private void generateClauses(ASContext info, List<Action> actions) {
        int count = count();

        for (int i = 0; i < count; i++)
            children[i].discardValues();

        for (int i = 0; i < count; i++) {
            children[i].generate(info, actions);
        }
    }

    private void generateReturn(ASContext info, List<Action> actions) {
        int count = count();

        switch (type) {
        case Break:
            actions.add(new ActionObject(256, new byte[2]));
            break;
        case Continue:
            actions.add(new ActionObject(257, new byte[2]));
            break;
        case Return:
            if (count == 0) {
                addLiteral(actions, Void.getInstance());
            } else {
                for (int i = 0; i < count; i++)
                    children[i].generate(info, actions);
            }
            actions.add(BasicAction.fromInt(ActionTypes.RETURN));
            break;
        default:
            break;
        }
    }

    private void generateValue(ASContext info, List<Action> actions) {
        int count = count();

        switch (type) {
        case Value:
            /*
             * If any of the children is a method call then generate the actions
             * for the method arguments. This ensures that the arguments will be
             * popped off the stack in the correct order.
             */
            for (int i = count - 1; i >= 0; i--) {
                if (children[i].type == Function || children[i].type == Method) {
                    ASNode[] grandChildren = children[i].children;

                    if (grandChildren != null) {
                        int numGrandChildren = grandChildren.length;

                        for (int j = numGrandChildren - 1; j >= 0; j--)
                            grandChildren[j].generate(info, actions);

                        addLiteral(actions, numGrandChildren);
                    } else {
                        addLiteral(actions, 0);
                    }
                }
            }

            /*
             * Now generate the actions for each node that returns a value. Note
             * that below methods do not generate actions for their children
             * since the parent node is always a Value. Functions only do so if
             * the parent node is not a Value.
             */
            children[0].generate(info, actions);

            for (int i = 1; i < count; i++) {
                if (children[i].type == Function) {
                    Action last = actions.get(actions.size() - 1);

                    if (last == BasicAction.GET_ATTRIBUTE) {
                        actions.remove(actions.size() - 1);
                        actions.add(BasicAction
                                .fromInt(ActionTypes.EXECUTE_METHOD));
                    } else {
                        actions.add(BasicAction
                                .fromInt(ActionTypes.EXECUTE_FUNCTION));
                    }
                } else
                    children[i].generate(info, actions);
            }

            if (discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case BooleanLiteral:
            addLiteral(actions, new Boolean(bValue));

            if (discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case IntegerLiteral:
            addLiteral(actions, iValue);

            if (discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case DoubleLiteral:
            int val = (int) dValue;

            if (dValue == -0.0) {
                addLiteral(actions, new Double(dValue));
            } else if (val == dValue) {
                addLiteral(actions, new Integer(val));
            } else {
                addLiteral(actions, new Double(dValue));
            }

            if (discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case StringLiteral:
            addReference(actions, info, sValue);

            if (discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case NullLiteral:
            addLiteral(actions, Null.getInstance());

            if (discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case Identifier:
            if (constants.containsKey(sValue)) {
                if (sValue.equals("undefined"))
                    addLiteral(actions, Void.getInstance());
                else
                    addLiteral(actions, constants.get(sValue));
            } else if (propertyNames.containsKey(sValue)) {
                if (info.context.contains("With")) {
                    addReference(actions, info, sValue);
                    actions.add(BasicAction.fromInt(ActionTypes.GET_VARIABLE));
                } else if (info.context.contains("Define Object")) {
                    addReference(actions, info, sValue);
                    actions.add(BasicAction.fromInt(ActionTypes.GET_VARIABLE));
                } else if (info.context.contains("setProperty")) {
                    int pVal = (propertyNames.get(sValue)).intValue();

                    if (pVal >= 16 && pVal <= 21)
                        addLiteral(actions, new Integer(pVal));
                    else
                        addLiteral(actions, new Property((earlyPropertyNames
                                .get(sValue)).intValue()));
                } else {
                    int pVal = (propertyNames.get(sValue)).intValue();

                    addReference(actions, info, "");
                    if (pVal >= 0 && pVal <= 21)
                        addLiteral(actions, new Integer(pVal));
                    else
                        addLiteral(actions, new Property(pVal));
                    actions.add(BasicAction.fromInt(ActionTypes.GET_PROPERTY));
                }
            } else {
                addReference(actions, info, sValue);
                actions.add(BasicAction.fromInt(ActionTypes.GET_VARIABLE));
            }
            if (discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case Attribute:
            addReference(actions, info, sValue);
            actions.add(BasicAction.fromInt(ActionTypes.GET_ATTRIBUTE));
            if (discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case Method:
            addReference(actions, info, sValue);
            actions.add(BasicAction.fromInt(ActionTypes.EXECUTE_METHOD));
            break;
        case NewObject:
            for (int i = count - 1; i >= 0; i--)
                children[i].generate(info, actions);
            addLiteral(actions, count);
            addReference(actions, info, sValue);
            actions.add(BasicAction.fromInt(ActionTypes.NAMED_OBJECT));
            break;
        case Subscript:
            children[0].generate(info, actions);
            actions.add(BasicAction.fromInt(ActionTypes.GET_ATTRIBUTE));
            break;
        default:
            break;
        }
    }

    private void generateDefinition(ASContext info, List<Action> actions) {
        int count = count();
        int last = count - 1;

        switch (type) {
        case DefineArray:
            for (int i = last; i >= 0; i--)
                children[i].generate(info, actions);
            addLiteral(actions, count);
            actions.add(BasicAction.fromInt(ActionTypes.NEW_ARRAY));
            break;
        case DefineObject:
            for (int i = 0; i < count; i++)
                children[i].generate(info, actions);
            addLiteral(actions, count);
            actions.add(BasicAction.fromInt(ActionTypes.NEW_OBJECT));
            break;
        case DefineFunction:

            if (sValue.equals("ifFrameLoaded")) {
                List<Action> array = new ArrayList<Action>();

                children[count - 1].discardValues();
                children[count - 1].generate(info, array);

                if (children[0].count() == 0) {
                    children[0].generate(info, actions);
                } else if (children[0].count() == 2) {
                    children[0].children[1].generate(info, actions);
                }

                addLiteral(actions, 0);
                actions.add(BasicAction.fromInt(ActionTypes.ADD));
                actions.add(new WaitForFrame2(array.size()));
                actions.addAll(array);
            } else if (sValue.equals("tellTarget")) {
                actions.add(new SetTarget(children[0].sValue));

                children[1].generate(info, actions);

                actions.add(new SetTarget(""));
            } else {
                List<String> functionArguments = new ArrayList<String>();
                List<Action> functionActions = new ArrayList<Action>();

                if (count() == 2) {
                    if (children[0].type == List) {
                        count = children[0].count();

                        for (int i = 0; i < count; i++)
                            functionArguments
                                    .add(children[0].children[i].sValue);
                    } else {
                        functionArguments.add(children[0].sValue);
                    }
                }
                children[last].discardValues();
                children[last].generate(info, functionActions);

                actions.add(new NewFunction(sValue, functionArguments,
                        functionActions));
            }
            break;
        case DefineMethod:
            List<String> methodArguments = new ArrayList<String>();
            List<Action> methodActions = new ArrayList<Action>();

            if (count() == 2) {
                if (children[0].type == List) {
                    count = children[0].count();

                    for (int i = 0; i < count; i++)
                        methodArguments.add(children[0].children[i].sValue);
                } else {
                    methodArguments.add(children[0].sValue);
                }
            }
            children[last].discardValues();
            children[last].generate(info, methodActions);

            actions.add(new NewFunction("", methodArguments, methodActions));
            break;
        case DefineAttribute:
            children[0].generate(info, actions);
            actions.remove(actions.size() - 1);
            children[1].generate(info, actions);
            break;
        case DefineVariable:
            addReference(actions, info, sValue);
            actions.add(BasicAction.fromInt(ActionTypes.INIT_VARIABLE));
            break;
        default:
            break;
        }
    }

    private void generateUnary(ASContext info, List<Action> actions) {
        Action lastAction = null;

        switch (type) {
        case PreInc:
            children[0].generate(info, actions);
            actions.remove(actions.size() - 1);
            children[0].generate(info, actions);
            lastAction = actions.get(actions.size() - 1);
            actions.add(BasicAction.fromInt(ActionTypes.INCREMENT));

            if (discardValue == false)
                actions.add(new RegisterCopy(0));

            if (lastAction == BasicAction.GET_ATTRIBUTE)
                actions.add(BasicAction.fromInt(ActionTypes.SET_ATTRIBUTE));
            else
                actions.add(BasicAction.fromInt(ActionTypes.SET_VARIABLE));

            if (discardValue == false)
                addLiteral(actions, new RegisterIndex(0));

            break;
        case PreDec:
            children[0].generate(info, actions);
            actions.remove(actions.size() - 1);
            children[0].generate(info, actions);
            lastAction = actions.get(actions.size() - 1);
            actions.add(BasicAction.fromInt(ActionTypes.DECREMENT));

            if (discardValue == false)
                actions.add(new RegisterCopy(0));

            if (lastAction == BasicAction.GET_ATTRIBUTE)
                actions.add(BasicAction.fromInt(ActionTypes.SET_ATTRIBUTE));
            else
                actions.add(BasicAction.fromInt(ActionTypes.SET_VARIABLE));

            if (discardValue == false)
                addLiteral(actions, new RegisterIndex(0));

            break;
        case PostInc:
            if (discardValue == false)
                children[0].generate(info, actions);

            children[0].generate(info, actions);
            actions.remove(actions.size() - 1);
            children[0].generate(info, actions);
            lastAction = actions.get(actions.size() - 1);
            actions.add(BasicAction.fromInt(ActionTypes.INCREMENT));
            if (lastAction == BasicAction.GET_ATTRIBUTE)
                actions.add(BasicAction.fromInt(ActionTypes.SET_ATTRIBUTE));
            else
                actions.add(BasicAction.fromInt(ActionTypes.SET_VARIABLE));
            break;
        case PostDec:
            if (discardValue == false)
                children[0].generate(info, actions);

            children[0].generate(info, actions);
            actions.remove(actions.size() - 1);
            children[0].generate(info, actions);
            lastAction = actions.get(actions.size() - 1);
            actions.add(BasicAction.fromInt(ActionTypes.DECREMENT));
            if (lastAction == BasicAction.GET_ATTRIBUTE)
                actions.add(BasicAction.fromInt(ActionTypes.SET_ATTRIBUTE));
            else
                actions.add(BasicAction.fromInt(ActionTypes.SET_VARIABLE));
            break;
        case Plus:
            if (children[0].type == BooleanLiteral) {
                children[0].generate(info, actions);
                addLiteral(actions, 0);
                actions.add(BasicAction.fromInt(ActionTypes.ADD));
            } else if (children[0].type == IntegerLiteral) {
                addLiteral(actions, children[0].iValue);
            } else if (children[0].type == StringLiteral) {
                children[0].generate(info, actions);
                addLiteral(actions, 0);
                actions.add(BasicAction.fromInt(ActionTypes.ADD));
            } else if (children[0].type == NullLiteral) {
                children[0].generate(info, actions);
                addLiteral(actions, 0);
                actions.add(BasicAction.fromInt(ActionTypes.ADD));
            } else {
                children[0].generate(info, actions);
            }
            if (discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case Minus:
            if (children[0].type == BooleanLiteral) {
                addLiteral(actions, 0);
                children[0].generate(info, actions);
                actions.add(BasicAction.fromInt(ActionTypes.SUBTRACT));
            } else if (children[0].type == IntegerLiteral) {
                if (children[0].iValue == 0) {
                    addLiteral(actions, -0.0);
                } else {
                    addLiteral(actions, -children[0].iValue);
                }
            } else if (children[0].type == StringLiteral) {
                addLiteral(actions, 0);
                children[0].generate(info, actions);
                actions.add(BasicAction.fromInt(ActionTypes.SUBTRACT));
            } else if (children[0].type == NullLiteral) {
                addLiteral(actions, 0);
                children[0].generate(info, actions);
                actions.add(BasicAction.fromInt(ActionTypes.SUBTRACT));
            } else {
                addLiteral(actions, 0);
                children[0].generate(info, actions);
                actions.add(BasicAction.fromInt(ActionTypes.SUBTRACT));
            }
            if (discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case BitNot:
            children[0].generate(info, actions);
            addLiteral(actions, new Double(Double
                    .longBitsToDouble(0x41EFFFFFFFE00000L)));
            actions.add(BasicAction.fromInt(ActionTypes.BITWISE_XOR));

            if (discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case Not:
            children[0].generate(info, actions);
            actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));

            if (discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case Delete:
            children[0].generate(info, actions);
            actions.remove(actions.size() - 1);

            if (children[0].type == Value)
                actions.add(BasicAction.fromInt(ActionTypes.DELETE_VARIABLE));
            else
                actions.add(BasicAction.fromInt(ActionTypes.DELETE));

            if (discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case Throw:
            children[0].generate(info, actions);
            actions.add(BasicAction.fromInt(ActionTypes.THROW));
            break;
        default:
            break;
        }
    }

    private void generateBinary(ASContext info, List<Action> actions) {
        List<Action> array = new ArrayList<Action>();

        int count = count();
        int offset = 0;

        /*
         * For most node types we want to generate the actions for the child
         * nodes (if any) before adding the actions for node type.
         */

        switch (type) {
        // > and <= are synthesised using < and !, see below.
        case LessThanEqual:
        case StringLessThanEqual:
        case StringGreaterThan:
            for (int i = count - 1; i >= 0; i--)
                children[i].generate(info, actions);
            break;
        // Code Logical And/Or generated using if actions, see below.
        case LogicalAnd:
        case LogicalOr:
        case StrictEqual:
        case StrictNotEqual:
        case GreaterThan:
            break;
        default:
            for (int i = 0; i < count; i++)
                children[i].generate(info, actions);
            break;
        }

        switch (type) {
        case StringAdd:
            actions.add(BasicAction.fromInt(ActionTypes.STRING_ADD));
            break;
        case StringLessThanEqual:
        case StringGreaterThanEqual:
            actions.add(BasicAction.fromInt(ActionTypes.STRING_LESS));
            actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));
            break;
        case StringGreaterThan:
            actions.add(BasicAction.fromInt(ActionTypes.STRING_LESS));
            break;
        case Add:
            actions.add(BasicAction.fromInt(ActionTypes.ADD));
            break;
        case Sub:
            actions.add(BasicAction.fromInt(ActionTypes.SUBTRACT));
            break;
        case Mul:
            actions.add(BasicAction.fromInt(ActionTypes.MULTIPLY));
            break;
        case Div:
            actions.add(BasicAction.fromInt(ActionTypes.DIVIDE));
            break;
        case Mod:
            actions.add(BasicAction.fromInt(ActionTypes.MODULO));
            break;
        case BitAnd:
            actions.add(BasicAction.fromInt(ActionTypes.BITWISE_AND));
            break;
        case BitOr:
            actions.add(BasicAction.fromInt(ActionTypes.BITWISE_OR));
            break;
        case BitXOr:
            actions.add(BasicAction.fromInt(ActionTypes.BITWISE_XOR));
            break;
        case LSL:
            actions.add(BasicAction.fromInt(ActionTypes.SHIFT_LEFT));
            break;
        case LSR:
            actions.add(BasicAction.fromInt(ActionTypes.SHIFT_RIGHT));
            break;
        case ASR:
            actions.add(BasicAction.fromInt(ActionTypes.ARITH_SHIFT_RIGHT));
            break;
        case Equal:
            actions.add(BasicAction.fromInt(ActionTypes.EQUALS));
            break;
        case NotEqual:
            actions.add(BasicAction.fromInt(ActionTypes.EQUALS));
            actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));
            break;
        case LessThan:
            actions.add(BasicAction.fromInt(ActionTypes.LESS));
            break;
        case GreaterThan:
            switch (info.get(Context.VERSION)) {
            case 5:
                children[1].generate(info, actions);
                children[0].generate(info, actions);
                actions.add(BasicAction.fromInt(ActionTypes.LESS));
//
//                if (parent.type != If)
//                    actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));
                break;
            case 6:
            case 7:
                children[0].generate(info, actions);
                children[1].generate(info, actions);
                actions.add(BasicAction.fromInt(ActionTypes.GREATER));
                break;
            }
            break;
        case LessThanEqual:
            actions.add(BasicAction.fromInt(ActionTypes.LESS));
            actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));
            break;
        case GreaterThanEqual:
            actions.add(BasicAction.fromInt(ActionTypes.LESS));
            if (parent.type != If)
                actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));
            break;
        case And:
            actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_AND));
            break;
        case Or:
            actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_OR));
            break;
        case LogicalAnd:
            array.add(BasicAction.fromInt(ActionTypes.POP));

            children[1].generate(info, array);
            offset = actionLength(array, info);

            children[0].generate(info, actions);

            actions.add(BasicAction.fromInt(ActionTypes.DUPLICATE));
            actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));

            actions.add(new If(offset));
            actions.addAll(array);
            break;
        case LogicalOr:
            array.add(BasicAction.fromInt(ActionTypes.POP));

            children[1].generate(info, array);
            offset = actionLength(array, info);

            children[0].generate(info, actions);
            actions.add(BasicAction.fromInt(ActionTypes.DUPLICATE));

            actions.add(new If(offset));
            actions.addAll(array);
            break;
        case InstanceOf:
            actions.add(BasicAction.fromInt(ActionTypes.INSTANCEOF));
            break;
        case StrictEqual:
            switch (info.get(Context.VERSION)) {
            case 5:
                children[0].generate(info, actions);
                actions.add(new RegisterCopy(1));
                actions.add(BasicAction.fromInt(ActionTypes.GET_TYPE));
                children[1].generate(info, actions);
                actions.add(new RegisterCopy(2));
                actions.add(BasicAction.fromInt(ActionTypes.GET_TYPE));
                actions.add(BasicAction.fromInt(ActionTypes.EQUALS));
                actions.add(new If(10));
                addLiteral(actions, new Boolean(true));
                actions.add(new Jump(8));
                addLiteral(actions, new RegisterIndex(1));
                addLiteral(actions, new RegisterIndex(2));
                actions.add(BasicAction.fromInt(ActionTypes.EQUALS));
                break;
            case 6:
                actions.add(BasicAction.fromInt(ActionTypes.STRICT_EQUALS));
                break;
            }
            break;
        case StrictNotEqual:
            switch (info.get(Context.VERSION)) {
            case 5:
                children[0].generate(info, actions);
                actions.add(new RegisterCopy(1));
                actions.add(BasicAction.fromInt(ActionTypes.GET_TYPE));
                children[1].generate(info, actions);
                actions.add(new RegisterCopy(2));
                actions.add(BasicAction.fromInt(ActionTypes.GET_TYPE));
                actions.add(BasicAction.fromInt(ActionTypes.EQUALS));
                actions.add(new If(10));
                addLiteral(actions, new Boolean(true));
                actions.add(new Jump(8));
                addLiteral(actions, new RegisterIndex(1));
                addLiteral(actions, new RegisterIndex(2));
                actions.add(BasicAction.fromInt(ActionTypes.EQUALS));
                actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));
                break;
            case 6:
                actions.add(BasicAction.fromInt(ActionTypes.STRICT_EQUALS));
                actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));
                break;
            }
            break;
        default:
            break;
        }
        if (discardValue)
            actions.add(BasicAction.fromInt(ActionTypes.POP));
    }

    private void generateSelect(ASContext info, List<Action> actions) {
        List<Action> trueActions = new ArrayList<Action>();
        int offsetToNext = 0;

        List<Action> falseActions = new ArrayList<Action>();
        int offsetToEnd = 0;

        children[2].generate(info, falseActions);

        offsetToNext = actionLength(falseActions, info);
        offsetToNext += 5; // Length of jump tag

        children[1].generate(info, trueActions);

        offsetToEnd = actionLength(trueActions, info);

        children[0].generate(info, actions);

        actions.add(new If(offsetToNext));
        actions.addAll(falseActions);

        actions.add(new Jump(offsetToEnd));

        actions.addAll(trueActions);

        if (discardValue)
            actions.add(BasicAction.fromInt(ActionTypes.POP));
    }

    private void generateAssignment(ASContext info, List<Action> actions) {
        children[0].generate(info, actions);

        Action lastAction = actions.get(actions.size() - 1);

        if (lastAction == BasicAction.GET_VARIABLE)
            actions.remove(actions.size() - 1);
        else if (lastAction == BasicAction.GET_ATTRIBUTE)
            actions.remove(actions.size() - 1);
        else if (lastAction == BasicAction.GET_PROPERTY)
            actions.remove(actions.size() - 1);
        else if (lastAction == BasicAction.INIT_VARIABLE)
            actions.remove(actions.size() - 1);

        if (type != Assign)
            children[0].generate(info, actions);

        children[1].generate(info, actions);

        switch (type) {
        case AssignAdd:
            actions.add(BasicAction.fromInt(ActionTypes.ADD));
            break;
        case AssignSub:
            actions.add(BasicAction.fromInt(ActionTypes.SUBTRACT));
            break;
        case AssignMul:
            actions.add(BasicAction.fromInt(ActionTypes.MULTIPLY));
            break;
        case AssignDiv:
            actions.add(BasicAction.fromInt(ActionTypes.DIVIDE));
            break;
        case AssignMod:
            actions.add(BasicAction.fromInt(ActionTypes.MODULO));
            break;
        case AssignBitAnd:
            actions.add(BasicAction.fromInt(ActionTypes.BITWISE_AND));
            break;
        case AssignBitOr:
            actions.add(BasicAction.fromInt(ActionTypes.BITWISE_OR));
            break;
        case AssignBitXOr:
            actions.add(BasicAction.fromInt(ActionTypes.BITWISE_XOR));
            break;
        case AssignLSL:
            actions.add(BasicAction.fromInt(ActionTypes.SHIFT_LEFT));
            break;
        case AssignLSR:
            actions.add(BasicAction.fromInt(ActionTypes.SHIFT_RIGHT));
            break;
        case AssignASR:
            actions.add(BasicAction.fromInt(ActionTypes.ARITH_SHIFT_RIGHT));
            break;
        default:
            break;
        }

        if (type == Assign && parent != null
                && (parent.type == List || parent.type == Assign)) {
            if (children[0].type != DefineVariable) {
                actions.add(new RegisterCopy(0));
            }
        }

        if (lastAction == BasicAction.GET_PROPERTY)
            actions.add(BasicAction.fromInt(ActionTypes.SET_PROPERTY));
        else if (lastAction == BasicAction.GET_ATTRIBUTE)
            actions.add(BasicAction.fromInt(ActionTypes.SET_ATTRIBUTE));
        else if (lastAction == BasicAction.GET_VARIABLE)
            actions.add(BasicAction.fromInt(ActionTypes.SET_VARIABLE));
        else if (lastAction == BasicAction.INIT_VARIABLE)
            actions.add(BasicAction.fromInt(ActionTypes.INIT_VARIABLE));

        if (type == Assign && parent != null
                && (parent.type == List || parent.type == Assign)) {
            if (children[0].type != DefineVariable) {
                addLiteral(actions, new RegisterIndex(0));

                if (parent.type == List)
                    actions.add(BasicAction.fromInt(ActionTypes.POP));
            }
        }

    }

    /*
     * generateFunction is used to add either a predefined action if the
     * function call is to one of Flash's built-in functions. A separate method
     * is used to make the code in the generate method more readable.
     */
    private void generateFunction(List<Action> actions, ASContext info,
            Object value) {
        String name = (value == null) ? "" : (String) value;
        int count = count();

        if (functions.containsKey(name.toLowerCase())) {
            if (sValue.toLowerCase().equals("call")) {
                children[0].generate(info, actions);

                Action lastAction = actions.get(actions.size() - 1);

                if (lastAction == BasicAction.GET_VARIABLE)
                    actions.remove(actions.size() - 1);

                actions.add(Call.getInstance());
            } else if (sValue.toLowerCase().equals("chr")) {
                children[0].generate(info, actions);
                actions.add(BasicAction.fromInt(ActionTypes.ASCII_TO_CHAR));
            } else if (sValue.toLowerCase().equals("delete")) {
                children[0].generate(info, actions);

                Action lastAction = actions.get(actions.size() - 1);

                if (lastAction == BasicAction.GET_VARIABLE)
                    actions.remove(actions.size() - 1);

                actions.add(BasicAction.fromInt(ActionTypes.DELETE));
            } else if (sValue.toLowerCase().equals("duplicatemovieclip")) {
                children[0].generate(info, actions);
                children[1].generate(info, actions);

                if (children[2].type == IntegerLiteral
                        && children[2].sValue == null) {
                    int level = 16384;

                    level += children[2].iValue;

                    addLiteral(actions, level);
                } else {
                    addLiteral(actions, 16384);

                    children[2].generate(info, actions);

                    actions.add(BasicAction.fromInt(ActionTypes.ADD));
                }
                actions.add(BasicAction.fromInt(ActionTypes.CLONE_SPRITE));
            } else if (sValue.toLowerCase().equals("eval")) {
                children[0].generate(info, actions);
                actions.add(BasicAction.fromInt(ActionTypes.GET_VARIABLE));
            } else if (sValue.toLowerCase().equals("fscommand")) {
                boolean isCommandString = children[0].type == StringLiteral
                        && children[0].sValue != null;
                boolean isArgumentString = false;

                if (count == 1) {
                    isArgumentString = true;
                }
                if (count > 1) {
                    isArgumentString = children[1].type == StringLiteral
                            && children[1].sValue != null;
                }

                if (isCommandString && isArgumentString) {
                    String url = children[0].sValue;
                    String target;
                    if (count == 1) {
                        target = "";
                    } else {
                        target = children[1].sValue;
                    }
                    actions.add(new GetUrl(url, target));
                } else {
                    if (isCommandString) {
                        addReference(actions, info, children[0].sValue);
                    } else {
                        addReference(actions, info, "FSCommand:");
                        children[0].generate(info, actions);
                        actions
                                .add(BasicAction
                                        .fromInt(ActionTypes.STRING_ADD));
                    }

                    if (count > 1) {
                        children[1].generate(info, actions);
                    }

                    actions.add(new GetUrl2(GetUrl2.Request.MOVIE_TO_LEVEL));
                }
            } else if (sValue.toLowerCase().equals("getproperty")) {
                String propertyName = children[1].sValue;
                int pVal = (propertyNames.get(propertyName)).intValue();

                children[0].generate(info, actions);
                if (pVal >= 1 && pVal <= 21)
                    addLiteral(actions, new Integer(pVal));
                else if (pVal == 0)
                    addLiteral(actions, new Double(pVal));
                else
                    addLiteral(actions, new Property(pVal));
                actions.add(BasicAction.fromInt(ActionTypes.GET_PROPERTY));
            } else if (sValue.toLowerCase().equals("gettimer")) {
                for (int i = count - 1; i >= 0; i--)
                    children[i].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.GET_TIME));
            } else if (sValue.toLowerCase().equals("geturl")) {
                switch (count) {
                case 1:
                    if (children[0].type == StringLiteral
                            && children[0].sValue != null) {
                        actions.add(new GetUrl(children[0].sValue, ""));
                    } else {
                        children[0].generate(info, actions);
                        addReference(actions, info, "");
                        actions
                                .add(new GetUrl2(GetUrl2.Request.MOVIE_TO_LEVEL));
                    }
                    break;
                case 2:
                    if (children[0].type == StringLiteral
                            && children[0].sValue != null
                            && children[1].type == StringLiteral
                            && children[1].sValue != null) {
                        actions.add(new GetUrl(children[0].sValue,
                                children[1].sValue));
                    } else {
                        children[0].generate(info, actions);
                        children[1].generate(info, actions);
                        actions
                                .add(new GetUrl2(GetUrl2.Request.MOVIE_TO_LEVEL));
                    }
                    break;
                case 3:
                    children[0].generate(info, actions);
                    children[1].generate(info, actions);

                    if (children[2].sValue.toLowerCase().equals("get"))
                        actions.add(new GetUrl2(
                                GetUrl2.Request.MOVIE_TO_LEVEL_WITH_GET));
                    else if (children[2].sValue.toLowerCase().equals("post"))
                        actions.add(new GetUrl2(
                                GetUrl2.Request.MOVIE_TO_LEVEL_WITH_POST));
                    else
                        actions
                                .add(new GetUrl2(GetUrl2.Request.MOVIE_TO_LEVEL));
                    break;
                default:
                    break;
                }
            } else if (sValue.toLowerCase().equals("getversion")) {
                addLiteral(actions, "/:$version");
                actions.add(BasicAction.fromInt(ActionTypes.GET_VARIABLE));
            } else if (sValue.toLowerCase().equals("gotoandplay")) {
                int index = count - 1;

                if (info.context.firstElement().toString().equals("MovieClip")) {
                    if (children[index].sValue == null) {
                        int frameNumber = children[index].iValue - 1;

                        actions.add(new GotoFrame(frameNumber));
                    } else {
                        actions.add(new GotoLabel(children[index].sValue));
                    }
                    actions.add(BasicAction.fromInt(ActionTypes.PLAY));
                } else {
                    if (children[index].sValue == null) {
                        int frameNumber = children[index].iValue - 1;

                        actions.add(new GotoFrame(frameNumber));
                        actions.add(BasicAction.fromInt(ActionTypes.PLAY));
                    } else if (children[index].sValue.toLowerCase().startsWith(
                            "frame ")) {
                        String frame = children[index].sValue.substring(6);
                        int frameNumber = 0;

                        try {
                            frameNumber = Integer.valueOf(frame).intValue() - 1;
                        } catch (NumberFormatException e) {

                        }

                        if (frameNumber == 1) {
                            children[index].generate(info, actions);
                            actions.add(new GotoFrame2(0, true));
                        } else {
                            actions.add(new GotoLabel(children[index].sValue));
                            actions.add(BasicAction.fromInt(ActionTypes.PLAY));
                        }
                    } else {
                        children[index].generate(info, actions);
                        actions.add(new GotoFrame2(0, true));
                    }
                }
            } else if (sValue.toLowerCase().equals("gotoandstop")) {
                int index = count - 1;

                if (info.context.firstElement().toString().equals("MovieClip")) {
                    if (children[index].sValue == null) {
                        int frameNumber = children[index].iValue - 1;

                        actions.add(new GotoFrame(frameNumber));
                    } else {
                        actions.add(new GotoLabel(children[index].sValue));
                    }
                } else {
                    if (children[index].sValue == null) {
                        int frameNumber = children[index].iValue - 1;

                        actions.add(new GotoFrame(frameNumber));
                    } else if (children[index].sValue.toLowerCase().startsWith(
                            "frame ")) {
                        String frame = children[index].sValue.substring(6);
                        int frameNumber = 0;

                        try {
                            frameNumber = Integer.valueOf(frame).intValue() - 1;
                        } catch (NumberFormatException e) {

                        }

                        if (frameNumber == 1) {
                            children[index].generate(info, actions);
                            actions.add(new GotoFrame2(0, false));
                        } else {
                            actions.add(new GotoLabel(children[index].sValue));
                        }
                    } else {
                        children[index].generate(info, actions);

                        actions.add(new GotoFrame2(0, false));
                    }
                }
            } else if (sValue.toLowerCase().equals("int")) {
                for (int i = count - 1; i >= 0; i--)
                    children[i].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.TO_INTEGER));
            } else if (sValue.toLowerCase().equals("length")) {
                for (int i = count - 1; i >= 0; i--)
                    children[i].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.STRING_LENGTH));
            } else if (sValue.toLowerCase().equals("loadmovie")) {
                switch (count) {
                case 2:
                    if (children[0].sValue != null
                            && children[1].sValue == null) {
                        String url = children[0].sValue;
                        String target = "_level" + children[1].iValue;

                        actions.add(new GetUrl(url, target));
                    } else {
                        children[0].generate(info, actions);
                        children[1].generate(info, actions);

                        actions
                                .add(new GetUrl2(
                                        GetUrl2.Request.MOVIE_TO_TARGET));
                    }
                    break;
                case 3:
                    children[0].generate(info, actions);
                    children[1].generate(info, actions);

                    if (children[2].sValue.toLowerCase().equals("get"))
                        actions.add(new GetUrl2(
                                GetUrl2.Request.MOVIE_TO_TARGET_WITH_GET));
                    else
                        actions.add(new GetUrl2(
                                GetUrl2.Request.MOVIE_TO_TARGET_WITH_POST));
                    break;
                default:
                    break;
                }
            } else if (sValue.toLowerCase().equals("loadvariables")) {
                switch (count) {
                case 2:
                    children[0].generate(info, actions);
                    children[1].generate(info, actions);

                    actions
                            .add(new GetUrl2(
                                    GetUrl2.Request.VARIABLES_TO_TARGET));
                    break;
                case 3:
                    children[0].generate(info, actions);
                    children[1].generate(info, actions);

                    if (children[2].sValue.toLowerCase().equals("get"))
                        actions.add(new GetUrl2(
                                GetUrl2.Request.VARIABLES_TO_TARGET_WITH_GET));
                    else
                        actions.add(new GetUrl2(
                                GetUrl2.Request.VARIABLES_TO_TARGET_WITH_POST));
                    break;
                default:
                    break;
                }
            } else if (sValue.toLowerCase().equals("mbchr")) {
                for (int i = count - 1; i >= 0; i--)
                    children[i].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.MB_ASCII_TO_CHAR));
            } else if (sValue.toLowerCase().equals("mbord")) {
                for (int i = count - 1; i >= 0; i--)
                    children[i].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.MB_CHAR_TO_ASCII));
            } else if (sValue.toLowerCase().equals("mbsubstring")) {
                for (int i = 0; i < count; i++)
                    children[i].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.MB_STRING_EXTRACT));
            } else if (sValue.toLowerCase().equals("nextframe")) {
                actions.add(BasicAction.fromInt(ActionTypes.NEXT_FRAME));
            } else if (sValue.toLowerCase().equals("nextscene")) {
                actions.add(new GotoFrame(0));
            } else if (sValue.toLowerCase().equals("number")) {
                children[0].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.TO_NUMBER));
            } else if (sValue.toLowerCase().equals("ord")) {
                for (int i = count - 1; i >= 0; i--)
                    children[i].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.CHAR_TO_ASCII));
            } else if (sValue.toLowerCase().equals("parseint")) {
                for (int i = count - 1; i >= 0; i--)
                    children[i].generate(info, actions);

                addLiteral(actions, count);
                addReference(actions, info, name);
                actions.add(BasicAction.fromInt(ActionTypes.EXECUTE_FUNCTION));
            } else if (sValue.toLowerCase().equals("play")) {
                actions.add(BasicAction.fromInt(ActionTypes.PLAY));
            } else if (sValue.toLowerCase().equals("prevframe")) {
                actions.add(BasicAction.fromInt(ActionTypes.PREV_FRAME));
            } else if (sValue.toLowerCase().equals("prevscene")) {
                actions.add(new GotoFrame(0));
            } else if (sValue.toLowerCase().equals("print")) {
                children[0].generate(info, actions);
                addReference(actions, info, children[1].sValue);
                actions.add(BasicAction.fromInt(ActionTypes.GET_VARIABLE));
                actions.add(new GetUrl2(GetUrl2.Request.MOVIE_TO_LEVEL));
            } else if (sValue.toLowerCase().equals("printnum")) {
                addReference(actions, info, children[1].sValue);

                if (children[0].type == Identifier) {
                    addReference(actions, info, "_level");
                    children[0].generate(info, actions);
                    actions.add(BasicAction.fromInt(ActionTypes.STRING_ADD));
                } else {
                    children[0].generate(info, actions);
                }
                actions.add(new GetUrl2(GetUrl2.Request.MOVIE_TO_LEVEL));
            } else if (sValue.toLowerCase().equals("printasbitmap")) {
                children[0].generate(info, actions);
                addReference(actions, info, children[1].sValue);
                actions.add(BasicAction.fromInt(ActionTypes.GET_VARIABLE));
                actions.add(new GetUrl2(GetUrl2.Request.MOVIE_TO_LEVEL));
            } else if (sValue.toLowerCase().equals("printasbitmapnum")) {
                addReference(actions, info, children[1].sValue);

                if (children[0].type == Identifier) {
                    addReference(actions, info, "_level");
                    children[0].generate(info, actions);
                    actions.add(BasicAction.fromInt(ActionTypes.STRING_ADD));
                } else {
                    children[0].generate(info, actions);
                }
                actions.add(new GetUrl2(GetUrl2.Request.MOVIE_TO_LEVEL));
            } else if (sValue.toLowerCase().equals("random")) {
                children[0].generate(info, actions);
                actions.add(BasicAction.fromInt(ActionTypes.RANDOM_NUMBER));
            } else if (sValue.toLowerCase().equals("removemovieclip")) {
                for (int i = 0; i < count; i++)
                    children[i].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.REMOVE_SPRITE));
            } else if (sValue.toLowerCase().equals("set")) {
                for (int i = 0; i < count; i++)
                    children[i].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.SET_VARIABLE));
            } else if (sValue.toLowerCase().equals("setproperty")) {
                for (int i = 0; i < count; i++)
                    children[i].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.SET_PROPERTY));
            } else if (sValue.toLowerCase().equals("startdrag")) {
                if (count > 2) {
                    children[2].generate(info, actions);
                    children[3].generate(info, actions);
                    children[4].generate(info, actions);
                    children[5].generate(info, actions);
                    addLiteral(actions, 1);

                    if (children[1].getType() == BooleanLiteral) {
                        addLiteral(actions, children[1].bValue ? 1 : 0);
                    } else {
                        children[1].generate(info, actions);
                    }
                } else if (count == 2) {
                    addLiteral(actions, 0);

                    if (children[1].getType() == BooleanLiteral) {
                        addLiteral(actions, children[1].bValue ? 1 : 0);
                    } else {
                        children[1].generate(info, actions);
                    }
                } else {
                    addLiteral(actions, 0);
                    addLiteral(actions, 0);
                }
                children[0].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.START_DRAG));
            } else if (sValue.toLowerCase().equals("stop")) {
                actions.add(BasicAction.fromInt(ActionTypes.STOP));
            } else if (sValue.toLowerCase().equals("stopallsounds")) {
                actions.add(BasicAction.fromInt(ActionTypes.STOP_SOUNDS));
            } else if (sValue.toLowerCase().equals("stopdrag")) {
                actions.add(BasicAction.fromInt(ActionTypes.END_DRAG));
            } else if (sValue.toLowerCase().equals("string")) {
                children[0].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.TO_STRING));
            } else if (sValue.toLowerCase().equals("substring")) {
                for (int i = 0; i < count; i++)
                    children[i].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.STRING_EXTRACT));
            } else if (sValue.toLowerCase().equals("targetpath")) {
                for (int i = 0; i < count; i++)
                    children[i].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.GET_TARGET));
            } else if (sValue.toLowerCase().equals("togglehighquality")) {
                actions.add(BasicAction.fromInt(ActionTypes.TOGGLE_QUALITY));
            } else if (sValue.toLowerCase().equals("trace")) {
                for (int i = 0; i < count; i++)
                    children[i].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.TRACE));
            } else if (sValue.toLowerCase().equals("typeof")) {
                for (int i = 0; i < count; i++)
                    children[i].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.GET_TYPE));
            } else if (sValue.toLowerCase().equals("unloadmovie")) {
                if (children[0].sValue == null) {
                    actions.add(new GetUrl("", "_level" + children[0].iValue));
                } else {
                    addLiteral(actions, "");
                    children[0].generate(info, actions);
                    actions.add(new GetUrl2(GetUrl2.Request.MOVIE_TO_TARGET));
                }
            } else if (sValue.toLowerCase().equals("unloadmovienum")) {
                if (children[0].sValue == null) {
                    actions.add(new GetUrl("", "_level" + children[0].iValue));
                } else {
                    addLiteral(actions, "");
                    children[0].generate(info, actions);
                    actions.add(new GetUrl2(GetUrl2.Request.MOVIE_TO_TARGET));
                }
            } else if (sValue.toLowerCase().equals("void")) {
                for (int i = 0; i < count; i++)
                    children[i].generate(info, actions);

                actions.add(BasicAction.fromInt(ActionTypes.POP));
                addLiteral(actions, Void.getInstance());
            } else {
                for (int i = 0; i < count; i++)
                    children[i].generate(info, actions);

                addReference(actions, info, name);
                actions.add(BasicAction.fromInt(ActionTypes.EXECUTE_FUNCTION));
            }

            if ((functions.get(name.toLowerCase())).booleanValue()) {
                if (discardValue)
                    actions.add(BasicAction.fromInt(ActionTypes.POP));
            }
        } else {
            if (sValue.toLowerCase().equals("parseint")) {
                for (int i = count - 1; i >= 0; i--)
                    children[i].generate(info, actions);

                addLiteral(actions, count);
                addReference(actions, info, name);
                actions.add(BasicAction.fromInt(ActionTypes.EXECUTE_FUNCTION));
            } else if (sValue.toLowerCase().equals("updateafterevent")) {
                for (int i = count - 1; i >= 0; i--)
                    children[i].generate(info, actions);

                addLiteral(actions, count);
                addReference(actions, info, name);
                actions.add(BasicAction.fromInt(ActionTypes.EXECUTE_FUNCTION));
            } else {
                for (int i = count - 1; i >= 0; i--)
                    children[i].generate(info, actions);

                addLiteral(actions, count);
                addReference(actions, info, name);
                actions.add(BasicAction.fromInt(ActionTypes.EXECUTE_FUNCTION));

                if (valueFunctions.containsKey(name.toLowerCase()) == false) {
                    if (discardValue)
                        actions.add(BasicAction.fromInt(ActionTypes.POP));
                }
            }
        }

        if (valueFunctions.containsKey(name.toLowerCase())) {
            if (discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
        }
    }

    private void addReference(List<Action> actions, ASContext info, Object literal) {
        if (info.useStrings && info.strings.contains(literal))
            literal = new TableIndex(info.strings.indexOf(literal));

        if (literal instanceof Integer) {
            int value = ((Integer) literal).intValue();

            if (value == 0)
                literal = new Double(0.0);
        }

        if (actions.size() > 0) {
            int index = actions.size() - 1;
            Action action = actions.get(index);

            if (action instanceof Push) {
                List<Object>values = ((Push) action).getValues();
                values.add(literal);
                actions.set(index, new Push(values));
            } else {
                actions.add(new Push.Builder().add(literal).build());
            }
        } else {
            actions.add(new Push.Builder().add(literal).build());
        }
    }

    private void addLiteral(List<Action> actions, int value) {
        Object number = null;

        if (value == 0)
            number = new Double(0.0);
        else
            number = new Integer(value);

        if (actions.size() > 0) {
            int index = actions.size() - 1;
            Action action = actions.get(index);

            if (action instanceof Push) {
                List<Object>values = ((Push) action).getValues();
                values.add(number);
                actions.set(index, new Push(values));
            }
            else {
                actions.add(new Push.Builder().add(number).build());
            }
        } else {
            actions.add(new Push.Builder().add(number).build());
        }
    }

    private void addLiteral(List<Action> actions, Object literal) {
        Action action = null;
        int index = actions.size() - 1;

        if (literal instanceof Integer) {
            int value = ((Integer) literal).intValue();

            if (value == 0)
                literal = new Double(0.0);
        }

        if (actions.size() > 0) {
            action = actions.get(index);
        }
        if (action instanceof Push) {
            List<Object>values = ((Push) action).getValues();
            values.add(literal);
            actions.set(index, new Push(values));
        } else {
            actions.add(new Push.Builder().add(literal).build());
        }
    }

    private int actionLength(List<Action> array, ASContext info) {
        int length = 0;

        for (Iterator<Action> i = array.iterator(); i.hasNext();) {
            Action action = i.next();

            length += action.prepareToEncode(info);
        }
        return length;
    }

    protected void discardValues() {
        discardValue = true;

        if (type == List || type == StatementList) {
            int count = count();

            for (int i = 0; i < count; i++)
                children[i].discardValues();
        }
    }

    /*
     * validate is used to provide additional error checking not covered in the
     * parser grammar.
     */
    public void validate() throws ParseException {
        boolean reportError = false;
        int count = count();
        ASNode node = this;

        switch (type) {
        case Button:
            /*
             * Check scripts for button only contain on() statements.
             */
            for (int i = 0; i < count; i++) {
                if (children[i].type != On)
                    reportError = true;
            }
            if (reportError)
                reportError("OnOnly", number);
            break;
        case MovieClip:
            /*
             * Check scripts for movie clips only contain onClipEvent()
             * statements.
             */
            for (int i = 0; i < count; i++) {
                if (children[i].getType() != OnClipEvent)
                    reportError = true;
            }
            if (reportError)
                reportError("OnClipEventOnly", number);
            break;
        case Break:
            reportError = true;
            while (node != null) {
                if (node.type == For || node.type == ForIn || node.type == Do
                        || node.type == While || node.type == Switch)
                    reportError = false;

                node = node.parent;
            }
            if (reportError)
                reportError("CannotUseBreak", number);
            break;
        case Continue:
            reportError = true;
            while (node != null) {
                if (node.type == For || node.type == ForIn || node.type == Do
                        || node.type == While)
                    reportError = false;

                node = node.parent;
            }
            if (reportError)
                reportError("CannotUseContinue", number);
            break;
        case Return:
            reportError = true;
            while (node != null) {
                if (node.type == DefineFunction || node.type == DefineMethod)
                    reportError = false;

                node = node.parent;
            }
            if (reportError)
                reportError("CannotUseReturn", number);
            break;
        case Function:
            /*
             * Check the number of arguments are supplied to built in Flash
             * functions. Some addition checking of attributes is also carried
             * out on a per function basis.
             */
            if (sValue.toLowerCase().equals("delete")) {
                if (count != 1)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("duplicatemovieclip")) {
                if (count != 3)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("escape")) {
                if (count != 1)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("eval")) {
                if (count != 1)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("fscommand")) {
                if (count < 1)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("getproperty")) {
                if (count != 2)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("geturl")) {
                if (count < 1 || count > 3)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("getversion")) {
                if (count != 0)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("gotoandplay")) {
                if (count < 1)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("gotoandstop")) {
                if (count < 1)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("hittest")) {
                if (count < 1 || count > 3)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("isfinite")) {
                if (count != 1)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("isnan")) {
                if (count != 1)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("loadmovie")) {
                if (count < 1 || count > 3)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("loadvariables")) {
                if (count < 1 || count > 3)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("nextframe")) {
                if (count != 0)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("nextscene")) {
                if (count != 0)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("number")) {
                if (count != 1)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("parseint")) {
                if (count < 1 || count > 2)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("parsefloat")) {
                if (count != 1)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("play")) {
                if (count != 0)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("prevframe")) {
                if (count != 0)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("prevscene")) {
                if (count != 0)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("print")) {
                if (count != 2)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("printasbitmap")) {
                if (count != 2)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("removemovieclip")) {
                if (count != 1)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("set")) {
                if (count != 2)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("setproperty")) {
                if (count != 3)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("startdrag")) {
                if ((count == 1 || count == 2 || count == 6) == false)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("stop")) {
                if (count != 0)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("stopallsounds")) {
                if (count != 0)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("stopdrag")) {
                if (count != 0)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("string")) {
                if (count != 1)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("targetpath")) {
                if (count != 1)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("togglehighquality")) {
                if (count != 0)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("trace")) {
                if (count != 1)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("typeof")) {
                if (count != 1)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("unescape")) {
                if (count != 1)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("unloadmovie")) {
                if (count != 1)
                    reportError("IncorrectArgumentCount", number);
            } else if (sValue.toLowerCase().equals("void")) {
                if (count != 1)
                    reportError("IncorrectArgumentCount", number);
            }
            break;
        }

        for (int i = 0; i < count; i++)
            children[i].validate();
    }

    private void reportError(String errorKey, int number) throws ParseException {
        ParseException parseError = new ParseException(errorKey);

        parseError.currentToken = new Token();
        parseError.currentToken.beginLine = number;

        throw parseError;
    }
}
