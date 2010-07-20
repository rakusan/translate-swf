package com.flagstone.translate;

import java.util.ArrayList;
import java.util.Stack;

public final class Context {
    /*
     * The stack used to track nodes where function definitions are
     * inserted.
     */
    public Stack<Node> nodes = new Stack<Node>();

    /*
     * Array of strings found in a script. The useStrings flag is set when
     * whenever a string is referenced more than once.
     */
    public ArrayList<String> strings = new ArrayList<String>(256);
    public boolean useStrings = false;

    /*
     * The context stack used to support conditional generation of actions.
     */
    public Stack<String> context = new Stack<String>();

    public String encoding;
    public int version;

    public Context(final String encoding, final int version) {
    	super();
    	this.encoding = encoding;
    	this.version = version;
    }

    /*
     * Adds a string to the table if it has not been added previously. Only
     * the first 256 strings are stored in the table. This should rarely be
     * a limitation however.
     * @param str a string representing a string literal, identifier, the
     * name of a property or function.
     */
    public void addString(String str) {
        if (strings.contains(str))
            useStrings = true;
        else if (strings.size() < 256) {
            strings.add(str);
        }
    }

    /*
     * Clears the strings table so definitions for event handler are only
     * defined within the scope of the event handler and not across the
     * entire script.
     */
    void clearStrings() {
        strings.clear();
        useStrings = false;
    }
}

