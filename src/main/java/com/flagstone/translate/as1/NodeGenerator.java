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
package com.flagstone.translate.as1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.flagstone.transform.EventHandler;
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
import com.flagstone.transform.coder.SWFEncodeable;
import com.flagstone.translate.AbstractCodeGenerator;
import com.flagstone.translate.Context;
import com.flagstone.translate.EventNode;
import com.flagstone.translate.Generator;
import com.flagstone.translate.Node;
import com.flagstone.translate.NodeType;
import com.flagstone.translate.as.ParseException;
import com.flagstone.translate.as.Token;

public class NodeGenerator extends AbstractCodeGenerator {

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

    private void generateScript(final Generator generator, final Context info, final Node node, List<Action> list) {
        int count = node.count();

        switch (node.getType()) {
        case ARRAY:
            if (node.getType() == NodeType.ARRAY && info.useStrings)
                list.add(new Table(info.strings));

            for (int i = 0; i < count; i++)
                node.get(i).discardValues();

            for (int i = 0; i < count; i++)
                generate(generator, info, node.get(i), list);

            list.add(BasicAction.END);
            break;
        case BUTTON:
        case MOVIE_CLIP:
            for (int i = 0; i < count; i++)
                generateEvent(generator, info, (EventNode)node.get(i), list);
            break;
        default:
            break;
        }
    }

    public void generateEvent(final Generator generator, Context info, final EventNode node, List<Action> list) {
        List<Action> array = new ArrayList<Action>();
        int count = node.count();

        if (info.useStrings)
            array.add(new Table(info.strings));

        for (int i = 0; i < count; i++)
            node.get(i).discardValues();

        for (int i = 0; i < count; i++)
        	generate(generator, info, node.get(i), array);

        array.add(BasicAction.END);
        list.add(new EventHandler(node.getEvents(), node.getKey(), array));
    }

    @Override
	public void reorder(final Generator generator, final Context info, final Node node) {
        switch (node.getType()) {
        case ARRAY:
        case BUTTON:
        case MOVIE_CLIP:
            info.nodes.push(node);
            break;
        case IDENTIFIER:
            if (constants.containsKey(node.getValue())) {
                node.setType(NodeType.IDENTIFIER);
                Object value = constants.get(node.getValue());

                if (value != null && value instanceof String) {
                	node.setType(NodeType.STRING);
                    node.setValue(value.toString());
                }
            }
            break;
        case VALUE:
            if (node.get(0).getType() == NodeType.IDENTIFIER && node.get(1).getType() == NodeType.ATTRIBUTE) {
                String name = node.get(0).getValue() + "." + node.get(1).getValue();

                if (constants.containsKey(name)) {
                    node.setType(NodeType.IDENTIFIER);
                    node.setValue(name);

                    node.remove(0);
                    node.remove(0);
                }
            }
            break;
        case ASSIGN:
            if (node.getParent() != null && node.getParent().getType() == NodeType.LIST && node.getParent().count() > 0) {
                if (node.getParent().get(0).count() > 0) {
                    if (node.getParent().get(0).get(0).getType() == NodeType.DEFINE_VARIABLE) {
                        if (node.getParent().getChildren().indexOf(node) != 0)
                            node.get(0).setType(NodeType.DEFINE_VARIABLE);
                    }
                }
            }
            break;
        case DEFINE_FUNCTION:
            int index = node.getParent().getChildren().indexOf(node);

            if (index != -1) {
                node.getParent().remove(index);
                Node aNode = info.nodes.peek();
                aNode.insert(aNode.insertIndex++, node);
            }

            info.nodes.push(node);
            break;
        case FUNCTION:
            if (node.getValue().equals("fscommand")) {
                if (node.get(0).getType() == NodeType.STRING)
                    node.get(0).setValue("FSCommand:" + node.get(0).getValue());
            } else if (node.getValue().equals("print")) {
                Node c0 = node.get(0);
                Node c1 = node.get(1);

                if (node.get(1).getValue().equals("bmovie"))
                    node.get(1).setValue("print:");
                else
                    node.get(1).setValue("print:#" + node.get(1).getValue());

                node.getChildren().set(0, c1);
                node.getChildren().set(1, c0);
            } else if (node.getValue().equals("printNum")) {
                if (node.get(0).getType() == NodeType.INTEGER) {
                    node.get(0).setType(NodeType.STRING);
                    node.get(0).setValue("_level" + node.get(0).getValue());
                }
                if (node.get(1).getValue().equals("bmovie")) {
                    node.get(1).setValue("print:");
                } else {
                    node.get(1).setValue("print:#" + node.get(1).getValue());
                }
            } else if (node.getValue().equals("printAsBitmap")) {
                Node c0 = node.get(0);
                Node c1 = node.get(1);

                if (node.get(1).getValue().equals("bmovie"))
                    node.get(1).setValue("printasbitmap:");
                else
                    node.get(1).setValue("printasbitmap:#" + node.get(1).getValue());

                node.getChildren().set(0, c1);
                node.getChildren().set(1, c0);
            } else if (node.getValue().equals("printAsBitmapNum")) {
                if (node.get(0).getType() == NodeType.INTEGER) {
                    node.get(0).setType(NodeType.STRING);
                    node.get(0).setValue("_level" + node.get(0).getValue());
                }
                if (node.get(1).getValue().equals("bmovie"))
                    node.get(1).setValue("printasbitmap:");
                else
                    node.get(1).setValue("printasbitmap:#" + node.get(1).getValue());
            }
            break;
        }

        /*
         * reorder any child nodes before reordering any binary operators to
         * ensure any integer literals are evaluated first.
         */
        int count = node.count();

        for (int i = 0; i < count; i++)
            reorder(generator, info, node.get(i));

        switch (node.getType()) {
        case ARRAY:
        case ADD:
        case SUB:
        case MUL:
        case DIV:
        case MOD:
            if (node.count() == 2) {
                if (node.get(0).getType() == NodeType.INTEGER
                        && node.get(1).getType() == NodeType.INTEGER) {
                    switch (node.getType()) {
                    case ADD:
                        node.setType(NodeType.INTEGER);
                        node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) + Integer.valueOf(node.get(1).getValue())));
                        break;
                    case SUB:
                    	node.setType(NodeType.INTEGER);
                        node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) - Integer.valueOf(node.get(1).getValue())));
                        break;
                    case MUL:
                    	node.setType(NodeType.INTEGER);
                        node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) * Integer.valueOf(node.get(1).getValue())));
                        break;
                    case DIV:
                        if (Integer.valueOf(node.get(0).getValue()) / Integer.valueOf(node.get(1).getValue()) == 0) {
                        	node.setType(NodeType.DOUBLE);
                        	node.setValue(String.valueOf(((double) Integer.valueOf(node.get(0).getValue()))
                                    / ((double) Integer.valueOf(node.get(1).getValue()))));
                        } else if (Integer.valueOf(node.get(0).getValue()) % Integer.valueOf(node.get(1).getValue()) != 0) {
                        	node.setType(NodeType.DOUBLE);
                        	node.setValue(String.valueOf(((double) Integer.valueOf(node.get(0).getValue()))
                                    / ((double) Integer.valueOf(node.get(1).getValue()))));
                        } else {
                            node.setType(NodeType.INTEGER);
                            node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) / Integer.valueOf(node.get(1).getValue())));
                        }
                        break;
                    case MOD:
                    	node.setType(NodeType.INTEGER);
                        node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) % Integer.valueOf(node.get(1).getValue())));
                        break;
                    }
                    node.remove(0);
                    node.remove(0);
                } else if (node.get(0).getType() == NodeType.DOUBLE
                        && node.get(1).getType() == NodeType.INTEGER) {
                    switch (node.getType()) {
                    case ADD:
                        node.setValue(String.valueOf(Double.valueOf(node.get(0).getValue()) + Integer.valueOf(node.get(1).getValue())));
                        break;
                    case SUB:
                    	node.setValue(String.valueOf(Double.valueOf(node.get(0).getValue()) - Integer.valueOf(node.get(1).getValue())));
                        break;
                    case MUL:
                    	node.setValue(String.valueOf(Double.valueOf(node.get(0).getValue()) * Integer.valueOf(node.get(1).getValue())));
                        break;
                    case DIV:
                    	node.setValue(String.valueOf(Double.valueOf(node.get(0).getValue()) / Integer.valueOf(node.get(1).getValue())));
                        break;
                    case MOD:
                    	node.setValue(String.valueOf(Double.valueOf(node.get(0).getValue()) % Integer.valueOf(node.get(1).getValue())));
                        break;
                    }
                    node.setType(NodeType.DOUBLE);
                    node.remove(0);
                    node.remove(0);
                } else if (node.get(0).getType() == NodeType.INTEGER
                        && node.get(1).getType() == NodeType.DOUBLE) {
                    switch (node.getType()) {
                    case ADD:
                    	node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) + Double.valueOf(node.get(1).getValue())));
                        break;
                    case SUB:
                        node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) - Double.valueOf(node.get(1).getValue())));
                        break;
                    case MUL:
                    	node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) * Double.valueOf(node.get(1).getValue())));
                        break;
                    case DIV:
                    	node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) / Double.valueOf(node.get(1).getValue())));
                        break;
                    case MOD:
                    	node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) % Double.valueOf(node.get(1).getValue())));
                        break;
                    }
                    node.setType(NodeType.DOUBLE);
                    node.remove(0);
                    node.remove(0);
                } else if (node.get(0).getType() == NodeType.DOUBLE
                        && node.get(1).getType() == NodeType.DOUBLE) {
                    switch (node.getType()) {
                    case ADD:
                    	node.setValue(String.valueOf(Double.valueOf(node.get(0).getValue()) + Double.valueOf(node.get(1).getValue())));
                        break;
                    case SUB:
                    	node.setValue(String.valueOf(Double.valueOf(node.get(0).getValue()) - Double.valueOf(node.get(1).getValue())));
                        break;
                    case MUL:
                    	node.setValue(String.valueOf(Double.valueOf(node.get(0).getValue()) * Double.valueOf(node.get(1).getValue())));
                        break;
                    case DIV:
                    	node.setValue(String.valueOf(Double.valueOf(node.get(0).getValue()) / Double.valueOf(node.get(1).getValue())));
                        break;
                    case MOD:
                    	node.setValue(String.valueOf(Double.valueOf(node.get(0).getValue()) % Double.valueOf(node.get(1).getValue())));
                        break;
                    }
                    node.setType(NodeType.DOUBLE);
                    node.remove(0);
                    node.remove(0);
                } else if (node.get(0).getType() == NodeType.STRING
                        || node.get(1).getType() == NodeType.STRING) {
                    String aValue = null;
                    String bValue = null;
                    switch (node.getType()) {
                    case ADD:
                        if (node.get(0).getType() == NodeType.STRING) {
                            aValue = node.get(0).getValue();
                        } else if (node.get(0).getType() == NodeType.INTEGER) {
                            aValue = node.get(0).getValue();
                        }
                        if (node.get(1).getType() == NodeType.STRING) {
                            bValue = node.get(1).getValue();
                        } else if (node.get(1).getType() == NodeType.INTEGER) {
                            bValue = node.get(1).getValue();
                        }
                        break;
                    }
                    if (aValue != null && bValue != null) {
                        node.setValue(aValue + bValue);
                        node.setType(NodeType.STRING);
                        node.remove(0);
                        node.remove(0);
                    }
                }
            }
            break;
        case ASR:
        case LSL:
        case LSR:
        case BIT_AND:
        case BIT_OR:
        case BIT_XOR:
            if (node.count() == 2) {
                if (node.get(0).getType() == NodeType.INTEGER
                        && node.get(1).getType() == NodeType.INTEGER) {
                    switch (node.getType()) {
                    case ASR:
                        node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) >> Integer.valueOf(node.get(1).getValue())));
                        break;
                    case LSL:
                        node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) << Integer.valueOf(node.get(1).getValue())));
                        break;
                    case LSR:
                        node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) >>> Integer.valueOf(node.get(1).getValue())));
                        break;
                    case BIT_AND:
                        node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) & Integer.valueOf(node.get(1).getValue())));
                        break;
                    case BIT_OR:
                        node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) | Integer.valueOf(node.get(1).getValue())));
                        break;
                    case BIT_XOR:
                        node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) ^ Integer.valueOf(node.get(1).getValue())));
                        break;
                    }
                    node.setType(NodeType.INTEGER);
                    node.remove(0);
                    node.remove(0);
                }
            }
            break;

        case AND:
        case LOGICAL_AND:
            if (node.count() == 2) {
//                if (node.get(0).getType() == NodeType.INTEGER) {
//                    node.get(0).getType() = BooleanLiteral;
//                    node.get(0).bValue = node.get(0).iValue != 0;
//                    node.get(0).iValue = 0;
//                }
//                if (node.get(1).getType() == NodeType.INTEGER) {
//                    node.get(1).getType() = BooleanLiteral;
//                    node.get(1).bValue = node.get(1).iValue != 0;
//                    node.get(1).iValue = 0;
//                }
//                if (node.get(0).getType() == NodeType.DOubleLiteral) {
//                    node.get(0).getType() = BooleanLiteral;
//                    node.get(0).bValue = node.get(0).dValue != 0.0;
//                    node.get(0).dValue = 0.0;
//                }
//                if (node.get(1).getType() == NodeType.DOubleLiteral) {
//                    node.get(1).getType() = BooleanLiteral;
//                    node.get(1).bValue = node.get(1).dValue != 0;
//                    node.get(1).dValue = 0.0;
//                }

                if (node.get(0).getType() == NodeType.BOOLEAN
                        && node.get(1).getType() == NodeType.BOOLEAN) {
                    switch (node.getType()) {
                    case LOGICAL_AND:
                    case AND:
                        node.setType(NodeType.BOOLEAN);
                        node.setValue(String.valueOf(Boolean.valueOf(node.get(0).getValue()) && Boolean.valueOf(node.get(1).getValue())));
                        break;
                    }
                    node.remove(0);
                    node.remove(0);
                } else if (node.get(0).getType() == NodeType.BOOLEAN
                        && node.get(1).getType() == NodeType.INTEGER) {
                    switch (node.getType()) {
                    case AND:
                    case LOGICAL_AND:
                        node.setType(NodeType.BOOLEAN);
                        node.setValue(String.valueOf(Boolean.valueOf(node.get(0).getValue())
                                && Integer.valueOf(node.get(1).getValue()) != 0));
                        break;
                    }
                    node.remove(0);
                    node.remove(0);
                } else if (node.get(0).getType() == NodeType.INTEGER
                        && node.get(1).getType() == NodeType.BOOLEAN) {
                    switch (node.getType()) {
                    case LOGICAL_AND:
                    case AND:
                        node.setType(NodeType.BOOLEAN);
                        node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) != 0
                                && Boolean.valueOf(node.get(1).getValue())));
                        break;
                    }
                    node.remove(0);
                    node.remove(0);
                } else if (node.get(0).getType() == NodeType.INTEGER
                        && node.get(1).getType() == NodeType.INTEGER) {
                    boolean a = Integer.valueOf(node.get(0).getValue()) != 0;
                    boolean b = Integer.valueOf(node.get(1).getValue()) != 0;

                    switch (node.getType()) {
                    case LOGICAL_AND:
                    case AND:
                        node.setType(NodeType.INTEGER);
                        node.setValue(a ? node.get(1).getValue() : "0");
                        break;
                    }
                    node.remove(0);
                    node.remove(0);
                }
            }
            break;
        case OR:
        case LOGICAL_OR:
            if (node.count() == 2) {
                if (node.get(0).getType() == NodeType.BOOLEAN
                        && node.get(1).getType() == NodeType.BOOLEAN) {
                    switch (node.getType()) {
                    case LOGICAL_OR:
                    case OR:
                        node.setType(NodeType.BOOLEAN);
                        node.setValue(String.valueOf(Boolean.valueOf(node.get(0).getValue()) || Boolean.valueOf(node.get(1).getValue())));
                        break;
                    }
                    node.remove(0);
                    node.remove(0);
                } else if (node.get(0).getType() == NodeType.BOOLEAN
                        && node.get(1).getType() == NodeType.INTEGER) {
                    switch (node.getType()) {
                    case LOGICAL_OR:
                    case OR:
                        node.setType(NodeType.INTEGER);
                        node.setValue(node.get(1).getValue());
                        break;
                    }
                    node.remove(0);
                    node.remove(0);
                } else if (node.get(0).getType() == NodeType.INTEGER
                        && node.get(1).getType() == NodeType.BOOLEAN) {
                    switch (node.getType()) {
                    case LOGICAL_OR:
                    case OR:
                        node.setType(NodeType.INTEGER);
                        node.setValue(String.valueOf((Integer.valueOf(node.get(0).getValue()) != 0 || Boolean.valueOf(node.get(1).getValue())) ? 1
                                : 0));
                        break;
                    }
                    node.remove(0);
                    node.remove(0);
                } else if (node.get(0).getType() == NodeType.INTEGER
                        && node.get(1).getType() == NodeType.INTEGER) {
                    boolean a = Integer.valueOf(node.get(0).getValue()) != 0;
                    boolean b = Integer.valueOf(node.get(1).getValue()) != 0;

                    switch (node.getType()) {
                    case LOGICAL_OR:
                    case OR:
                        node.setType(NodeType.INTEGER);
                        node.setValue(String.valueOf(a || b ? 1 : 0));
                        break;
                    }
                    node.remove(0);
                    node.remove(0);
                }
            }
            break;
        case NOT:
            if (node.count() == 1) {
                if (node.get(0).getType() == NodeType.BOOLEAN) {
                    node.setType(NodeType.BOOLEAN);
                    node.setValue(String.valueOf(!Boolean.valueOf(node.get(0).getValue())));
                    node.remove(0);
                } else if (node.get(0).getType() == NodeType.INTEGER) {
                    node.setType(NodeType.BOOLEAN);
                    node.setValue(String.valueOf(Integer.valueOf(node.get(0).getValue()) == 0));
                    node.remove(0);
                }
            }
            break;
        case BIT_NOT:
            if (node.count() == 1) {
                if (node.get(0).getType() == NodeType.INTEGER) {
                    node.setType(NodeType.INTEGER);
                    node.setValue(String.valueOf(~Integer.valueOf(node.get(0).getValue())));
                    node.remove(0);
                }
            }
            break;
        default:
            break;
        }

        switch (node.getType()) {
        case ARRAY:
        case BUTTON:
        case MOVIE_CLIP:
        case DEFINE_FUNCTION:
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
     * @param info is an Context object that passes context information between
     * nodes.
     */
    @Override
	public void search(final Generator generator, final Context info, final Node node) {
        int count = node.count();

        if (node.getType() == NodeType.FUNCTION)
            info.context.push(node.getValue());
        else
            info.context.push(node.getType().getName());

        switch (node.getType()) {
//        case On:
//        case OnClipEvent:
//            info.clearStrings();
//            for (int i = 0; i < count; i++)
//                search(generator, info, node.get(i));
//            break;
        case STRING:
        	String value = node.getValue().replaceAll("\\\\n", "\n");
            value = value.replaceAll("\\\\n", "\n");
            value = value.replaceAll("\\\\t", "\t");
            value = value.replaceAll("\\\\b", "\b");
            value = value.replaceAll("\\\\r", "\r");
            value = value.replaceAll("\\\\f", "\f");

            node.setValue(value);
            info.addString(value);
            break;
        case IDENTIFIER:
            if (constants.containsKey(node.getValue()))
                break;
            else if (propertyNames.containsKey(node.getValue())) {
                if (info.context.contains("getProperty"))
                    break;
                else if (info.context.contains("setProperty"))
                    break;
                else if (info.context.contains("with"))
                    info.addString(node.getValue());
                else if (info.context.contains("DefineObject"))
                    info.addString(node.getValue());
                else
                    info.addString("");
                break;
            }
            info.addString(node.getValue());
            break;
        case DEFINE_VARIABLE:
            info.addString(node.getValue());
            break;
        case ATTRIBUTE:
        case METHOD:
        case NEW_OBJECT:

            for (int i = count - 1; i >= 0; i--)
                search(generator, info, node.get(i));

            if (node.getValue().length() > 0)
                info.addString(node.getValue());
            break;
        case FUNCTION:
            if (node.getValue() != null && functions.containsKey(node.getValue().toLowerCase()) == false) {
                for (int i = 0; i < count; i++)
                    search(generator, info, node.get(i));

                if (node.getValue().length() > 0)
                    info.addString(node.getValue());
            } else {
                if (node.getValue() != null && node.getValue().toLowerCase().equals("fscommand")) {
                    info.addString("FSCommand:");

                    for (int i = 0; i < count; i++)
                        search(generator, info, node.get(i));
                } else if (node.getValue() != null && node.getValue().toLowerCase().equals("getURL")) {
                    if (count > 0)
                        search(generator, info, node.get(0));

                    if (count > 1)
                        search(generator, info, node.get(1));

                    if (count == 1 && node.get(0).getType() != NodeType.STRING)
                        info.addString("");

                    break;
                } else if (node.getValue() != null && node.getValue().toLowerCase().equals("gotoAndPlay")) {
                    if (count == 1)
                        search(generator, info, node.get(0));
                    else if (count == 2)
                        search(generator, info, node.get(1));

                    break;
                } else if (node.getValue() != null && node.getValue().toLowerCase().equals("gotoAndStop")) {
                    if (count == 1)
                        search(generator, info, node.get(0));
                    else if (count == 2)
                        search(generator, info, node.get(1));

                    break;
                } else if (node.getValue() != null && node.getValue().toLowerCase().equals("loadMovie")) {
                    if (count > 0)
                        search(generator, info, node.get(0));

                    if (count > 1)
                        search(generator, info, node.get(1));

                    if (count == 1)
                        info.addString("");

                    break;
                } else if (node.getValue() != null && node.getValue().toLowerCase().equals("loadVariables")) {
                    if (count > 0)
                        search(generator, info, node.get(0));

                    if (count > 1)
                        search(generator, info, node.get(1));

                    if (count == 1)
                        info.addString("");

                    break;
                } else if (node.getValue() != null && node.getValue().toLowerCase().equals("printNum")) {
                    search(generator, info, node.get(1));

                    if (node.get(0).getType() == NodeType.IDENTIFIER)
                        info.addString("_level");

                    search(generator, info, node.get(0));
                } else if (node.getValue() != null && node.getValue().toLowerCase().equals("printAsBitmapNum")) {
                    search(generator, info, node.get(1));

                    if (node.get(0).getType() == NodeType.IDENTIFIER)
                        info.addString("_level");

                    search(generator, info, node.get(0));
                } else {
                    for (int i = 0; i < count; i++)
                        search(generator, info, node.get(i));
                }
            }
            break;
        case DEFINE_METHOD:
            search(generator, info, node.get(count - 1));
            break;
        case DEFINE_FUNCTION:
            if (node.getValue() != null && node.getValue().equals("ifFrameLoaded")) {
                if (node.get(0).count() == 0) {
                    search(generator, info, node.get(0));
                } else if (node.get(0).count() == 2) {
                    search(generator, info, node.get(0).get(1));
                }
            }
            search(generator, info, node.get(count - 1));
            break;
        case DEFINE_ARRAY:
            for (int i = count - 1; i >= 0; i--)
                search(generator, info, node.get(i));
            break;
        case VALUE:
            if (count > 0) {
                if (node.get(0).getValue() != null
                        && classes.containsKey(node.get(0).getValue())) {
                    boolean containsClass = false;

                    for (Iterator<String> i = info.strings.iterator(); i
                            .hasNext();) {
                        if (i.next().toString().equals(node.get(0).getValue())) {
                            containsClass = true;
                            break;
                        }
                    }
                    // Swap the name of the function and the class to
                    // simplify verification during testing.

                    if (containsClass == false) {
                        int index = info.strings.size();

                        for (int i = 0; i < count; i++)
                            search(generator, info, node.get(i));

                        info.strings.set(index, info.strings.get(index + 1));
                        info.strings.set(index + 1, node.get(0).getValue());
                    } else {
                        for (int i = 0; i < count; i++)
                            search(generator, info, node.get(i));
                    }
                } else {
                    for (int i = 0; i < count; i++)
                        search(generator, info, node.get(i));
                }
            }
            break;
        default:
            for (int i = 0; i < count; i++)
                search(generator, info, node.get(i));
            break;
        }
        info.context.pop();
    }

    /*
     * generate 'compiles' ActionScript statements that this node and all child
     * nodes represent into the set of actions that will be executed by the
     * Flash Player.
     * @param info an Context object that is used to pass context and context
     * information between nodes. This should be the same object used when
     * preprocessing modes.
     * @param actions an array that the compiled actions will be added to.
     */
    @Override
	public void generate(final Generator generator, final Context info, final Node node, List<Action> actions) {
        if (node.getType() == NodeType.FUNCTION)
            info.context.push(node.getValue());
        else
            info.context.push(node.getType().getName());

        switch (node.getType()) {
        case ARRAY:
        case BUTTON:
        case MOVIE_CLIP:
            generateScript(generator, info, node, actions);
            break;
        case STATEMENT_LIST:
        case LIST:
            generateList(generator, info, node, actions);
            break;
        case IF:
            generateIf(generator, info, node, actions);
            break;
        case DO:
            generateDo(generator, info, node, actions);
            break;
        case WHILE:
            generateWhile(generator, info, node, actions);
            break;
        case FOR:
            generateFor(generator, info, node, actions);
            break;
        case FORIN:
            generateForIn(generator, info, node, actions);
            break;
        case WITH:
            generateWith(generator, info, node, actions);
            break;
        case SWITCH:
            generateSwitch(generator, info, node, actions);
            break;
        case LABEL:
            generateLabel(generator, info, node, actions);
            break;
        case EXCEPTION:
            generateException(generator, info, node, actions);
            break;
        case TRY:
        case CATCH:
        case FINALLY:
            generateClauses(generator, info, node, actions);
            break;
//        case OnClipEvent:
//            generateOnClipEvent(generator, info, node, actions);
//            break;
//        case On:
//            generateOn(generator, info, node, actions);
//            break;
        case BREAK:
        case CONTINUE:
        case RETURN:
            generateReturn(generator, info, node, actions);
            break;
        case VALUE:
        case BOOLEAN:
        case INTEGER:
        case DOUBLE:
        case STRING:
        case NULL:
        case IDENTIFIER:
        case ATTRIBUTE:
        case METHOD:
        case NEW_OBJECT:
        case SUBSCRIPT:
            generateValue(generator, info, node, actions);
            break;
        case FUNCTION:
            generateFunction(generator, info, node, actions);
            break;
        case DEFINE_ARRAY:
        case DEFINE_OBJECT:
        case DEFINE_FUNCTION:
        case DEFINE_METHOD:
        case DEFINE_ATTRIBUTE:
        case DEFINE_VARIABLE:
            generateDefinition(generator, info, node, actions);
            break;
        case PRE_INC:
        case PRE_DEC:
        case POST_INC:
        case POST_DEC:
        case PLUS:
        case MINUS:
        case NOT:
        case BIT_NOT:
        case DELETE:
        case THROW:
            generateUnary(generator, info, node, actions);
            break;
        case STRING_ADD:
        case ADD:
        case SUB:
        case MUL:
        case DIV:
        case MOD:
        case BIT_AND:
        case BIT_OR:
        case BIT_XOR:
        case LSL:
        case LSR:
        case ASR:
        case EQUAL:
        case NOT_EQUAL:
        case LESS_THAN:
        case GREATER_THAN:
        case LESS_THAN_EQUAL:
        case GREATER_THAN_EQUAL:
        case STRING_EQUAL:
        case STRING_NOT_EQUAL:
        case STRING_GREATER_THAN:
        case STRING_LESS_THAN_EQUAL:
        case STRING_GREATER_THAN_EQUAL:
        case AND:
        case OR:
        case LOGICAL_AND:
        case LOGICAL_OR:
        case INSTANCEOF:
        case STRICT_EQUAL:
        case STRICT_NOT_EQUAL:
            generateBinary(generator, info, node, actions);
            break;
        case SELECT:
            generateSelect(generator, info, node, actions);
            break;
        case ASSIGN:
        case ASSIGN_ADD:
        case ASSIGN_SUB:
        case ASSIGN_MUL:
        case ASSIGN_DIV:
        case ASSIGN_MOD:
        case ASSIGN_BIT_AND:
        case ASSIGN_BIT_OR:
        case ASSIGN_BIT_XOR:
        case ASSIGN_LSL:
        case ASSIGN_LSR:
        case ASSIGN_ASR:
            generateAssignment(generator, info, node, actions);
            break;
        default:
            break;
        }
        info.context.pop();
    }

    private void generateList(final Generator generator, final Context info, final Node node, List<Action> actions) {
        int count = node.count();

        for (int i = 0; i < count; i++)
            generate(generator, info, node.get(i), actions);
    }

    private void generateIf(final Generator generator, final Context info, final Node node, List<Action> actions) {
        int count = node.count();
        boolean addJump = false;

        List<Action> trueActions = new ArrayList<Action>();
        int offsetToNext = 0;

        List<Action> falseActions = new ArrayList<Action>();
        int offsetToEnd = 0;

        if (count > 1) {
            node.get(1).discardValues();
            generate(generator, info, node.get(1), trueActions);
            offsetToNext = actionLength(trueActions, info);
        }

        if (count == 3) {
            node.get(2).discardValues();
            generate(generator, info, node.get(2), falseActions);
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

        generate(generator, info, node.get(0), actions);

        actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));
        actions.add(new If(offsetToNext));

        actions.addAll(trueActions);

        if (addJump == true && offsetToEnd > 0)
            actions.add(new Jump(offsetToEnd));

        actions.addAll(falseActions);
    }

    private void generateDo(final Generator generator, final Context info, final Node node, List<Action> actions) {
        ArrayList<Action> blockActions = new ArrayList<Action>();
        int blockLength = 0;

        ArrayList<Action> conditionActions = new ArrayList<Action>();
        int conditionLength = 0;

        node.get(0).discardValues();
        generate(generator, info, node.get(0), blockActions);

        generate(generator, info, node.get(1), conditionActions);

        blockLength = actionLength(blockActions, info);
        conditionLength = actionLength(conditionActions, info);

        conditionLength += 5; // include following if statement

        conditionActions.add(new If(-(blockLength + conditionLength))); // includes
                                                                        // if

        int currentLength = 0;

        // Replace any break and continue place holders with jump statements.

        com.flagstone.transform.coder.Context ctxt = new com.flagstone.transform.coder.Context();

        for (int i = 0; i < blockActions.size(); i++) {
            Action currentAction = blockActions.get(i);

            currentLength += currentAction.prepareToEncode(ctxt);

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

    private void generateWhile(final Generator generator, final Context info, final Node node, List<Action> actions) {
        int count = node.count();

        ArrayList<Action> blockActions = new ArrayList<Action>();
        int blockLength = 0;

        ArrayList<Action> conditionActions = new ArrayList<Action>();
        int conditionLength = 0;

        if (count == 2) {
            node.get(1).discardValues();
            generate(generator, info, node.get(1), blockActions);
        }
        blockLength = actionLength(blockActions, info);

        generate(generator, info, node.get(0), conditionActions);
        conditionActions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));
        conditionActions.add(new If(blockLength + 5)); // includes loop jump
        conditionLength = actionLength(conditionActions, info);

        blockActions.add(new Jump(-(conditionLength + blockLength + 5)));
        blockLength += 5;

        int currentLength = conditionLength;

        // Replace any break and continue place holders with jump statements.

        com.flagstone.transform.coder.Context ctxt = new com.flagstone.transform.coder.Context();

        for (int i = 0; i < blockActions.size(); i++) {
            Action currentAction = blockActions.get(i);

            currentLength += currentAction.prepareToEncode(ctxt);

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

    private void generateFor(final Generator generator, final Context info, final Node node, List<Action> actions) {
        ArrayList<Action> initializeActions = new ArrayList<Action>();
        ArrayList<Action> conditionActions = new ArrayList<Action>();
        ArrayList<Action> iteratorActions = new ArrayList<Action>();
        ArrayList<Action> blockActions = new ArrayList<Action>();

        //int initializeLength = 0;
        int conditionLength = 0;
        int blockLength = 0;
        int iteratorLength = 0;

        if (node.get(0).getType() != NodeType.NO_OP) {
            generate(generator, info, node.get(0), initializeActions);
            //initializeLength = actionLength(initializeActions, info);
        }
        if (node.get(1).getType() != NodeType.NO_OP) {
            generate(generator, info, node.get(1), conditionActions);
            conditionLength = actionLength(conditionActions, info);
        }
        if (node.get(2).getType() != NodeType.NO_OP) {
            node.get(2).discardValues();
            generate(generator, info, node.get(2), iteratorActions);
            iteratorLength = actionLength(iteratorActions, info);
        }
        if (node.get(3).getType() != NodeType.NO_OP) {
            node.get(3).discardValues();
            generate(generator, info, node.get(3), blockActions);
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
        com.flagstone.transform.coder.Context ctxt = new com.flagstone.transform.coder.Context();

        for (int i = 0; i < blockActions.size(); i++) {
            Action currentAction = blockActions.get(i);

            currentLength += currentAction.prepareToEncode(ctxt);

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

    private void generateForIn(final Generator generator, final Context info, final Node node, List<Action> actions) {
        int count = node.count();

        ArrayList<Action> conditionActions = new ArrayList<Action>();
        ArrayList<Action> blockActions = new ArrayList<Action>();

        int conditionLength = 0;
        int blockLength = 0;

        // Push all the attributes of the specified object onto the stack

        switch (info.version) {
        case 5:
            generate(generator, info, node.get(1), actions);
            actions.remove(actions.size() - 1);
            actions.add(BasicAction.fromInt(ActionTypes.ENUMERATE));
            break;
        case 6:
        case 7:
            generate(generator, info, node.get(1), actions);
            actions.add(BasicAction.fromInt(ActionTypes.ENUMERATE_OBJECT));
            break;
        }
        // Set the enumerator variable with the current attribute

        addReference(generator, info, blockActions, node.get(0).getValue());
        addLiteral(blockActions, new RegisterIndex(0));
        blockActions.add(BasicAction.fromInt(ActionTypes.SET_VARIABLE));

        // Translate the body of the for..in statement

        if (count == 3) {
            node.get(2).discardValues();
            generate(generator, info, node.get(2), blockActions);
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

        com.flagstone.transform.coder.Context ctxt = new com.flagstone.transform.coder.Context();

        for (int i = 0; i < blockActions.size(); i++) {
            Action currentAction = blockActions.get(i);

            currentLength += currentAction.prepareToEncode(ctxt);

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

    private void generateWith(final Generator generator, final Context info, final Node node, List<Action> actions) {
        ArrayList<Action> array = new ArrayList<Action>();
        int count = node.count();

        for (int i = 1; i < count; i++)
            node.get(i).discardValues();

        for (int i = 1; i < count; i++)
            generate(generator, info, node.get(i), array);

        generate(generator, info, node.get(0), actions);

        actions.add(new With(array));
    }

    @SuppressWarnings("unchecked")
    private void generateSwitch(final Generator generator, final Context info, final Node node, List<Action> actions) {
        int count = node.count();

        int listCount = 0;
        int labelCount = 0;

        int defaultIndex = -1;
        int defaultTarget = -1;

        for (int i = 0; i < count; i++) {
            if (node.get(i).getType() == NodeType.LIST) {
                listCount += 1;
            } else if (node.get(i).getType() == NodeType.LABEL) {
                if (node.get(i).getChildren().isEmpty()) {
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
            if (node.get(i).getType() == NodeType.LABEL) {
                if (!node.get(i).getChildren().isEmpty()) {
                    if (labelIndex == 0)
                        labelArray[labelIndex].add(new RegisterCopy(0));
                    else
                        addLiteral(labelArray[labelIndex], new RegisterIndex(0));
                }

                generate(generator, info, node.get(i), labelArray[labelIndex]);
                labelLength[labelIndex] = actionLength(labelArray[labelIndex],
                        info);
                labelTarget[labelIndex] = listIndex;
                labelIndex += 1;
            } else if (node.get(i).getType() == NodeType.LIST) {
                generate(generator, info, node.get(i), listArray[listIndex]);
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

        generate(generator, info, node.get(0), actions);

        for (int i = 1; i < count; i++) {
            if (node.get(i).getType() == NodeType.LABEL) {
                if (!node.get(i).getChildren().isEmpty()) {
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

    private void generateLabel(final Generator generator, final Context info, final Node node, List<Action> actions) {
        int count = node.count();

        for (int i = 0; i < count; i++) {
            generate(generator, info, node.get(i), actions);
        }
    }

    @SuppressWarnings("unchecked")
    private void generateException(final Generator generator, final Context info, final Node node, List<Action> actions) {
        int count = node.count();

        ArrayList<Action> actionArray[] = new ArrayList[count];

        for (int i = 0; i < count; i++)
            node.get(i).discardValues();

        for (int i = 0; i < count; i++) {
            actionArray[i] = new ArrayList<Action>();

            generate(generator, info, node.get(i), actionArray[i]);
        }

        actions.add(new ExceptionHandler(101, actionArray[0], actionArray[1],
                actionArray[2]));
    }

    private void generateClauses(final Generator generator, final Context info, final Node node, List<Action> actions) {
        int count = node.count();

        for (int i = 0; i < count; i++)
            node.get(i).discardValues();

        for (int i = 0; i < count; i++) {
            generate(generator, info, node.get(i), actions);
        }
    }

    private void generateReturn(final Generator generator, final Context info, final Node node, List<Action> actions) {
        int count = node.count();

        switch (node.getType()) {
        case BREAK:
            actions.add(new ActionObject(256, new byte[2]));
            break;
        case CONTINUE:
            actions.add(new ActionObject(257, new byte[2]));
            break;
        case RETURN:
            if (count == 0) {
                addLiteral(actions, Void.getInstance());
            } else {
                for (int i = 0; i < count; i++)
                    generate(generator, info, node.get(i), actions);
            }
            actions.add(BasicAction.fromInt(ActionTypes.RETURN));
            break;
        default:
            break;
        }
    }

    private void generateValue(final Generator generator, final Context info, final Node node, List<Action> actions) {
        int count = node.count();

        switch (node.getType()) {
        case VALUE:
            /*
             * If any of the children is a method call then generate the actions
             * for the method arguments. This ensures that the arguments will be
             * popped off the stack in the correct order.
             */
            for (int i = count - 1; i >= 0; i--) {
                if (node.get(i).getType() == NodeType.FUNCTION || node.get(i).getType() == NodeType.METHOD) {
                    List<Node> grandChildren = node.get(i).getChildren();

                    if (grandChildren != null) {
                        int numGrandChildren = grandChildren.size();

                        for (int j = numGrandChildren - 1; j >= 0; j--)
                            generate(generator, info, grandChildren.get(j), actions);

                        addLiteral(actions, numGrandChildren);
                    } else {
                        addLiteral(actions, 0);
                    }
                }
            }

            /*
             * Now generate the actions for each node that returns a value. Note
             * that below methods do not generate actions for their children
             * since the node.getParent() node is always a Value. Functions only do so if
             * the node.getParent() node is not a Value.
             */
            generate(generator, info, node.get(0), actions);

            for (int i = 1; i < count; i++) {
                if (node.get(i).getType() == NodeType.FUNCTION) {
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
                    generate(generator, info, node.get(i), actions);
            }

            if (node.discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case BOOLEAN:
            addLiteral(actions, Boolean.valueOf(node.getValue()));

            if (node.discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case INTEGER:
        	if (node.getValue().toLowerCase().startsWith("0x")) {
        		addLiteral(actions, Integer.valueOf(node.getValue().substring(2), 16));
        	} else {
        		addLiteral(actions, Integer.valueOf(node.getValue()));
        	}
            if (node.discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case DOUBLE:
        	if (node.getValue().endsWith("e")) {
        		node.setValue(node.getValue() + "0");
        	}

            int val = Double.valueOf(node.getValue()).intValue();

            if (node.getValue().equals("-0.0")) {
                addLiteral(actions, Double.valueOf(node.getValue()));
            } else if (Double.valueOf(node.getValue()) == val) {
                addLiteral(actions, Double.valueOf(node.getValue()).intValue());
            } else {
                addLiteral(actions, Double.valueOf(node.getValue()));
            }

            if (node.discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case STRING:
            addReference(generator, info, actions, node.getValue());

            if (node.discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case NULL:
            addLiteral(actions, Null.getInstance());

            if (node.discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case IDENTIFIER:
            if (constants.containsKey(node.getValue())) {
                if (node.getValue().equals("undefined"))
                    addLiteral(actions, Void.getInstance());
                else
                    addLiteral(actions, constants.get(node.getValue()));
            } else if (propertyNames.containsKey(node.getValue())) {
                if (info.context.contains("with")) {
                    addReference(generator, info, actions, node.getValue());
                    actions.add(BasicAction.fromInt(ActionTypes.GET_VARIABLE));
                } else if (info.context.contains("DefineObject")) {
                    addReference(generator, info, actions, node.getValue());
                    actions.add(BasicAction.fromInt(ActionTypes.GET_VARIABLE));
                } else if (info.context.contains("setProperty")) {
                    int pVal = (propertyNames.get(node.getValue())).intValue();

                    if (pVal >= 16 && pVal <= 21)
                        addLiteral(actions, new Integer(pVal));
                    else
                        addLiteral(actions, new Property((earlyPropertyNames
                                .get(node.getValue())).intValue()));
                } else {
                    int pVal = (propertyNames.get(node.getValue())).intValue();

                    addReference(generator, info, actions, "");
                    if (pVal >= 0 && pVal <= 21)
                        addLiteral(actions, new Integer(pVal));
                    else
                        addLiteral(actions, new Property(pVal));
                    actions.add(BasicAction.fromInt(ActionTypes.GET_PROPERTY));
                }
            } else {
                addReference(generator, info, actions, node.getValue());
                actions.add(BasicAction.fromInt(ActionTypes.GET_VARIABLE));
            }
            if (node.discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case ATTRIBUTE:
            addReference(generator, info, actions, node.getValue());
            actions.add(BasicAction.fromInt(ActionTypes.GET_ATTRIBUTE));
            if (node.discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case METHOD:
            addReference(generator, info, actions, node.getValue());
            actions.add(BasicAction.fromInt(ActionTypes.EXECUTE_METHOD));
            break;
        case NEW_OBJECT:
            for (int i = count - 1; i >= 0; i--)
                generate(generator, info, node.get(i), actions);
            addLiteral(actions, count);
            addReference(generator, info, actions, node.getValue());
            actions.add(BasicAction.fromInt(ActionTypes.NAMED_OBJECT));
            break;
        case SUBSCRIPT:
            generate(generator, info, node.get(0), actions);
            actions.add(BasicAction.fromInt(ActionTypes.GET_ATTRIBUTE));
            break;
        default:
            break;
        }
    }

    private void generateDefinition(final Generator generator, final Context info, final Node node, List<Action> actions) {
        int count = node.count();
        int last = count - 1;

        switch (node.getType()) {
        case DEFINE_ARRAY:
            for (int i = last; i >= 0; i--)
                generate(generator, info, node.get(i), actions);
            addLiteral(actions, count);
            actions.add(BasicAction.fromInt(ActionTypes.NEW_ARRAY));
            break;
        case DEFINE_OBJECT:
            for (int i = 0; i < count; i++)
                generate(generator, info, node.get(i), actions);
            addLiteral(actions, count);
            actions.add(BasicAction.fromInt(ActionTypes.NEW_OBJECT));
            break;
        case DEFINE_FUNCTION:

            if (node.getValue().equals("ifFrameLoaded")) {
                List<Action> array = new ArrayList<Action>();

                node.get(count - 1).discardValues();
                generate(generator, info, node.get(count - 1), array);

                if (node.get(0).count() == 0) {
                    generate(generator, info, node.get(0), actions);
                } else if (node.get(0).count() == 2) {
                    generate(generator, info, node.get(0).get(1), actions);
                }

                addLiteral(actions, 0);
                actions.add(BasicAction.fromInt(ActionTypes.ADD));
                actions.add(new WaitForFrame2(array.size()));
                actions.addAll(array);
            } else if (node.getValue().equals("tellTarget")) {
                actions.add(new SetTarget(node.get(0).getValue()));

                generate(generator, info, node.get(1), actions);

                actions.add(new SetTarget(""));
            } else {
                List<String> functionArguments = new ArrayList<String>();
                List<Action> functionActions = new ArrayList<Action>();

                if (node.count() == 2) {
                    if (node.get(0).getType() == NodeType.LIST) {
                        count = node.get(0).count();

                        for (int i = 0; i < count; i++)
                            functionArguments
                                    .add(node.get(0).get(i).getValue());
                    } else {
                        functionArguments.add(node.get(0).getValue());
                    }
                }
                node.get(last).discardValues();
                generate(generator, info, node.get(last), functionActions);

                actions.add(new NewFunction(node.getValue(), functionArguments,
                        functionActions));
            }
            break;
        case DEFINE_METHOD:
            List<String> methodArguments = new ArrayList<String>();
            List<Action> methodActions = new ArrayList<Action>();

            if (node.count() == 2) {
                if (node.get(0).getType() == NodeType.LIST) {
                    count = node.get(0).count();

                    for (int i = 0; i < count; i++)
                        methodArguments.add(node.get(0).get(i).getValue());
                } else {
                    methodArguments.add(node.get(0).getValue());
                }
            }
            node.get(last).discardValues();
            generate(generator, info, node.get(last), methodActions);

            actions.add(new NewFunction("", methodArguments, methodActions));
            break;
        case DEFINE_ATTRIBUTE:
            generate(generator, info, node.get(0), actions);
            actions.remove(actions.size() - 1);
            generate(generator, info, node.get(1), actions);
            break;
        case DEFINE_VARIABLE:
            addReference(generator, info, actions, node.getValue());
            actions.add(BasicAction.fromInt(ActionTypes.INIT_VARIABLE));
            break;
        default:
            break;
        }
    }

    private void generateUnary(final Generator generator, final Context info, final Node node, List<Action> actions) {
        Action lastAction = null;

        switch (node.getType()) {
        case PRE_INC:
            generate(generator, info, node.get(0), actions);
            actions.remove(actions.size() - 1);
            generate(generator, info, node.get(0), actions);
            lastAction = actions.get(actions.size() - 1);
            actions.add(BasicAction.fromInt(ActionTypes.INCREMENT));

            if (node.discardValue == false)
                actions.add(new RegisterCopy(0));

            if (lastAction == BasicAction.GET_ATTRIBUTE)
                actions.add(BasicAction.fromInt(ActionTypes.SET_ATTRIBUTE));
            else
                actions.add(BasicAction.fromInt(ActionTypes.SET_VARIABLE));

            if (node.discardValue == false)
                addLiteral(actions, new RegisterIndex(0));

            break;
        case PRE_DEC:
            generate(generator, info, node.get(0), actions);
            actions.remove(actions.size() - 1);
            generate(generator, info, node.get(0), actions);
            lastAction = actions.get(actions.size() - 1);
            actions.add(BasicAction.fromInt(ActionTypes.DECREMENT));

            if (node.discardValue == false)
                actions.add(new RegisterCopy(0));

            if (lastAction == BasicAction.GET_ATTRIBUTE)
                actions.add(BasicAction.fromInt(ActionTypes.SET_ATTRIBUTE));
            else
                actions.add(BasicAction.fromInt(ActionTypes.SET_VARIABLE));

            if (node.discardValue == false)
                addLiteral(actions, new RegisterIndex(0));

            break;
        case POST_INC:
            if (node.discardValue == false)
                generate(generator, info, node.get(0), actions);

            generate(generator, info, node.get(0), actions);
            actions.remove(actions.size() - 1);
            generate(generator, info, node.get(0), actions);
            lastAction = actions.get(actions.size() - 1);
            actions.add(BasicAction.fromInt(ActionTypes.INCREMENT));
            if (lastAction == BasicAction.GET_ATTRIBUTE)
                actions.add(BasicAction.fromInt(ActionTypes.SET_ATTRIBUTE));
            else
                actions.add(BasicAction.fromInt(ActionTypes.SET_VARIABLE));
            break;
        case POST_DEC:
            if (node.discardValue == false)
                generate(generator, info, node.get(0), actions);

            generate(generator, info, node.get(0), actions);
            actions.remove(actions.size() - 1);
            generate(generator, info, node.get(0), actions);
            lastAction = actions.get(actions.size() - 1);
            actions.add(BasicAction.fromInt(ActionTypes.DECREMENT));
            if (lastAction == BasicAction.GET_ATTRIBUTE)
                actions.add(BasicAction.fromInt(ActionTypes.SET_ATTRIBUTE));
            else
                actions.add(BasicAction.fromInt(ActionTypes.SET_VARIABLE));
            break;
        case PLUS:
            if (node.get(0).getType() == NodeType.BOOLEAN) {
                generate(generator, info, node.get(0), actions);
                addLiteral(actions, 0);
                actions.add(BasicAction.fromInt(ActionTypes.ADD));
            } else if (node.get(0).getType() == NodeType.INTEGER) {
            	if (Integer.valueOf(node.get(0).getValue()) == 0) {
                    addLiteral(actions, 0.0);
            	} else {
                    addLiteral(actions, node.get(0).getValue());
            	}
            } else if (node.get(0).getType() == NodeType.STRING) {
                generate(generator, info, node.get(0), actions);
                addLiteral(actions, 0);
                actions.add(BasicAction.fromInt(ActionTypes.ADD));
            } else if (node.get(0).getType() == NodeType.NULL) {
                generate(generator, info, node.get(0), actions);
                addLiteral(actions, 0);
                actions.add(BasicAction.fromInt(ActionTypes.ADD));
            } else {
                generate(generator, info, node.get(0), actions);
            }
            if (node.discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case MINUS:
            if (node.get(0).getType() == NodeType.BOOLEAN) {
                addLiteral(actions, 0);
                generate(generator, info, node.get(0), actions);
                actions.add(BasicAction.fromInt(ActionTypes.SUBTRACT));
            } else if (node.get(0).getType() == NodeType.INTEGER) {
                if (Integer.valueOf(node.get(0).getValue()) == 0) {
                    addLiteral(actions, -0.0);
                } else {
                    addLiteral(actions, -Integer.valueOf(node.get(0).getValue()));
                }
            } else if (node.get(0).getType() == NodeType.STRING) {
                addLiteral(actions, 0);
                generate(generator, info, node.get(0), actions);
                actions.add(BasicAction.fromInt(ActionTypes.SUBTRACT));
            } else if (node.get(0).getType() == NodeType.NULL) {
                addLiteral(actions, 0);
                generate(generator, info, node.get(0), actions);
                actions.add(BasicAction.fromInt(ActionTypes.SUBTRACT));
            } else {
                addLiteral(actions, 0);
                generate(generator, info, node.get(0), actions);
                actions.add(BasicAction.fromInt(ActionTypes.SUBTRACT));
            }
            if (node.discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case BIT_NOT:
            generate(generator, info, node.get(0), actions);
            addLiteral(actions, new Double(Double
                    .longBitsToDouble(0x41EFFFFFFFE00000L)));
            actions.add(BasicAction.fromInt(ActionTypes.BITWISE_XOR));

            if (node.discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case NOT:
            generate(generator, info, node.get(0), actions);
            actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));

            if (node.discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case DELETE:
            generate(generator, info, node.get(0), actions);
            actions.remove(actions.size() - 1);

            if (node.get(0).getType() == NodeType.VALUE)
                actions.add(BasicAction.fromInt(ActionTypes.DELETE_VARIABLE));
            else
                actions.add(BasicAction.fromInt(ActionTypes.DELETE));

            if (node.discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
            break;
        case THROW:
            generate(generator, info, node.get(0), actions);
            actions.add(BasicAction.fromInt(ActionTypes.THROW));
            break;
        default:
            break;
        }
    }

    private void generateBinary(final Generator generator, final Context info, final Node node, List<Action> actions) {
        List<Action> array = new ArrayList<Action>();

        int count = node.count();
        int offset = 0;

        /*
         * For most node types we want to generate the actions for the child
         * nodes (if any) before adding the actions for node type.
         */

        switch (node.getType()) {
        // > and <= are synthesised using < and !, see below.
        case LESS_THAN_EQUAL:
        case STRING_LESS_THAN_EQUAL:
        case STRING_GREATER_THAN:
            for (int i = count - 1; i >= 0; i--)
                generate(generator, info, node.get(i), actions);
            break;
        // Code Logical And/Or generated using if actions, see below.
        case LOGICAL_AND:
        case LOGICAL_OR:
        case STRICT_EQUAL:
        case STRICT_NOT_EQUAL:
        case GREATER_THAN:
            break;
        default:
            for (int i = 0; i < count; i++)
                generate(generator, info, node.get(i), actions);
            break;
        }

        switch (node.getType()) {
        case STRING_ADD:
            actions.add(BasicAction.fromInt(ActionTypes.STRING_ADD));
            break;
        case STRING_LESS_THAN_EQUAL:
        case STRING_GREATER_THAN_EQUAL:
            actions.add(BasicAction.fromInt(ActionTypes.STRING_LESS));
            actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));
            break;
        case STRING_GREATER_THAN:
            actions.add(BasicAction.fromInt(ActionTypes.STRING_LESS));
            break;
        case ADD:
            actions.add(BasicAction.fromInt(ActionTypes.ADD));
            break;
        case SUB:
            actions.add(BasicAction.fromInt(ActionTypes.SUBTRACT));
            break;
        case MUL:
            actions.add(BasicAction.fromInt(ActionTypes.MULTIPLY));
            break;
        case DIV:
            actions.add(BasicAction.fromInt(ActionTypes.DIVIDE));
            break;
        case MOD:
            actions.add(BasicAction.fromInt(ActionTypes.MODULO));
            break;
        case BIT_AND:
            actions.add(BasicAction.fromInt(ActionTypes.BITWISE_AND));
            break;
        case BIT_OR:
            actions.add(BasicAction.fromInt(ActionTypes.BITWISE_OR));
            break;
        case BIT_XOR:
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
        case EQUAL:
            actions.add(BasicAction.fromInt(ActionTypes.EQUALS));
            break;
        case NOT_EQUAL:
            actions.add(BasicAction.fromInt(ActionTypes.EQUALS));
            actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));
            break;
        case LESS_THAN:
            actions.add(BasicAction.fromInt(ActionTypes.LESS));
            break;
        case GREATER_THAN:
            switch (info.version) {
            case 5:
                generate(generator, info, node.get(1), actions);
                generate(generator, info, node.get(0), actions);
                actions.add(BasicAction.fromInt(ActionTypes.LESS));
//
//                if (node.getParent().type != If)
//                    actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));
                break;
            case 6:
            case 7:
                generate(generator, info, node.get(0), actions);
                generate(generator, info, node.get(1), actions);
                actions.add(BasicAction.fromInt(ActionTypes.GREATER));
                break;
            }
            break;
        case LESS_THAN_EQUAL:
            actions.add(BasicAction.fromInt(ActionTypes.LESS));
            actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));
            break;
        case GREATER_THAN_EQUAL:
            actions.add(BasicAction.fromInt(ActionTypes.LESS));
            if (node.getParent().getType() != NodeType.IF)
                actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));
            break;
        case AND:
            actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_AND));
            break;
        case OR:
            actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_OR));
            break;
        case LOGICAL_AND:
            array.add(BasicAction.fromInt(ActionTypes.POP));

            generate(generator, info, node.get(1), array);
            offset = actionLength(array, info);

            generate(generator, info, node.get(0), actions);

            actions.add(BasicAction.fromInt(ActionTypes.DUPLICATE));
            actions.add(BasicAction.fromInt(ActionTypes.LOGICAL_NOT));

            actions.add(new If(offset));
            actions.addAll(array);
            break;
        case LOGICAL_OR:
            array.add(BasicAction.fromInt(ActionTypes.POP));

            generate(generator, info, node.get(1), array);
            offset = actionLength(array, info);

            generate(generator, info, node.get(0), actions);
            actions.add(BasicAction.fromInt(ActionTypes.DUPLICATE));

            actions.add(new If(offset));
            actions.addAll(array);
            break;
        case INSTANCEOF:
            actions.add(BasicAction.fromInt(ActionTypes.INSTANCEOF));
            break;
        case STRICT_EQUAL:
            switch (info.version) {
            case 5:
                generate(generator, info, node.get(0), actions);
                actions.add(new RegisterCopy(1));
                actions.add(BasicAction.fromInt(ActionTypes.GET_TYPE));
                generate(generator, info, node.get(1), actions);
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
        case STRICT_NOT_EQUAL:
            switch (info.version) {
            case 5:
                generate(generator, info, node.get(0), actions);
                actions.add(new RegisterCopy(1));
                actions.add(BasicAction.fromInt(ActionTypes.GET_TYPE));
                generate(generator, info, node.get(1), actions);
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
        if (node.discardValue)
            actions.add(BasicAction.fromInt(ActionTypes.POP));
    }

    private void generateSelect(final Generator generator, final Context info, final Node node, List<Action> actions) {
        List<Action> trueActions = new ArrayList<Action>();
        int offsetToNext = 0;

        List<Action> falseActions = new ArrayList<Action>();
        int offsetToEnd = 0;

        generate(generator, info, node.get(2), falseActions);

        offsetToNext = actionLength(falseActions, info);
        offsetToNext += 5; // Length of jump tag

        generate(generator, info, node.get(1), trueActions);

        offsetToEnd = actionLength(trueActions, info);

        generate(generator, info, node.get(0), actions);

        actions.add(new If(offsetToNext));
        actions.addAll(falseActions);

        actions.add(new Jump(offsetToEnd));

        actions.addAll(trueActions);

        if (node.discardValue)
            actions.add(BasicAction.fromInt(ActionTypes.POP));
    }

    private void generateAssignment(final Generator generator, final Context info, final Node node, List<Action> actions) {
        generate(generator, info, node.get(0), actions);

        Action lastAction = actions.get(actions.size() - 1);

        if (lastAction == BasicAction.GET_VARIABLE)
            actions.remove(actions.size() - 1);
        else if (lastAction == BasicAction.GET_ATTRIBUTE)
            actions.remove(actions.size() - 1);
        else if (lastAction == BasicAction.GET_PROPERTY)
            actions.remove(actions.size() - 1);
        else if (lastAction == BasicAction.INIT_VARIABLE)
            actions.remove(actions.size() - 1);

        if (node.getType() != NodeType.ASSIGN)
            generate(generator, info, node.get(0), actions);

        generate(generator, info, node.get(1), actions);

        switch (node.getType()) {
        case ASSIGN_ADD:
            actions.add(BasicAction.fromInt(ActionTypes.ADD));
            break;
        case ASSIGN_SUB:
            actions.add(BasicAction.fromInt(ActionTypes.SUBTRACT));
            break;
        case ASSIGN_MUL:
            actions.add(BasicAction.fromInt(ActionTypes.MULTIPLY));
            break;
        case ASSIGN_DIV:
            actions.add(BasicAction.fromInt(ActionTypes.DIVIDE));
            break;
        case ASSIGN_MOD:
            actions.add(BasicAction.fromInt(ActionTypes.MODULO));
            break;
        case ASSIGN_BIT_AND:
            actions.add(BasicAction.fromInt(ActionTypes.BITWISE_AND));
            break;
        case ASSIGN_BIT_OR:
            actions.add(BasicAction.fromInt(ActionTypes.BITWISE_OR));
            break;
        case ASSIGN_BIT_XOR:
            actions.add(BasicAction.fromInt(ActionTypes.BITWISE_XOR));
            break;
        case ASSIGN_LSL:
            actions.add(BasicAction.fromInt(ActionTypes.SHIFT_LEFT));
            break;
        case ASSIGN_LSR:
            actions.add(BasicAction.fromInt(ActionTypes.SHIFT_RIGHT));
            break;
        case ASSIGN_ASR:
            actions.add(BasicAction.fromInt(ActionTypes.ARITH_SHIFT_RIGHT));
            break;
        default:
            break;
        }

        if (node.getType() == NodeType.ASSIGN && node.getParent() != null
                && (node.getParent().getType() == NodeType.LIST || node.getParent().getType() == NodeType.ASSIGN)) {
            if (node.get(0).getType() != NodeType.DEFINE_VARIABLE) {
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

        if (node.getType() == NodeType.ASSIGN && node.getParent() != null
                && (node.getParent().getType() == NodeType.LIST || node.getParent().getType() == NodeType.ASSIGN)) {
            if (node.get(0).getType() != NodeType.DEFINE_VARIABLE) {
                addLiteral(actions, new RegisterIndex(0));

                if (node.getParent().getType() == NodeType.LIST)
                    actions.add(BasicAction.fromInt(ActionTypes.POP));
            }
        }

    }

    /*
     * generateFunction is used to add either a predefined action if the
     * function call is to one of Flash's built-in functions. A separate method
     * is used to make the code in the generate method more readable.
     */
    private void generateFunction(final Generator generator, final Context info, final Node node, List<Action> actions) {
        String name = node.getValue();
        int count = node.count();

        if (functions.containsKey(name.toLowerCase())) {
            if (node.getValue().toLowerCase().equals("call")) {
                generate(generator, info, node.get(0), actions);

                Action lastAction = actions.get(actions.size() - 1);

                if (lastAction == BasicAction.GET_VARIABLE)
                    actions.remove(actions.size() - 1);

                actions.add(Call.getInstance());
            } else if (node.getValue().toLowerCase().equals("chr")) {
                generate(generator, info, node.get(0), actions);
                actions.add(BasicAction.fromInt(ActionTypes.ASCII_TO_CHAR));
            } else if (node.getValue().toLowerCase().equals("delete")) {
                generate(generator, info, node.get(0), actions);

                Action lastAction = actions.get(actions.size() - 1);

                if (lastAction == BasicAction.GET_VARIABLE)
                    actions.remove(actions.size() - 1);

                actions.add(BasicAction.fromInt(ActionTypes.DELETE));
            } else if (node.getValue().toLowerCase().equals("duplicatemovieclip")) {
                generate(generator, info, node.get(0), actions);
                generate(generator, info, node.get(1), actions);

                if (node.get(2).getType() == NodeType.INTEGER
                        && node.get(2).getType() == NodeType.INTEGER) {
                    int level = 16384;

                    level += Integer.valueOf(node.get(2).getValue());

                    addLiteral(actions, level);
                } else {
                    addLiteral(actions, 16384);

                    generate(generator, info, node.get(2), actions);

                    actions.add(BasicAction.fromInt(ActionTypes.ADD));
                }
                actions.add(BasicAction.fromInt(ActionTypes.CLONE_SPRITE));
            } else if (node.getValue().toLowerCase().equals("eval")) {
                generate(generator, info, node.get(0), actions);
                actions.add(BasicAction.fromInt(ActionTypes.GET_VARIABLE));
            } else if (node.getValue().toLowerCase().equals("fscommand")) {
                boolean isCommandString = node.get(0).getType() == NodeType.STRING
                        && node.get(0).getValue() != null;
                boolean isArgumentString = false;

                if (count == 1) {
                    isArgumentString = true;
                }
                if (count > 1) {
                    isArgumentString = node.get(1).getType() == NodeType.STRING
                            && node.get(1).getValue() != null;
                }

                if (isCommandString && isArgumentString) {
                    String url = node.get(0).getValue();
                    String target;
                    if (count == 1) {
                        target = "";
                    } else {
                        target = node.get(1).getValue();
                    }
                    actions.add(new GetUrl(url, target));
                } else {
                    if (isCommandString) {
                        addReference(generator, info, actions, node.get(0).getValue());
                    } else {
                        addReference(generator, info, actions, "FSCommand:");
                        generate(generator, info, node.get(0), actions);
                        actions
                                .add(BasicAction
                                        .fromInt(ActionTypes.STRING_ADD));
                    }

                    if (count > 1) {
                        generate(generator, info, node.get(1), actions);
                    }

                    actions.add(new GetUrl2(GetUrl2.Request.MOVIE_TO_LEVEL));
                }
            } else if (node.getValue().toLowerCase().equals("getproperty")) {
                String propertyName = node.get(1).getValue();
                int pVal = (propertyNames.get(propertyName)).intValue();

                generate(generator, info, node.get(0), actions);
                if (pVal >= 1 && pVal <= 21)
                    addLiteral(actions, new Integer(pVal));
                else if (pVal == 0)
                    addLiteral(actions, new Double(pVal));
                else
                    addLiteral(actions, new Property(pVal));
                actions.add(BasicAction.fromInt(ActionTypes.GET_PROPERTY));
            } else if (node.getValue().toLowerCase().equals("gettimer")) {
                for (int i = count - 1; i >= 0; i--)
                    generate(generator, info, node.get(i), actions);

                actions.add(BasicAction.fromInt(ActionTypes.GET_TIME));
            } else if (node.getValue().toLowerCase().equals("geturl")) {
                switch (count) {
                case 1:
                    if (node.get(0).getType() == NodeType.STRING
                            && node.get(0).getValue() != null) {
                        actions.add(new GetUrl(node.get(0).getValue(), ""));
                    } else {
                        generate(generator, info, node.get(0), actions);
                        addReference(generator, info, actions, "");
                        actions
                                .add(new GetUrl2(GetUrl2.Request.MOVIE_TO_LEVEL));
                    }
                    break;
                case 2:
                    if (node.get(0).getType() == NodeType.STRING
                            && node.get(0).getValue() != null
                            && node.get(1).getType() == NodeType.STRING
                            && node.get(1).getValue() != null) {
                        actions.add(new GetUrl(node.get(0).getValue(),
                                node.get(1).getValue()));
                    } else {
                        generate(generator, info, node.get(0), actions);
                        generate(generator, info, node.get(1), actions);
                        actions
                                .add(new GetUrl2(GetUrl2.Request.MOVIE_TO_LEVEL));
                    }
                    break;
                case 3:
                    generate(generator, info, node.get(0), actions);
                    generate(generator, info, node.get(1), actions);

                    if (node.get(2).getValue().toLowerCase().equals("get"))
                        actions.add(new GetUrl2(
                                GetUrl2.Request.MOVIE_TO_LEVEL_WITH_GET));
                    else if (node.get(2).getValue().toLowerCase().equals("post"))
                        actions.add(new GetUrl2(
                                GetUrl2.Request.MOVIE_TO_LEVEL_WITH_POST));
                    else
                        actions
                                .add(new GetUrl2(GetUrl2.Request.MOVIE_TO_LEVEL));
                    break;
                default:
                    break;
                }
            } else if (node.getValue().toLowerCase().equals("getversion")) {
                addLiteral(actions, "/:$version");
                actions.add(BasicAction.fromInt(ActionTypes.GET_VARIABLE));
            } else if (node.getValue().toLowerCase().equals("gotoandplay")) {
                int index = count - 1;

                if (info.context.firstElement().toString().equals("MovieClip")) {
                    if (node.get(index).getType() == NodeType.INTEGER) {
                        int frameNumber = Integer.valueOf(node.get(index).getValue()) - 1;

                        actions.add(new GotoFrame(frameNumber));
                    } else {
                        actions.add(new GotoLabel(node.get(index).getValue()));
                    }
                    actions.add(BasicAction.fromInt(ActionTypes.PLAY));
                } else {
                    if (node.get(index).getType() == NodeType.INTEGER) {
                        int frameNumber = Integer.valueOf(node.get(index).getValue()) - 1;

                        actions.add(new GotoFrame(frameNumber));
                        actions.add(BasicAction.fromInt(ActionTypes.PLAY));
                    } else if (node.get(index).getValue().toLowerCase().startsWith(
                            "frame ")) {
                        String frame = node.get(index).getValue().substring(6);
                        int frameNumber = 0;

                        try {
                            frameNumber = Integer.valueOf(frame).intValue() - 1;
                        } catch (NumberFormatException e) {

                        }

                        if (frameNumber == 1) {
                            generate(generator, info, node.get(index), actions);
                            actions.add(new GotoFrame2(0, true));
                        } else {
                            actions.add(new GotoLabel(node.get(index).getValue()));
                            actions.add(BasicAction.fromInt(ActionTypes.PLAY));
                        }
                    } else {
                        generate(generator, info, node.get(index), actions);
                        actions.add(new GotoFrame2(0, true));
                    }
                }
            } else if (node.getValue().toLowerCase().equals("gotoandstop")) {
                int index = count - 1;

                if (info.context.firstElement().toString().equals("MovieClip")) {
                    if (node.get(index).getType() == NodeType.INTEGER) {
                        int frameNumber = Integer.valueOf(node.get(index).getValue()) - 1;

                        actions.add(new GotoFrame(frameNumber));
                    } else {
                        actions.add(new GotoLabel(node.get(index).getValue()));
                    }
                } else {
                    if (node.get(index).getType() == NodeType.INTEGER) {
                        int frameNumber = Integer.valueOf(node.get(index).getValue()) - 1;

                        actions.add(new GotoFrame(frameNumber));
                    } else if (node.get(index).getValue().toLowerCase().startsWith(
                            "frame ")) {
                        String frame = node.get(index).getValue().substring(6);
                        int frameNumber = 0;

                        try {
                            frameNumber = Integer.valueOf(frame).intValue() - 1;
                        } catch (NumberFormatException e) {

                        }

                        if (frameNumber == 1) {
                            generate(generator, info, node.get(index), actions);
                            actions.add(new GotoFrame2(0, false));
                        } else {
                            actions.add(new GotoLabel(node.get(index).getValue()));
                        }
                    } else {
                        generate(generator, info, node.get(index), actions);

                        actions.add(new GotoFrame2(0, false));
                    }
                }
            } else if (node.getValue().toLowerCase().equals("int")) {
                for (int i = count - 1; i >= 0; i--)
                    generate(generator, info, node.get(i), actions);

                actions.add(BasicAction.fromInt(ActionTypes.TO_INTEGER));
            } else if (node.getValue().toLowerCase().equals("length")) {
                for (int i = count - 1; i >= 0; i--)
                    generate(generator, info, node.get(i), actions);

                actions.add(BasicAction.fromInt(ActionTypes.STRING_LENGTH));
            } else if (node.getValue().toLowerCase().equals("loadmovie")) {
                switch (count) {
                case 2:
                    if (node.get(0).getValue() != null
                            && node.get(1).getType() == NodeType.INTEGER) {
                        String url = node.get(0).getValue();
                        String target = "_level" + Integer.valueOf(node.get(1).getValue());

                        actions.add(new GetUrl(url, target));
                    } else {
                        generate(generator, info, node.get(0), actions);
                        generate(generator, info, node.get(1), actions);

                        actions
                                .add(new GetUrl2(
                                        GetUrl2.Request.MOVIE_TO_TARGET));
                    }
                    break;
                case 3:
                    generate(generator, info, node.get(0), actions);
                    generate(generator, info, node.get(1), actions);

                    if (node.get(2).getValue().toLowerCase().equals("get"))
                        actions.add(new GetUrl2(
                                GetUrl2.Request.MOVIE_TO_TARGET_WITH_GET));
                    else
                        actions.add(new GetUrl2(
                                GetUrl2.Request.MOVIE_TO_TARGET_WITH_POST));
                    break;
                default:
                    break;
                }
            } else if (node.getValue().toLowerCase().equals("loadvariables")) {
                switch (count) {
                case 2:
                    generate(generator, info, node.get(0), actions);
                    generate(generator, info, node.get(1), actions);

                    actions
                            .add(new GetUrl2(
                                    GetUrl2.Request.VARIABLES_TO_TARGET));
                    break;
                case 3:
                    generate(generator, info, node.get(0), actions);
                    generate(generator, info, node.get(1), actions);

                    if (node.get(2).getValue().toLowerCase().equals("get"))
                        actions.add(new GetUrl2(
                                GetUrl2.Request.VARIABLES_TO_TARGET_WITH_GET));
                    else
                        actions.add(new GetUrl2(
                                GetUrl2.Request.VARIABLES_TO_TARGET_WITH_POST));
                    break;
                default:
                    break;
                }
            } else if (node.getValue().toLowerCase().equals("mbchr")) {
                for (int i = count - 1; i >= 0; i--)
                    generate(generator, info, node.get(i), actions);

                actions.add(BasicAction.fromInt(ActionTypes.MB_ASCII_TO_CHAR));
            } else if (node.getValue().toLowerCase().equals("mbord")) {
                for (int i = count - 1; i >= 0; i--)
                    generate(generator, info, node.get(i), actions);

                actions.add(BasicAction.fromInt(ActionTypes.MB_CHAR_TO_ASCII));
            } else if (node.getValue().toLowerCase().equals("mbsubstring")) {
                for (int i = 0; i < count; i++)
                    generate(generator, info, node.get(i), actions);

                actions.add(BasicAction.fromInt(ActionTypes.MB_STRING_EXTRACT));
            } else if (node.getValue().toLowerCase().equals("nextframe")) {
                actions.add(BasicAction.fromInt(ActionTypes.NEXT_FRAME));
            } else if (node.getValue().toLowerCase().equals("nextscene")) {
                actions.add(new GotoFrame(0));
            } else if (node.getValue().toLowerCase().equals("number")) {
                generate(generator, info, node.get(0), actions);

                actions.add(BasicAction.fromInt(ActionTypes.TO_NUMBER));
            } else if (node.getValue().toLowerCase().equals("ord")) {
                for (int i = count - 1; i >= 0; i--)
                    generate(generator, info, node.get(i), actions);

                actions.add(BasicAction.fromInt(ActionTypes.CHAR_TO_ASCII));
            } else if (node.getValue().toLowerCase().equals("parseint")) {
                for (int i = count - 1; i >= 0; i--)
                    generate(generator, info, node.get(i), actions);

                addLiteral(actions, count);
                addReference(generator, info, actions, name);
                actions.add(BasicAction.fromInt(ActionTypes.EXECUTE_FUNCTION));
            } else if (node.getValue().toLowerCase().equals("play")) {
                actions.add(BasicAction.fromInt(ActionTypes.PLAY));
            } else if (node.getValue().toLowerCase().equals("prevframe")) {
                actions.add(BasicAction.fromInt(ActionTypes.PREV_FRAME));
            } else if (node.getValue().toLowerCase().equals("prevscene")) {
                actions.add(new GotoFrame(0));
            } else if (node.getValue().toLowerCase().equals("print")) {
                generate(generator, info, node.get(0), actions);
                addReference(generator, info, actions, node.get(1).getValue());
                actions.add(BasicAction.fromInt(ActionTypes.GET_VARIABLE));
                actions.add(new GetUrl2(GetUrl2.Request.MOVIE_TO_LEVEL));
            } else if (node.getValue().toLowerCase().equals("printnum")) {
                addReference(generator, info, actions, node.get(1).getValue());

                if (node.get(0).getType() == NodeType.IDENTIFIER) {
                    addReference(generator, info, actions, "_level");
                    generate(generator, info, node.get(0), actions);
                    actions.add(BasicAction.fromInt(ActionTypes.STRING_ADD));
                } else {
                    generate(generator, info, node.get(0), actions);
                }
                actions.add(new GetUrl2(GetUrl2.Request.MOVIE_TO_LEVEL));
            } else if (node.getValue().toLowerCase().equals("printasbitmap")) {
                generate(generator, info, node.get(0), actions);
                addReference(generator, info, actions, node.get(1).getValue());
                actions.add(BasicAction.fromInt(ActionTypes.GET_VARIABLE));
                actions.add(new GetUrl2(GetUrl2.Request.MOVIE_TO_LEVEL));
            } else if (node.getValue().toLowerCase().equals("printasbitmapnum")) {
                addReference(generator, info, actions, node.get(1).getValue());

                if (node.get(0).getType() == NodeType.IDENTIFIER) {
                    addReference(generator, info, actions, "_level");
                    generate(generator, info, node.get(0), actions);
                    actions.add(BasicAction.fromInt(ActionTypes.STRING_ADD));
                } else {
                    generate(generator, info, node.get(0), actions);
                }
                actions.add(new GetUrl2(GetUrl2.Request.MOVIE_TO_LEVEL));
            } else if (node.getValue().toLowerCase().equals("random")) {
                generate(generator, info, node.get(0), actions);
                actions.add(BasicAction.fromInt(ActionTypes.RANDOM_NUMBER));
            } else if (node.getValue().toLowerCase().equals("removemovieclip")) {
                for (int i = 0; i < count; i++)
                    generate(generator, info, node.get(i), actions);

                actions.add(BasicAction.fromInt(ActionTypes.REMOVE_SPRITE));
            } else if (node.getValue().toLowerCase().equals("set")) {
                for (int i = 0; i < count; i++)
                    generate(generator, info, node.get(i), actions);

                actions.add(BasicAction.fromInt(ActionTypes.SET_VARIABLE));
            } else if (node.getValue().toLowerCase().equals("setproperty")) {
                for (int i = 0; i < count; i++)
                    generate(generator, info, node.get(i), actions);

                actions.add(BasicAction.fromInt(ActionTypes.SET_PROPERTY));
            } else if (node.getValue().toLowerCase().equals("startdrag")) {
                if (count > 2) {
                    generate(generator, info, node.get(2), actions);
                    generate(generator, info, node.get(3), actions);
                    generate(generator, info, node.get(4), actions);
                    generate(generator, info, node.get(5), actions);
                    addLiteral(actions, 1);

                    if (node.get(1).getType() == NodeType.BOOLEAN) {
                        addLiteral(actions, Boolean.valueOf(node.get(1).getValue()) ? 1 : 0);
                    } else {
                        generate(generator, info, node.get(1), actions);
                    }
                } else if (count == 2) {
                    addLiteral(actions, 0);

                    if (node.get(1).getType() == NodeType.BOOLEAN) {
                        addLiteral(actions, Boolean.valueOf(node.get(1).getValue()) ? 1 : 0);
                    } else {
                        generate(generator, info, node.get(1), actions);
                    }
                } else {
                    addLiteral(actions, 0);
                    addLiteral(actions, 0);
                }
                generate(generator, info, node.get(0), actions);

                actions.add(BasicAction.fromInt(ActionTypes.START_DRAG));
            } else if (node.getValue().toLowerCase().equals("stop")) {
                actions.add(BasicAction.fromInt(ActionTypes.STOP));
            } else if (node.getValue().toLowerCase().equals("stopallsounds")) {
                actions.add(BasicAction.fromInt(ActionTypes.STOP_SOUNDS));
            } else if (node.getValue().toLowerCase().equals("stopdrag")) {
                actions.add(BasicAction.fromInt(ActionTypes.END_DRAG));
            } else if (node.getValue().toLowerCase().equals("string")) {
                generate(generator, info, node.get(0), actions);

                actions.add(BasicAction.fromInt(ActionTypes.TO_STRING));
            } else if (node.getValue().toLowerCase().equals("substring")) {
                for (int i = 0; i < count; i++)
                    generate(generator, info, node.get(i), actions);

                actions.add(BasicAction.fromInt(ActionTypes.STRING_EXTRACT));
            } else if (node.getValue().toLowerCase().equals("targetpath")) {
                for (int i = 0; i < count; i++)
                    generate(generator, info, node.get(i), actions);

                actions.add(BasicAction.fromInt(ActionTypes.GET_TARGET));
            } else if (node.getValue().toLowerCase().equals("togglehighquality")) {
                actions.add(BasicAction.fromInt(ActionTypes.TOGGLE_QUALITY));
            } else if (node.getValue().toLowerCase().equals("trace")) {
                for (int i = 0; i < count; i++)
                    generate(generator, info, node.get(i), actions);

                actions.add(BasicAction.fromInt(ActionTypes.TRACE));
            } else if (node.getValue().toLowerCase().equals("typeof")) {
                for (int i = 0; i < count; i++)
                    generate(generator, info, node.get(i), actions);

                actions.add(BasicAction.fromInt(ActionTypes.GET_TYPE));
            } else if (node.getValue().toLowerCase().equals("unloadmovie")) {
                if (node.get(0).getType() == NodeType.INTEGER) {
                    actions.add(new GetUrl("", "_level" + node.get(0).getValue()));
                } else {
                    addLiteral(actions, "");
                    generate(generator, info, node.get(0), actions);
                    actions.add(new GetUrl2(GetUrl2.Request.MOVIE_TO_TARGET));
                }
            } else if (node.getValue().toLowerCase().equals("unloadmovienum")) {
                if (node.get(0).getType() == NodeType.INTEGER) {
                    actions.add(new GetUrl("", "_level" + node.get(0).getValue()));
                } else {
                    addLiteral(actions, "");
                    generate(generator, info, node.get(0), actions);
                    actions.add(new GetUrl2(GetUrl2.Request.MOVIE_TO_TARGET));
                }
            } else if (node.getValue().toLowerCase().equals("void")) {
                for (int i = 0; i < count; i++)
                    generate(generator, info, node.get(i), actions);

                actions.add(BasicAction.fromInt(ActionTypes.POP));
                addLiteral(actions, Void.getInstance());
            } else {
                for (int i = 0; i < count; i++)
                    generate(generator, info, node.get(i), actions);

                addReference(generator, info, actions, name);
                actions.add(BasicAction.fromInt(ActionTypes.EXECUTE_FUNCTION));
            }

            if ((functions.get(name.toLowerCase())).booleanValue()) {
                if (node.discardValue)
                    actions.add(BasicAction.fromInt(ActionTypes.POP));
            }
        } else {
            if (node.getValue().toLowerCase().equals("parseint")) {
                for (int i = count - 1; i >= 0; i--)
                    generate(generator, info, node.get(i), actions);

                addLiteral(actions, count);
                addReference(generator, info, actions, name);
                actions.add(BasicAction.fromInt(ActionTypes.EXECUTE_FUNCTION));
            } else if (node.getValue().toLowerCase().equals("updateafterevent")) {
                for (int i = count - 1; i >= 0; i--)
                    generate(generator, info, node.get(i), actions);

                addLiteral(actions, count);
                addReference(generator, info, actions, name);
                actions.add(BasicAction.fromInt(ActionTypes.EXECUTE_FUNCTION));
            } else {
                for (int i = count - 1; i >= 0; i--)
                    generate(generator, info, node.get(i), actions);

                addLiteral(actions, count);
                addReference(generator, info, actions, name);
                actions.add(BasicAction.fromInt(ActionTypes.EXECUTE_FUNCTION));

                if (valueFunctions.containsKey(name.toLowerCase()) == false) {
                    if (node.discardValue)
                        actions.add(BasicAction.fromInt(ActionTypes.POP));
                }
            }
        }

        if (valueFunctions.containsKey(name.toLowerCase())) {
            if (node.discardValue)
                actions.add(BasicAction.fromInt(ActionTypes.POP));
        }
    }

    private void addReference(final Generator generator, final Context info, List<Action> actions, Object literal) {
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

    private int actionLength(List<Action> array, Context info) {
        int length = 0;

        com.flagstone.transform.coder.Context ctxt = new com.flagstone.transform.coder.Context();

        for (Iterator<Action> i = array.iterator(); i.hasNext();) {
            Action action = i.next();

            length += action.prepareToEncode(ctxt);
        }
        return length;
    }

    private void reportError(String errorKey, int number) throws ParseException {
        ParseException parseError = new ParseException(errorKey);

        parseError.currentToken = new Token();
        parseError.currentToken.beginLine = number;

        throw parseError;
    }
}
