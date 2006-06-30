/*
 * ASNode.java
 * Translate AS1
 *
 * A compiler for ActionScript 1.x
 * Copyright (c) 2003-2006 Flagstone Software Ltd. All rights reserved.
 *
 @license@
 */
package com.flagstone.translate;

import java.util.*;

/**
 * ASNode is the class used by the parser to construct a tree representation of
 * an ActionScript file based on the parser grammar.
 * 
 * Node trees can also be constructed 'manually' and then encoded to give the 
 * binary representation of the byte-codes and actions that will be executed 
 * by the Flash Player.
 * 
 * For example, the node tree for the ActionScript statement:
 * 
 * <pre>
 *     c = a + b;
 * </pre>
 * 
 * Can be represented using the following code to build the tree.
 * 
 * <pre>
 *     ASNode a = new ASNode(ASNode.Identifier, "a");
 *     ASNode b = new ASNode(ASNode.Identifier, "b");
 *     ASNode c = new ASNode(ASNode.Identifier, "c");
 * 
 *     ASNode add = new ASNode(ASNode.Add, a, b);
 *     ASNode assign = new ASNode(a, add);
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
public final class ASNode extends Object 
{
    /*
     * The ASInfo class is used to pass state information between nodes when 
     * a tree is translated into an equivalent set of action objects and when
     * it is encoded. The context is used for four purposes when encoding a set
     * of nodes:
     * 
     * 1. Maintaining the version of Flash that the script implements ensuring 
     * compatibility with the programming model supported by the Flash Player. 
     * Only ActionScript for Flash 5 and onwards is supported. Tracking the
     * Flash version is important so that changes in the way objects are encoded
     * can be taken into account.
     * 
     * 2. Creating a common table of string. Rather than pushing a string onto 
     * the stack each time a reference to an identifier or string literal is used 
     * in a script, Flash supports a table which contains all the the strings in 
     * a script (up to 256). To specify a string the index of the string in the 
     * table is pushed onto the stack reducing the size of the script.
     * 
     * Flash only generates a string table if any of the identifiers or string 
     * literals are used more than once in a script. The boolean flag, useStrings 
     * is set to true whenever a literal is referenced more than once indicating
     * that the table should be added to the translated script. 
     * 
     * 3. Maintaining insertion points for function definitions. When the Flash 
     * authoring application compiles ActionScript function definitions are moved 
     * to the start of the script. Similarly definitions nested inside a block are 
     * moved to the start of the block. This behaviour is maintained when a tree
     * of nodes are translated, to make regression testing against code generated 
     * by the Flash authoring application easier.
     * 
     * To do this a stack is maintained which contains the nodes representing a 
     * complete script or block. The ASNode method, reorder, inserts the tree 
     * of nodes that make up the function definition into the array of child nodes
     * maintained by the node on the top of this stack. To preserve the order
     * in which functions are defined each ASNode maintains a private member
     * which is the index into the array of children where the next definition will
     * be inserted. When a start of a new block or function definition is encountered
     * the new node is pushed onto the stack - currently function definitions 
     * are only nested inside other function definitions all other definitions 
     * are added to the first node in the script.
     * 
     * 4. A stack of the different type of nodes being traversed is maintained 
     * to allow conditional code generation. For example if a property identifier
     * (_x, _y, _width, etc.) is encountered then normally the value is not added
     * to the string table, unless the property is inside a 'with' statement.
     * 
     * As nodes representing the different types of ActionScript statement are
     * traversed the name of the node is pushed onto the stack so the correct 
     * context can be determined when translating and encoding nodes lower in the 
     * tree.
     * 
     */
    private class ASInfo
    {
        /*
         * The current version of Flash that the tree of ASNodes will
         * be translated and compiled into. 
         */
        int version = 0;
        
        /*
         * The character encoding used to represent identifiers and string 
         * literals.
         */
        String encoding = null;
        
        /*
         * The stack used to track nodes where function definitions are inserted.
         */
        Stack nodes = new Stack();

        /*
         * Array of strings found in a script. The useStrings flag is set when 
         * whenever a string is referenced more than once.
         */ 
        ArrayList strings = new ArrayList(256);        
        boolean useStrings = false;
        
        /*
         * The context stack used to support conditional generation of actions.
         */
        Stack context = new Stack();
        
        /*
         * Constructs a new ASInfo object with the specified version of Flash.
         *
         * @param version the version of Flash that the script is being 
         * translated and compiled into.
         * 
         * @param encoding the character set used to encode strings.
         */
        ASInfo(int version, String encoding)
        {
            this.version = version;
            this.encoding = encoding;
            
        }

        /*
         * Adds a string to the table if it has not been added previously. Only
         * the first 256 strings are stored in the table. This should rarely be 
         * a limitation however.
         * 
         * @param str a string representing a string literal, identifier,
         * the name of a property or function.
         */
        void addString(String str)
        {
            if (strings.contains(str))
                useStrings = true;
            else if (strings.size() < 256)
            {
                strings.add(str);
            }
        }
    }
    
    /*
     * The Coder class is used to encode the objects representing the different
     * types of action that the ActionScript is translated into to the binary
     * format supported by Flash.
     *
     * The size of the byte array holding the encoded actions is calculated in
     * in advance to avoid dynamically resizing the array as new objects are 
     * encoded.
     */
    private class Coder
    {
        /*
         * The current version of Flash that the tree of ASNodes will
         * be translated and compiled into. 
         */
        int version = 0;
        
        /*
         * The character encoding used to represent identifiers and string 
         * literals.
         */
        String encoding = null;

        /*
         * The array of bytes used to encode the actions representing a script.
         */
        byte[] data = null;
        
        /*
         * The index into the array of bytes where the next byte being encoded
         * will be written to.
         */
        int ptr = 0;
        
        /*
         * Constructs a new Coder object with an array large enough to hold the 
         * encoded objects.
         */
        Coder(byte[] bytes, String encoding)
        {
            
            this.encoding = encoding;
            data = bytes;
        }
        
        /*
         * Encode an integer value. Values are encoded in Little-Endian format.
         * The number of bytes is specified allowing an int to represent 8, 16 
         * or 32-bit values.
         * 
         * @param value the number to be encoded
         * @param numberOfBytes the size, in bytes, of the value to be encoded.
         */
        void encode(int value, int numberOfBytes)
        {
            for (int i=0; i<numberOfBytes; i++, ptr++, value >>>= 8)
                data[ptr] = (byte)value;
        }
        
        /*
         * Encode a String value. String are assumed to be encoded using UTF-8.
         * 
         * @param str a String object to be encoded.
         */
        void encode(String str)
        {
            try
            {
                byte[] bytes = str.getBytes(encoding);
                
                for (int i=0; i<bytes.length; i++)
                    data[ptr++] = bytes[i];
                
                data[ptr++] = 0;
            }
            catch (java.io.UnsupportedEncodingException e) 
            {
            }
        }
    }
    
    /*
     * Coding is an abstract class used to specify the interface that the classes
     * that represent the different types of action that the ASNodes are 
     * translated into must support in order to be encoded to the Flash binary
     * format.
     */
    private abstract class Coding
    {
        /*
         * Identifies that an array of objects containing actions will be encoded
         * so it can be added to an object representing the DoAction data 
         * structure identifying the actions that are executed when a given frame 
         * is played in a movie.
         */
        static final int Frame = 1;

        /*
         * Identifies that an array of objects containing actions will be encoded
         * so it can be added to an object representing the DefineButton2 data 
         * structure identifying the actions that are executed when an event 
         * occurs in a button.
         */
        static final int Button = 2;

        /*
         * Identifies that an array of objects containing actions will be encoded
         * so it can be added to an object representing the PlaceObject2 data 
         * structure identifying the actions that are executed when an event 
         * occurs in a movie clip or sprite.
         */
        static final int MovieClip = 3;
        
        /*
         * length is used to return the number of bytes that an action object 
         * will occupy when it is encoded. This method is used to calculate
         * the size of the array of bytes that is allocated for the Coder object
         * that is used to encode the actions that represent a given script.
         * 
         * Each action contains type and length attributes that are used as header
         * values for the data structure that represents the action when it is 
         * encoded. The length(int) method sets the length attribute as a side 
         * effect when calculating the space to allocate for the encoded data  
         * removing the need to call the method again to when encoding the object. 
         * 
         * @param version the version of Flash.
         * @param encoding the character set used for strings.
         *
         * @return the number of bytes an action will occupy when encoded.
         */
        abstract int length(int version, String encoding);

        /**
         * encode is used to encode an action to the array of bytes maintained 
         * in the Coder object.
         * 
         * @param coder the Coder object that stores the encoded representation 
         * of the action.
         */
        abstract void encode(Coder coder);
    }
    
    /*
     * The ASArray is used to represent an array either actions, button event or 
     * clip event objects. The type of objects store depends on the type of script 
     * being encoded:
     *
     * For scripts executed when a frame is displayed in a movie then the array 
     * will contain Action objects.
     *
     * For scripts executed when an event occurs in a button then the array 
     * will contain ASEvent objects. Each ASEvent represents an on() statement
     * in ActionScript.
     *
     * For scripts executed when an event occurs in a movie clip then the array 
     * will contain ASEvent objects. Each ASEvent represents an onClipEvent() 
     * statement in ActionScript.
     * 
     * Note that the array should only contain objects of the same type.
     *
     * An ASArray object is used as the main wrapper for the encoded actions that 
     * a script is compiled into. The binary data generated can then be used to 
     * create a DoAction, PlaceObject2 or DefineButton2 data structure allowing
     * the actions to be added to a Flash movie.
     *
     * A single class is used to simplify the code and because the encoded
     * representations are very similar.
     */
    private class ASArray extends Coding
    {
        /*
         * Identifies the type of object that the array contains:
         *
         * Coding.Frame - the array contains Action objects.
         * Coding.Button - the array contains ASEvent objects for buttons.
         * Coding.MovieClip - the array contains ASEvent objects for movie clips.
         */
        int type = 0;
        
        ArrayList array = null;
        
        /*
         * Constructs an ASArray object of the specified type.
         *
         * @param type
         * @param array an array of actions, button event or clip event objects.
         */
        ASArray(int type, ArrayList array)
        {
            this.type = type;
            this.array = array;
        }
        
        /*
         * Adds an object to the array.
         *
         * @param object either an Action or an ASEvent.
         */
        void add(Coding object)
        {
            array.add(object);        
        }
        
        /*
         * Returns the number of bytes that the object will occupy when encoded.
         *
         * @return the number of bytes when encoded.
         */
        int length(int version, String encoding)
        {
            int length = 0;
            
            switch (type)
            {
                case Coding.Frame:
                    for (Iterator i=array.iterator(); i.hasNext();)
                        length += ((Coding)i.next()).length(version, encoding);
                    break;
                case Coding.Button:
                    for (Iterator i=array.iterator(); i.hasNext();)
                        length += 2 + ((Coding)i.next()).length(version, encoding);
                    break;
                case Coding.MovieClip:
                    length += version > 5 ? 10 : 6;
                    for (Iterator i=array.iterator(); i.hasNext();)
                        length += ((Coding)i.next()).length(version, encoding);
                    break;
            }        
            return length;
        }
        
        /*
         * Encodes the object to binary representation support by the 
         * Flash file format.
         *
         * @param coder a Coder object supporting method to encode the
         * object to an array of bytes.
         */
        void encode(Coder coder)
        {
            switch (type)
            {
                /*
                 * Encode the object so it can be used in a DoAction tag.
                 */
                case Coding.Frame:
                    for (Iterator i = array.iterator(); i.hasNext();) 
                        ((Coding)i.next()).encode(coder);            
                    break;
                /*
                 * Encode the object so it can be used in a DefineButton2 tag.
                 */
                case Coding.Button:
                    for (int i=0; i<array.size(); i++)
                    {
                        Coding event = (Coding) array.get(i);
                        boolean lastEvent = i >= array.size()-1;
                    
                        /*
                         * Write the offset in bytes to the next event. An offset
                         * of zero indicates that no more ASEvent object follow.
                         */
                        coder.encode((lastEvent == false) ? event.length(coder.version, coder.encoding)+2 : 0, 2);
                        event.encode(coder);
                    }
                    break;
                /*
                 * Encode the object so it can be used in a PlaceObject2 tag.
                 */
                case Coding.MovieClip:
                    int allEvents = 0;
                    int eventSize = coder.version > 5 ? 4 : 2;
                
                    /*
                     * Generate the event mask which contains bits set for each 
                     * event specified in the ASEvent objects.
                     */
                    for (int i=0; i<array.size(); i++)
                        allEvents |= ((ASEvent)array.get(i)).event;
                    
                    coder.encode(0, 2);
                    coder.encode(allEvents, eventSize);
    
                    for (int i=0; i<array.size(); i++)
                        ((Coding)array.get(i)).encode(coder);
    
                    /*
                     * Each ASEvent starts with a 16-bit which contains a series 
                     * of bit flag indicating which event the movie clip will
                     * respond to. Setting all the bits to zero is used to signal
                     * that no further ASEvent objects follow.
                     */
                    coder.encode(0, eventSize);
                    break;
            }
        }
    }
    
    /*
     * ASEvent is used to represent the ClipEvent or ButtonEvent data structures in
     * the Flash file format specification. Each contains an array of action objects
     * that are executed in response to an event occurring in a button or movie clip.
     *
     * An ASEvent structure also directly represents either an individual on() or 
     * onClipEvent() statement in ActionScript - depending on the type of script 
     * being parsed.
     *
     * To specify a script containing more than one on() or onClipEvent() statement
     * the ASEvent objects must be wrapped with an ASArray object so they will be 
     * encoded correctly.
     */
    private class ASEvent extends Coding
    {
        /*
         * Identifier whether the object represents a ButtonEvent object (Coding.Button)
         * or a ButtonEvent object (Coding.MovieClip).
         */
        int type = 0;
        
        /*
         * Used to calculate the offset to the next ASEvent when representing a 
         * ClipEvent object.
         */
        int length = 0;
        
        /*
         * Each event that the button or movie clip responds to is encoded as a 
         * sequence of flags. The flags are set when the original on() or onClipEvent() 
         * statement is parsed. Please see the Flash file format specification from 
         * Macromedia for more details.
         */
        int event = 0;
        
        /*
         * Movie clips from Flash 6 onwards support the button event model. This allows
         * a key to be specified directly rather than testing which key was pressed 
         * using the built-in Key object.
         */
        int keyCode = 0;
        
        /*
         * Array containing the action objects that the ActionScript is translated into.
         */
        ArrayList array = new ArrayList();
        
        /*
         * Constructs an ASEvent object that representing either a ButtonEvent or 
         * ClipEvent.
         *
         * @param the type of structure that the ASEvent object represents Coding.Button
         * for a ButtonEvent or Coding.MovieClip for a ClipEvent.
         *
         * @param event the set of flags indicating the series of events that the 
         * actions will be executed in response to.
         *
         * @param array and array of action objects that will be executed when the 
         * specified event(s) occur.
         */
        ASEvent(int type, int event, ArrayList array)
        {
            this.type = type;
            this.event = event;
            this.array = array;
        }
        
        /*
         * Constructs an ASEvent object that representing either a ButtonEvent or 
         * ClipEvent.
         *
         * @param the type of structure that the ASEvent object represents Coding.Button
         * for a ButtonEvent or Coding.MovieClip for a ClipEvent.
         *
         * @param event the set of flags indicating the series of events that the 
         * actions will be executed in response to.
         *
         * @param code the code for the key that is pressed on the keyboard to 
         * trigger the event.
         *
         * @param array and array of action objects that will be executed when the 
         * specified event(s) occur.
         */
        ASEvent(int type, int event, int code, ArrayList array)
        {
            this.type = type;
            this.event = event;
            this.keyCode = code;
            this.array = array;
        }
        
        /*
         * Calculate the number of bytes required to encode the object.
         *
         * @param version the version number of Flash.
         *
         * @return the number of bytes when encoded.
         */
        int length(int version, String encoding)
        {
            switch (type)
            {
                case Coding.MovieClip:
                    length = 5 + ((version > 5) ? 4 : 2);
                    
                    if (version > 5 && (event & 131072) != 0)
                        length += 1;
                    
                    for (Iterator i=array.iterator(); i.hasNext();)
                        length += ((Coding)i.next()).length(version, encoding);
                    break;
                case Coding.Button:
                    length = 3;
                    
                    for (Iterator i=array.iterator(); i.hasNext();)
                        length += ((Coding)i.next()).length(version, encoding);
                    break;
            }
            return length;
        }
        
        /*
         * Encode the object.
         *
         * @param coder the Coder object that stores the encoded representation of
         * the action.
         */
        void encode(Coder coder)
        {
            int eventSize = (coder.version > 5) ? 4 : 2;
            
            switch (type)
            {
                case Coding.MovieClip:
                    coder.encode(event, eventSize);
                    coder.encode(length-(4+eventSize), 4);
                
                    if (coder.version > 5 && (event & 131072) != 0)
                        coder.encode(keyCode, 1);
                    
                    for (Iterator i = array.iterator(); i.hasNext();) 
                        ((Coding)i.next()).encode(coder);
                        
                    coder.encode(0, 1);
                    break;
                case Coding.Button:
                    coder.encode(event, 2);
                
                    for (Iterator i = array.iterator(); i.hasNext();) 
                        ((Coding)i.next()).encode(coder);

                    coder.encode(0, 1);
                    break;
            }
        }
    }

    /*
     * Action is used to represent the byte-codes that specify the stack-based 
     * actions that are executed by the Flash Player. The Action class is also 
     * the parent class for more complicated actions that contain arguments. 
     */
    private class Action extends Coding
    {
        /*
         * The byte-codes used to represent the different type of stack-based 
         * actions supported by the Flash Player.
         */
        static final int NextFrame            = 4;
        static final int PrevFrame            = 5;
        static final int Play                 = 6;
        static final int Stop                 = 7;
        static final int ToggleQuality        = 8;
        static final int StopSounds           = 9;
        static final int IntegerAdd           = 10;
        static final int Subtract             = 11;
        static final int Multiply             = 12;
        static final int Divide               = 13;
        static final int IntegerEquals        = 14;
        static final int IntegerLess          = 15;
        static final int And                  = 16;
        static final int Or                   = 17;
        static final int Not                  = 18;
        static final int StringEquals         = 19;
        static final int StringLength         = 20;
        static final int StringExtract        = 21;
        static final int Pop                  = 23;
        static final int ToInteger            = 24;
        static final int GetVariable          = 28;
        static final int SetVariable          = 29;
        static final int SetTarget2           = 32;
        static final int StringAdd            = 33;
        static final int GetProperty          = 34;
        static final int SetProperty          = 35;
        static final int CloneSprite          = 36;
        static final int RemoveSprite         = 37;
        static final int Trace                = 38;
        static final int StartDrag            = 39;
        static final int EndDrag              = 40;
        static final int StringLess           = 41;
        static final int RandomNumber         = 48;
        static final int MBStringLength       = 49;
        static final int CharToAscii          = 50;
        static final int AsciiToChar          = 51;
        static final int GetTime              = 52;
        static final int MBStringExtract      = 53;
        static final int MBCharToAscii        = 54;
        static final int MBAsciiToChar        = 55;
        static final int DeleteVariable       = 58;
        static final int Delete                  = 59;
        static final int InitVariable         = 60;
        static final int ExecuteFunction      = 61;
        static final int Return               = 62;
        static final int Modulo                  = 63;
        static final int NamedObject          = 64;
        static final int NewVariable          = 65;
        static final int NewArray             = 66;
        static final int NewObject            = 67;
        static final int GetType              = 68;
        static final int GetTarget            = 69;
        static final int Enumerate            = 70;
        static final int Add                  = 71;
        static final int Less                 = 72;
        static final int Equals               = 73;
        static final int ToNumber             = 74;
        static final int ToString             = 75;
        static final int Duplicate            = 76;
        static final int Swap                 = 77;
        static final int GetAttribute         = 78;
        static final int SetAttribute         = 79;
        static final int Increment            = 80;
        static final int Decrement            = 81;
        static final int ExecuteMethod        = 82;
        static final int NewMethod            = 83;
        static final int InstanceOf           = 84;
        static final int BitwiseAnd           = 96;
        static final int BitwiseOr            = 97;
        static final int BitwiseXOr           = 98;
        static final int LogicalShiftLeft     = 99;
        static final int ArithmeticShiftRight = 100;
        static final int LogicalShiftRight    = 101;
        
        /*
         * Call is represented as a byte-code however it also encodes a length field 
         * containing zero - a poor design decision by Macromedia.
         */
        static final int Call                 = 158;
        
        /*
         * The following actions are represented using instances of the ValueAction class.
         */
        static final int GotoFrame            = 129;
        static final int GetUrl               = 131;
        static final int RegisterCopy         = 135;
        static final int WaitForFrame         = 138;
        static final int SetTarget            = 139;
        static final int GotoLabel            = 140;
        static final int WaitForFrame2        = 141;
        static final int With                 = 148;
        static final int Jump                 = 153;
        static final int GetUrl2              = 154;
        static final int If                   = 157;
        static final int GotoFrame2           = 159;
        
        /*
         * The following actions contain multiple arguments and are represented by
         * dedicated classes.
         */
        static final int Table                = 136;
        static final int Push                 = 150;
        static final int NewFunction          = 155;

        /*
         * Identifiers for the different type of arguments used when using the GetUrl2 
         * action to load web pages, movie clips or send and receive variable values 
         * from a server. 
         */
        static final int MovieToLevel              = 0;
        static final int MovieToLevelWithGet       = 1;
        static final int MovieToLevelWithPost      = 2;
        static final int MovieToTarget             = 64;
        static final int MovieToTargetWithGet      = 65;
        static final int MovieToTargetWithPost     = 66;
        static final int VariablesToLevel          = 128;
        static final int VariablesToLevelWithGet   = 129;
        static final int VariablesToLevelWithPost  = 130;
        static final int VariablesToTarget         = 192;
        static final int VariablesToTargetWithGet  = 193;
        static final int VariablesToTargetWithPost = 194;
        
        protected int type = 0;
        protected int length = 1;
        
        /*
         * Constructs an action of the specified type. The type is used to identify the 
         * action when the object is encoded.
         *
         * @param aType the code identifying the type of action.
         */
        Action(int aType)
        {
            type = aType;
            
            if (type == Call)
                length = 3;
        }
        
        /*
         * Return the action type.
         *
         * @return the code used to identify the type of action.
         */
        int type()
        {
            return type;
        }
        
        /*
         * Return the length of the action when encoded.
         *
         * @param version the version number of Flash.
         *
         * @return the number of bytes used to encode the action.
         */
        int length(int version, String encoding)
        {
            return length; 
        }
    
        /*
         * Encode the action.
         *
         * @param coder the Coder object that stores the encoded representation of 
         * the action.
         */
        void encode(Coder coder)
        {
            coder.encode(type, 1);

            if (type == Call)
                coder.encode(length-3, 2);
        }
    }
    
    /*
     * The ValueAction class is used to represent actions that contain an argument.
     *
     * The class supports either an integer or one to two strings. The type of action 
     * represented determines which attributes are used. All three are combined to reduce 
     * the amount of code compared to implementing different classes.
     *
     * Note only GetUrl uses both string attributes. All other actions use either the 
     * integer or one string attribute.
     */
    private class ValueAction extends Action
    {
        int iValue = 0;
        String sValueA = null;
        String sValueB = null;
        
        /*
         * Constructs a ValueAction that contains an integer argument.
         *
         * @param type the code identifying the type of action.
         * @param value the integer argument.
         */
        ValueAction(int type, int value)
        {
            super(type);
            
            switch (this.type)
            {
                case GetUrl2:
                case GotoFrame2:
                case RegisterCopy:
                    length = 4;
                    break;
                default:
                    length = 5;
                    break;
            }
            iValue = value;            
        }

        /*
         * Constructs a ValueAction that contains a String argument.
         *
         * @param type the code identifying the type of action.
         * @param value the String argument.
         */
        ValueAction(int type, String value)
        {
            super(type);
            length = 4 + value.length();
            sValueA = value;
        }

        /*
         * Constructs a ValueAction that represents a GetUrl action.
         *
         * @param type the code identifying the type of action.
         *
         * @param a the String argument specifying the URL to load.
         *
         * @param b the String argument specifying the window or level in the movie 
         * in which to load the information obtained when the URL is submitted to
         * the server.
         */
        ValueAction(int type, String a, String b)
        {
            super(type);
            this.length = 5 + a.length() + b.length();
            sValueA = a;
            sValueB = b; 
        }

        /*
         * Encode the action.
         *
         * @param coder the Coder object that stores the encoded representation of 
         * the action.
         */
        void encode(Coder coder)
        {
            coder.encode(this.type, 1);
            coder.encode(length-3, 2);
            
            if (sValueA != null)
            {
                coder.encode(sValueA);
            
                if (sValueB != null)
                    coder.encode(sValueB);
            }
            else
            {
                coder.encode(iValue, length-3);
            }
        }
    }
    
    /*
     * The Table class is used to represent the Table action which contains the array
     * of strings found in a script. Strings may be pushed onto the Flash Player's 
     * stack by specifying the index into the string table rather than the string itself.
     */
    private class Table extends Action
    {
        private ArrayList values = null;
        
        /*
         * Construct a Table action with an array of strings. The table supports up to
         * 256 strings.
         *
         * @param array an array of Strings used to fill the table.
         */
        Table(ArrayList array)
        {
            super(Table);
            values = array;
        }

        /*
         * Return the length of the Table action when encoded.
         *
         * @return the number of bytes the Table object will occupy when encoded.
         */ 
        int length(int version, String encoding)
        {
            length = 5;
            
            try
            {
                for (Iterator i = values.iterator(); i.hasNext();) 
                    length += ((String)i.next()).getBytes(encoding).length+1;
            }
            catch (Exception e)
            {
                
            }
            return length;
        }
    
        /*
         * Encodes the Table action to the binary representation supported in the 
         * Flash file format.
         *
         * @param coder the Coder object that stores the encoded representation of 
         * the action.
         */
        void encode(Coder coder)
        {
            coder.encode(this.type, 1);
            coder.encode(length-3, 2);
            coder.encode(values.size(), 2);
            
            for (Iterator i = values.iterator(); i.hasNext();)
                coder.encode((String)i.next());
        }
    }
    
    /*
     * Null is a lightweight class used to push null values onto the Flash 
     * Player's stack using the Push action.
     */
    private class Null { Null() {} }    

    /*
     * Void is a lightweight class used to push void values onto the Flash 
     * Player's stack using the Push action.
     */
    private class Void { Void() {} }    
    
    /*
     * TableIndex is a lightweight object used to push a reference to an entry
     * in a Table object onto the Flash Player's stack using a Push action.
     */
    private class TableIndex
    {
        int index = 0;
        
        TableIndex(int anIndex) { index = anIndex; }
    }
    
    /*
     * RegisterIndex is a lightweight object used to push a reference to one 
     * of the Flash Player's four internal registers on to the Flash Player's 
     * stack using a Push action.
     */
    private class RegisterIndex
    {
        int index = 0;
        
        RegisterIndex(int anIndex) { index = anIndex; }
    }
    /*
     * Property is a lightweight object used to push a movie property onto the
     * Flash Player's stack using a Push action.
     */
    private class Property
    {
        int value = 0;
        
        Property(int val) { value = val; }
    }
    
    /*
     * Push is used to push literals onto the Flash Player's stack. A literal
     * may be either a boolean, integer, double-precision floating point value,
     * null, void, an index into a table of string literals or a reference to
     * one of the Flash Player's internal registers.
     */
    private class Push extends Action
    {
        private ArrayList values = new ArrayList();
        
        /*
         * Constructs a new Push action adding the specified object to the 
         * (empty) array of values.
         *
         * @param an object containing a value to be pushed onto the Flash 
         * Player' stack - must be either a Boolean, Integer, Double, 
         * String, Null, Void, TableIndex or RegisterIndex.
         */
        Push(Object value)
        {
            super(Push);
            values.add(value);
        }
        
        /*
         * Get the array of values that will be pushed onto the Flash Player's stack.
         *
         * @return array of values to be pushed on the stack.
         */
        ArrayList getValues()
        {
            return values;
        }

        /*
         * Add an object to the array of values.
         *
         * @param an object containing a value to be pushed onto the Flash 
         * Player' stack - must be either a Boolean, Integer, Double, 
         * String, Null, Void, TableIndex or RegisterIndex.
         */
        void add(Object anObject)
        {
            values.add(anObject);
        }

        /* 
         * Return the length of the action when encoded to the Flash binary format.
         *
         * @return the number of bytes the object occupies when encoded.
         */
        int length(int version, String encoding)
        {
            length = 3;
        
            for (Iterator i=values.iterator(); i.hasNext();)
            {
                Object anObject = i.next();
        
                if (anObject instanceof Boolean)
                    length += 2;
                else if (anObject instanceof Integer)
                    length += 5;
                else if (anObject instanceof Double)
                    length += 9;
                else if (anObject instanceof String) 
                {
                    try {
                        length += ((String)anObject).getBytes(encoding).length+2;
                    }
                    catch (java.io.UnsupportedEncodingException e) 
                    {
                    }
                }
                else if (anObject instanceof Null)
                    length += 1;
                else if (anObject instanceof Void)
                    length += 1;
                else if (anObject instanceof TableIndex)
                    length += 2;
                else if (anObject instanceof RegisterIndex)
                    length += 2;
                else if (anObject instanceof Property)
                    length += 5;
            }
            return length;
        }
    
        /*
         * Encodes the Push action to the binary representation supported in the 
         * Flash file format.
         *
         * @param coder the Coder object that stores the encoded representation of 
         * the action.
         */
        void encode(Coder coder)
        {
            coder.encode(this.type, 1);
            coder.encode(length-3, 2);

            for (Iterator i=values.iterator(); i.hasNext();)
            {
                Object anObject = i.next();
        
                if (anObject instanceof Boolean)
                {
                    coder.encode(5, 1);
                    coder.encode(((Boolean)anObject).booleanValue() ? 1 : 0, 1);
                }
                else if (anObject instanceof Integer)
                {
                    coder.encode(7, 1);
                    coder.encode(((Integer)anObject).intValue(), 4);
                }
                else if (anObject instanceof Double)
                {
                    coder.encode(6, 1);

                    long longValue = Double.doubleToLongBits(((Double)anObject).doubleValue());
                
                    int lowerInt = (int)longValue;
                    int upperInt = (int)(longValue >>> 32);
       
                    coder.encode(upperInt, 4);
                    coder.encode(lowerInt, 4);
                }
                else if (anObject instanceof Null)
                {
                    coder.encode(2, 1);
                }
                else if (anObject instanceof Void)
                {
                    coder.encode(3, 1);
                }
                else if (anObject instanceof RegisterIndex)
                {
                    coder.encode(4, 1);
                    coder.encode(((RegisterIndex)anObject).index, 1);
                }
                else if (anObject instanceof TableIndex)
                {
                    coder.encode(8, 1);
                    coder.encode(((TableIndex)anObject).index, 1);
                }
                else if (anObject instanceof Property)
                {
                    coder.encode(1, 1);
                    coder.encode(((Property)anObject).value, 4);
                }
                else if (anObject instanceof String)
                {
                    coder.encode(0, 1);
                    coder.encode((String)anObject);
                }
            }
        }
    }
    
    /*
     * NewFunction is used to define functions. The class differs slightly from 
     * the NewFunction action in the Flash file format specification in that the 
     * class also contains the array of actions that implement the body of the 
     * function - in the Flash specification the actions are separate and follow
     * immediately after the NewFunctionAction. Here the actions are added to 
     * simplify the encoding.
     */
    private class NewFunction extends Action
    {
        /*
         * A separate attribute is used to store the length of the array of 
         * actions as the array does not form part of the encoded representation.
         * The NewFunction action encodes the length of the actions following
         * which make up the body of the function.
         */
        private int actionLength = 0;
        
        private String name = null;
        private ArrayList arguments = null;
        private ArrayList actions = null;
    
        /*
         * Constructs a NewFunction action with the name of the function,
         * any arguments and the array of actions that make up the body
         * of the function. If no arguments are required then the arguments
         * array must be empty.
         *
         * @param name the name of the function. This may be the empty
         * string if the NewFunction action is used to define a method for
         * an object.
         *
         * @param arguments an array of strings listing the names of the
         * arguments that will be passed to the function. If no arguments
         * will be supplied then the array should be empty.
         *
         * @param actions the array of action objects that define the 
         * body of the function.
         */
        NewFunction(String name, ArrayList arguments, ArrayList actions, int version, String encoding)
        {
            super(NewFunction);
            
            this.name = name;
            this.arguments = arguments;
            this.actions = actions;
            
            length = 8 + name.length();
            
            for (Iterator i = arguments.iterator(); i.hasNext();) 
                length += ((String)i.next()).length()+1;

            for (Iterator i = actions.iterator(); i.hasNext();) 
                actionLength += ((Coding)i.next()).length(version, encoding);
        }
        
        /*
         * Return the length of the action when encoded to the Flash binary format.
         *
         * @param version the version number of Flash.
         * @param encoding the character set used to encode strings.
         *
         * @return the number of bytes the object occupies when encoded.
         */
        int length(int version, String encoding)
        {
            return length + actionLength;
        }
    
        /*
         * Encodes the NewFunction action to the binary representation supported in the 
         * Flash file format.
         *
         * @param coder the Coder object that stores the encoded representation of 
         * the action.
         */
        void encode(Coder coder)
        {
            coder.encode(this.type, 1);
            coder.encode(length-3, 2);
            coder.encode(name);
            coder.encode(arguments.size(), 2);
            
            for (Iterator i=arguments.iterator(); i.hasNext();)
                coder.encode((String)i.next());

            coder.encode(actionLength, 2);
            
            for (Iterator j=actions.iterator(); j.hasNext();)
                ((Coding)j.next()).encode(coder);
        }
    }

    /**
     * Array nodes are used to represent any list of ActionScript statements.
     * Use this type of node when constructing trees to represent sequences of 
     * actions for FSDoAction, FSClipEvent or FSButtonEvent objects.
       */
    public static final int Array     = 1;
    /**
     * Button nodes are used to represent the on() block statement in ActionScript.
     * Use this type of node when constructing trees that will be encoded and 
     * added to FSDefineButton2 objects.
     */
    public static final int Button    = 2;
    /**
     * MovieClip nodes are used to represent the onClipEvent() block statement 
     * in ActionScript. Use this type of node when constructing trees that will 
     * be encoded and added to FSPlaceObject2 objects.
     */
    public static final int MovieClip = 3;
    /**
     * List nodes are used to represent groups of one or more statements. They are
     * used to represent statements included in any block structure such as an 
     * if statement or for loop.
     * 
     * Lists are also used to simplify the construction of complex statements 
     * such as for loops. Using Lists, a for loop contains a maximum of four child 
     * nodes with lists used to group the statements forming the initialisation 
     * and iteration part of the for statement and body of the loop.
     */
    public static final int StatementList = 4;
    public static final int List = 5;
    /**
     * NoOp is used as a place-holder for child nodes with resorting to using null.
     * No actions will be generated when the node is translated.
     */
    public static final int NoOp = 6;
    
    /** Use to represent if statements */
    public static final int If          = 7;
    /** Use to represent for loops */
    public static final int For         = 8;
    /** Use to represent for..in statements */
    public static final int ForIn       = 9;
    /** Use to represent while loops */
    public static final int While       = 10;
    /** Use to represent do..while loops */
    public static final int Do          = 11;
    /** Use to represent with statements */
    public static final int With        = 12;
    /** Use to represent onClipEvent statements */
    public static final int OnClipEvent = 13;
    /** Use to represent on statements */
    public static final int On          = 14;
    /** Use to represent break statements */
    public static final int Break       = 15;
    /** Use to represent return statements */
    public static final int Return      = 16;
    /**< Use to represent continue statements */
    public static final int Continue    = 17;
    /**
     * Value is an abstract node type used to group together nodes that will result
     * in a value being generated such as subscripting an array variable or dereferencing
     * an object's attribute.
     */
    public static final int Value      = 18;

    /** Use to represent a boolean value */
    public static final int BooleanLiteral    = 20;
    /** Use to represent an integer value */
    public static final int IntegerLiteral    = 21;
    /** Use to represent an double-precision floating point value */
    public static final int DoubleLiteral    = 22;
    /** Use to represent a string value */
    public static final int StringLiteral    = 23;
    /** Use to represent a null literal */
    public static final int NullLiteral    = 24;

    /** Use to represent a variable */
    public static final int Identifier = 30;
    /** Use to represent an attribute of an object */
    public static final int Attribute  = 31;
    /** Use to represent the name of a method */
    public static final int Method     = 32; 
    /** Use to represent the name of one of ActionScript's built-in functions.  */
    public static final int Function   = 33;
    /** Use to represent new statements for creating instances of objects. */
    public static final int NewObject  = 34;
    /** Use to represent subscript operation when accessing the elements of an array. */
    public static final int Subscript  = 35;


    /** Use to represent a user defined function. */
    public static final int DefineFunction  = 36; 
    /** Use to represent an anonyomus array. */
    public static final int DefineArray     = 37; 
    /** Use to represent a user defined object. */
    public static final int DefineObject    = 38; 
    /** Use to represent a method on a user defined object. */
    public static final int DefineMethod    = 39; 
    /** Use to represent an attribute on a user defined object. */
    public static final int DefineAttribute = 40; 
    /** Use to represent a var statement */
    public static final int DefineVariable  = 41; 
    /** Add operation */
    public static final int Add              = 42; 
    /** Subtract operation */
    public static final int Sub              = 43; 
    /** Multiply operation */
    public static final int Mul              = 44; 
    /** Divide operation */
    public static final int Div              = 45; 
    /** Modulo operation */
    public static final int Mod              = 46; 
    /** Logical Shift Left operation */
    public static final int LSL              = 47; 
    /** Arithmetic Shift Right operation */
    public static final int ASR              = 48; 
    /** Logical Shift Right operation */
    public static final int LSR              = 49; 
    /** Bitwise AND operation */
    public static final int BitAnd           = 50; 
    /** Bitwise OR operation */
    public static final int BitOr            = 51; 
    /** Bitwise Exclusive-OR operation */
    public static final int BitXOr           = 52; 
    /** Logical AND operation */
    public static final int And              = 53; 
    /** Logical OR operation */
    public static final int Or               = 54; 
    /** Equal comparison */
    public static final int Equal            = 55; 
    /** Not Equal comparison */
    public static final int NotEqual         = 56; 
    /** Greater Than comparison */
    public static final int GreaterThan      = 57; 
    /** Less Than comparison */
    public static final int LessThan         = 58; 
    /** Greater Than or Equal comparison */
    public static final int GreaterThanEqual = 59; 
    /** Less Than or Equal comparison */
    public static final int LessThanEqual    = 60; 
    /** ternary operator. */
    public static final int Select = 61; 
    /** Unary not */
    public static final int Not      = 62; 
    /** Unary bit-not */
    public static final int BitNot   = 63; 
    /** Unary plus */
    public static final int Plus     = 64; 
    /** Unary minus */
    public static final int Minus    = 65; 
    /** Pre-increment */
    public static final int PreInc   = 66; 
    /** Pre-decorement */
    public static final int PreDec   = 67; 
    /** Post-increment */
    public static final int PostInc  = 68; 
    /** Post-decrement */
    public static final int PostDec  = 69; 
    /** Assign, = */
    public static final int Assign       = 70; 
    /** Assign add, += */
    public static final int AssignAdd    = 71; 
    /** Assign subtract, -= */
    public static final int AssignSub    = 72; 
    /** Assign multiply, *= */
    public static final int AssignMul    = 73; 
    /** Assign divide, /= */
    public static final int AssignDiv    = 74; 
    /** Assign modulo, %= */
    public static final int AssignMod    = 75; 
    /** Assign logical shift left, <<= */
    public static final int AssignLSL    = 76; 
    /** Assign arithmetic shift right, >>= */
    public static final int AssignASR    = 77; 
    /** Assign logical shift right, >>>= */
    public static final int AssignLSR    = 78; 
    /** Assign bitwise-AND, &= */
    public static final int AssignBitAnd = 79; 
    /** Assign bitwise-OR, |= */
    public static final int AssignBitOr  = 80; 
    /** Assign bitwise-exclusive-OR, ^= */
    public static final int AssignBitXOr = 81; 
    /** Object identity */
    public static final int InstanceOf = 82; 
    /** Object reclamation */
    public static final int Delete = 83; 
    
    /*
     * Names for each of the different types of node. Names are used in the 
     * toString() method.
     */
    private static String[] nodeNames = {
        "",
        "Frame",
        "Button",
        "MovieClip",
        "Statements",
        "List",
        "NoOp",
        "if",
        "for",
        "for..in",
        "while",
        "do..while",
        "With",
        "OnClipEvent",
        "On",
        "Break",
        "Return",
        "Continue",
        "Value",
        "",
        "Boolean",
        "Integer",
        "Double",
        "String",
        "Null",
        "",
        "",
        "",
        "",
        "",
        "Identifier",
        "Attribute",
        "Method",
        "Function",
        "NewObject",
        "Subscript",
        "Define Function",
        "Define Array",
        "Define Object",
        "Define Method",
        "Define Attribute",
        "Define Variable",
        "+",
        "-",
        "*",
        "/",
        "%",
        "<<",
        ">>",
        ">>>",
        "&",
        "|",
        "^",
        "&&",
        "||",
        "==",
        "!=",
        ">",
        "<",
        ">=",
        "<=",
        "?",
        "!",
        "~",
        "+x",
        "-x",
        "++x",
        "--x",
        "x++",
        "x--",
        "=",
        "+=",
        "-=",
        "*=",
        "/=",
        "%=",
        "<<=",
        ">>=",
        ">>>=",
        "&=",
        "|=",
        "^=",
        "intanceof",
        "delete",
    };

    /*
     * Table for the different types of events that buttons respond to. The table 
     * is accessible in the package as it is used in the ASParser to convert the 
     * identifiers representing the different events into the codes indicating 
     * which flags are set.
     */ 
    static HashMap buttonEvents = new HashMap();

    /*
     * Table for the different types of events that movie clips respond to. The 
     * table is package accessible as it is used in the ASParser to convert the 
     * identifiers representing the different events into the codes indicating 
     * which flags are set.
     */ 
    static HashMap clipEvents = new HashMap();

    // Table for constants defined in Flash.
    private static Hashtable constants = new Hashtable();
    // Table for properties defined in Flash.
    private static HashMap propertyNames = new HashMap();
    // Table for properties defined in Flash 4 or earlier.
    private static HashMap earlyPropertyNames = new HashMap();
    // Table for the functions built into Flash.
    private static HashMap functions = new HashMap();
    // Table for the functions built into Flash that return a value.
    private static HashMap valueFunctions = new HashMap();
    // Table for the classes built into Flash that return a value.
    private static HashMap classes = new HashMap();
        
    static {
        /*
         * Button events identifies the values that represents the bit flags that 
         * are set in the encoded event field as well as code values for special
         * keyboard keys.
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
         * Button events identifies the values that represents the bit flags that 
         * are set in the encoded event field.
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
        constants.put("Math.LOG2E", new Double(Math.log(Math.E)/Math.log(2)));
        constants.put("Math.LN10", new Double(Math.log(10)));
        constants.put("Math.LOG10E", new Double(Math.log(Math.E)/Math.log(10)));
        constants.put("Math.PI", new Double(Math.PI));
        constants.put("Math.SQRT1_2", new Double(Math.sqrt(0.5)));
        constants.put("Math.SQRT2", new Double(Math.sqrt(2)));
        constants.put("Number.MAX_VALUE", new Double(Double.MAX_VALUE));
        constants.put("Number.MIN_VALUE", new Double(Double.MIN_VALUE));
        constants.put("Number.NaN", new Double(Double.NaN));
        constants.put("Number.NEGATIVE_INFINITY", new Double(Double.NEGATIVE_INFINITY));
        constants.put("Number.POSITIVE_INFINITY", new Double(Double.POSITIVE_INFINITY));
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
         * The functions table is only used to identify built-in functions so
         * no value is associated with each name.
         */
        functions.put("delete", null);
        functions.put("duplicateMovieClip", null);
        functions.put("eval", null);
        functions.put("fscommand", null);
        functions.put("getProperty", null);
        functions.put("getURL", null);
        functions.put("getVersion", null);
        functions.put("gotoAndPlay", null);
        functions.put("gotoAndStop", null);
        functions.put("hitTest", null);
        functions.put("loadMovie", null);
        functions.put("loadVariables", null);
        functions.put("nextFrame", null);
        functions.put("nextScene", null);
        functions.put("Number", null);
        functions.put("play", null);
        functions.put("prevFrame", null);
        functions.put("prevScene", null);
        functions.put("print", null);
        functions.put("printAsBitmap", null);
        functions.put("random", null);
        functions.put("removeMovieClip", null);
        functions.put("set", null);
        functions.put("setProperty", null);
        functions.put("startDrag", null);
        functions.put("stop", null);
        functions.put("stopAllSounds", null);
        functions.put("stopDrag", null);
        functions.put("String", null);
        functions.put("targetPath", null);
        functions.put("toggleHighQuality", null);
        functions.put("trace", null);
        functions.put("typeof", null);
        functions.put("unloadMovie", null);
        functions.put("void", null);

        /*
         * The functions table is only used to identify built-in functions that
         * return a value to determine whether a pop action should be generated
         * if the value returned by the function is not assigned to a variable.
         */
        valueFunctions.put("attachMovie", null);
        valueFunctions.put("delete", null);
        valueFunctions.put("escape", null);
        valueFunctions.put("eval", null);
        valueFunctions.put("getBounds", null);
        valueFunctions.put("getBytesLoaded", null);
        valueFunctions.put("getBytesTotal", null);
        valueFunctions.put("getProperty", null);
        valueFunctions.put("getVersion", null);
        valueFunctions.put("globalToLocal", null);
        valueFunctions.put("hitTest", null);
        valueFunctions.put("isFinite", null);
        valueFunctions.put("isNaN", null);
        valueFunctions.put("localToGlobal", null);
        valueFunctions.put("parseFloat", null);
        valueFunctions.put("parseInt", null);
        valueFunctions.put("random", null);
        valueFunctions.put("swapDepths", null);
        valueFunctions.put("targetPath", null);
        valueFunctions.put("typeof", null);
        valueFunctions.put("unescape", null);
        valueFunctions.put("updateAfterEvent", null);
        valueFunctions.put("void", null);
    
        classes.put("Math", null);
        classes.put("Clip", null);
    }
    
    private int type = 0;
    
    /*
     * Nodes may store integer, floating-point literals or the names of functions, 
     * identifiers or string literals. Separate attributes are used rather than 
     * an Object to avoid repeated class casting, improve readability of code and 
     * increase performance.
     */
    private int iValue = 0;
    private double dValue = Double.NaN;
    private String sValue = null;
    private boolean bValue = false;
    
    /*
     * the discardValue flag is used to signal to a node that the value it returns 
     * is not used by the parent and so a pop action should be added when the node
     * is translated into action objects.
     *
     * The discardValue flag is set by the parent node through the discardValues()
     * method.
     */
    private boolean discardValue = false;
    
    /*
     * insertIndex is used when reordering nodes so that function definitions defined 
     * within a block are placed at the start - mirroring the behaviour of the Flash 
     * authoring application. The index is used to preserve the order in which functions
     * are defined making regression testing easier when comparing the code generated
     * by a node against the code generated by the Flash authoring application.
     */
    private int insertIndex = 0;
    
    private ASNode parent = null;
    private ASNode[] children = null;
    
    /*
     * The number attribute is used either to store the line number in a script where
     * the node was generated or an identifier for the node if a node tree was created
     * manually. The number is used when reporting errors while validating the nodes.
     */
    private int number = 0;
    
    /**
     * Constructs an ASNode with the specified type.
     *
     * @param nodeType the type of node being constructed.
     */
    public ASNode(int nodeType) 
    {
        type = nodeType;
    }
    
    /**
     * Constructs an ASNode with the specified type and integer value. This 
     * constructor is primarily used to create nodes representing integer literals.
     *
     * @param nodeType the type of node being constructed.
     * @param value the integer value assigned to the node.
     */
    public ASNode(int nodeType, int value) 
    {
        type = nodeType;
        iValue = value;
    }
    
    /**
     * Constructs an ASNode with the specified type and floating-point value. This 
     * constructor is primarily used to create nodes representing literals.
     *
     * @param nodeType the type of node being constructed.
     * @param value the floating-point value assigned to the node.
     */
    public ASNode(int nodeType, double value) 
    {
        type = nodeType;
        dValue = value;
    }
    
    /**
     * Constructs an ASNode with the specified type and string value. This 
     * constructor is primarily used to create string literals and identifiers.
     *
     * @param nodeType the type of node being constructed.
     * @param value the string assigned to the node.
     */
    public ASNode(int nodeType, String value) 
    {
        type = nodeType;
        sValue = value;
    }
    
    /**
     * Constructs an ASNode with the specified type and adds the child node.
     *
     * @param nodeType the type of node being constructed.
     * @param node a child node which will be added to the new node.
     */
    public ASNode(int nodeType, ASNode node) 
    {
        type = nodeType;
        
        add(node);
    }
    
    /**
     * Constructs an ASNode with the specified type and adds the child nodes.
     *
     * @param nodeType the type of node being constructed.
     * @param node1 a child node which will be added to the new node.
     * @param node2 a child node which will be added to the new node.
     */
    public ASNode(int nodeType, ASNode node1, ASNode node2) 
    {
        type = nodeType;
        
        add(node1);
        add(node2);
    }
    
    /**
     * Gets the type of the node.
     *
     * @return the type assigned to the node.
     */
    public int getType()
    {
        return type;
    }
    
    /**
     * Sets the type of the node.
     *
     * @param type the type assigned to the node.
     */
    public void setType(int type)
    {
        this.type = type;
    }
    
    /**
     * Get the boolean value assigned to a node.
     *
     * @return the boolean value assigned to a node.
     */
    public boolean getBoolValue()
    {
        return bValue;
    }
    
    /**
     * Set the boolean value assigned to a node.
     *
     * @param value a value that will be assigned to the node.
     */
    public void setBoolValue(boolean value)
    {
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
    public int getIntValue()
    {
        return iValue;
    }
    
    /**
     * Set the integer value assigned to a node.
     *
     * @param value a value that will be assigned to the node.
     */
    public void setIntValue(int value)
    {
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
    public double getDoubleValue()
    {
        return dValue;
    }
    
    /**
     * Set the floating-point value assigned to a node.
     *
     * @param value a floating-point value that will be assigned to the node.
     */
    public void setDoubleValue(double value)
    {
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
    public String getStringValue()
    {
        return sValue;
    }

    /**
     * Set the number assigned to a node.
     *
     * @param value a unique number that will be assigned to the node.
     */
    public void setNumber(int value)
    {
        number = value;
    }
    
    /**
     * Get the number assigned to a node.
     *
     * @return the number assigned to a node.
     */
    public int getNumber()
    {
        return number;
    }

    /**
     * Set the string value assigned to a node.
     *
     * @param value a string that will be assigned to the node.
     */
    public void setStringValue(String value)
    {
        bValue = false;
        iValue = 0;
        dValue = Double.NaN;
        sValue = value;
    }

    /**
     * Returns the node at the specified index from the array of child nodes. 
     * If the index is outside the range of the array then an ArrayIndexOutOfBounds 
     * exception is thrown.
     * 
     * @param index the index of the child node to return.
     * @return the ith node in the array of children.
     * @throws ArrayIndexOutOfBoundsException if (index < 0 || index >= length).
     */
    public ASNode get(int index)
    {
        if (children == null || index < 0 || index >= children.length)
            throw new ArrayIndexOutOfBoundsException(index);
        
        return children[index];
    }

    /**
     * Replaces the node at position i in the array of children. If the position 
     * is outside the range of the array (i< 0 || i >= length) then an
     * ArrayIndexOutOfBoundsException is thrown.
     *  
     * @param i the index of the child node to replace.
     * @param aNode the node to replace the ith node.
     * @throws ArrayIndexOutOfBoundsException if (index < 0 || index >= length).
     */
    public void set(int i, ASNode aNode) 
    {
        if (aNode != null && children != null)
        {
            if (i < 0 || i >= children.length)
                throw new ArrayIndexOutOfBoundsException(i);
        
            aNode.parent = this;
            children[i] = aNode;
        }
    }
    
    /**
     * Adds a node to the array of children. If the node is null then it is ignored.
     * 
     * @param aNode the node to be added.
     */
    public void add(ASNode aNode) 
    {
        if (aNode != null)
        {
            aNode.parent = this;
            
            if (children == null) 
            {
                children = new ASNode[1];
            } 
            else 
            {
                ASNode c[] = new ASNode[children.length + 1];
                System.arraycopy(children, 0, c, 0, children.length);
                children = c;
            }
            children[children.length-1] = aNode;
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
     * @param index the index of the child node to replace.
     * @param aNode the node to replace the ith node.
     * @throws ArrayIndexOutOfBoundsException if (index < 0 || index >= length).
     */
    public void insert(int index, ASNode aNode) 
    {
        if (children == null || index < 0 || index >= children.length)
            throw new ArrayIndexOutOfBoundsException(index);
        
        aNode.parent = this;
        
        ASNode c[] = new ASNode[children.length+1];

        for (int i=0; i<index; i++)
            c[i] = children[i];
                
        c[index] = aNode;

        for (int i=index; i<children.length; i++)
            c[i+1] = children[i];
            
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
     * @param index the index of the child node to remove.
     * @throws ArrayIndexOutOfBoundsException if (index < 0 || index >= length).
     */
    public void remove(int index) 
    {
        if (children == null || index < 0 || index >= children.length)
            throw new ArrayIndexOutOfBoundsException(index);
        
        children[index].parent = null;
        children[index] = null;
        
        ASNode c[] = new ASNode[children.length-1];

        for (int i=0, j=0; i<children.length; i++)
        {
            if (children[i] != null)
                c[j++] = children[i];
        }
        children = c;
    }

    /**
      * Returns the index position of a node in the array of child nodes.
      * If the node is not one of the current nodes children then -1 is 
      * returned.
      * 
      * @param aNode the node to search the array of children for.
      * 
      * @return the index of the node in the array of children, -1 if the 
      * node is not a child of this node.
      */
     public int indexOf(ASNode aNode)
     {
         int index = -1;
    
         for (int i=0; i<children.length; i++) 
            if (children[i].equals(aNode))
                index = i;
   
         return index;
     }
    
    /**
     * Gets the parent node of this one. If no parent is define then null is returned.
     *
     * @return the parent node of this one.
     */
    public ASNode getParent()
    {
        return parent;
    }
    
    /** 
     * Return the number of child nodes contained by this node.
     * 
     * @return the number of child nodes.
     */
    public int count() 
    {
        return (children == null) ? 0 : children.length;
    }
    
    /**
     * Returns a string containing the type of node, any associated 
     * value and the number of children.
     *
     * @return the string representation of the node.
     */
    public String toString()
    {
        String str = nodeNames[type];
        
        if (type == BooleanLiteral)
        {
            str = str + " = " + (bValue ? "true" : "false") + "; ";  
        }
        else if (type == IntegerLiteral)
        {
            str = str + " = " + iValue + "; ";  
        }
        else if (type == DoubleLiteral)
        {
            str = str + " = " + dValue + "; ";  
        }
        else if (type == StringLiteral)
        {
            str = str + " = \"" + sValue + "\"; ";
        }
        else if (type == NullLiteral)
        {
            str = str + " = null; ";  
        }
        else if (sValue != null)
        {
            str = str + " = " + sValue + "; ";
        }
        return str;
    }
    
    /**
     * displayTree is used to display the structure of the node tree, with the 
     * root starting at the current node. The prefix argument is used to indent 
     * the text displayed. The level of indent is increased by appending the 
     * string "  " before calling the displayTree method on each child node. This 
     * illustrates the tree structure with nodes at the same level in the tree 
     * displayed with the same level of indent.
     * 
     * @param prefix the string prepended to the text representation for this 
     * node.
     */
    public void displayTree(String prefix)
    { 
        int count = count();
        
        System.out.println(prefix + toString());

        for (int i=0; i<count; i++)
            children[i].displayTree(prefix + "  ");
    }
    
    /*
     * Translates the array of nodes into the actions that will be executed by
     * the Flash Player.
     *
     * The version of Flash for which the actions are generated is specified to 
     * ensure compatibility with future release of Flash.
     *
     * IMPORTANT: The programming model changed with Flash version 5 to support
     * stack-based actions. Earlier versions of Flash are not support. An
     * IllegalArgumentException will be thrown if the version is earlier than 5.
     *
     * @param version the version of Flash that control the actions that are 
     * generated.
     *
     * @param encoding the character set used to represent the strings parsed
     * in the script.
     *
     * @throws IllegalArgumentException is the version is less than 5.
     */
    private ArrayList translate(int version, String encoding)
    {    
        int count = 0;
        
        if (version < 5)
            throw new IllegalArgumentException();

        ASInfo info = new ASInfo(version, encoding);
        ArrayList array = new ArrayList();
        
        reorder(info);
        findStrings(info);
        generate(info, array);

        return array;
    }
    
    /**
     * The encode method 'compiles' the node and all child nodes into an array 
     * of action objects which represents the sequence of actions performed by the 
     * Flash Player. The actions are then encoded to generate the binary data that
     * can be added to an encoded Flash file.
     *
     * The version of Flash for which the actions are generated is specified to 
     * ensure compatibility with future release of Flash.
     *
     * IMPORTANT: The programming model changed with Flash version 5 to support
     * stack-based actions. Earlier versions of Flash are not support. An
     * IllegalArgumentException will be thrown if the version is earlier than 5.
     * 
     * Identifiers and string literals are assumed to have an encoding of UTF-8.
     *
     * @param version the version of Flash that control the actions that are 
     * generated.
     *
     * @throws IllegalArgumentException is the version is less than 5.
     * @return an array of bytes containing encoded action objects.  
     */ 
    public byte[] encode(int version)
    {
        int length = 0;
        
        if (version < 5)
            throw new IllegalArgumentException();

        ArrayList array = translate(version, "UTF-8");
        
        for (Iterator i = array.iterator(); i.hasNext();) 
            length += ((Coding)i.next()).length(version, "UTF-8");        

        Coder coder = new Coder(new byte[length], "UTF-8");
                    
        for (Iterator i = array.iterator(); i.hasNext();) 
            ((Coding)i.next()).encode(coder);        

        return coder.data;
    }
    
    /*
     * reorder is used to restructure the node tree and individual nodes
     * to simplify the generation of the action objects that represent
     * the 'compiled' version of an ActionScript program.
     */
    private void reorder(ASInfo info)
    {
        switch (type)
        {
            case Array:
            case Button:
            case MovieClip:
                info.nodes.push(this);
                break;
            case Value:
                if (children[0].type == Identifier && children[1].type == Attribute)
                {
                    String name = children[0].sValue + "." + children[1].sValue;
                    
                    if (constants.containsKey(name))
                    {
                        type = Identifier;
                        sValue = name;
    
                        remove(0);
                        remove(0);
                    }
                }
                break;
            case Assign:
                if (parent != null && parent.type == List && parent.count() > 0)
                {
                    if (parent.children[0].count() > 0)
                    {
                        if (parent.children[0].children[0].type == DefineVariable)
                        {
                            if (parent.indexOf(this) != 0)                            
                                children[0].type = DefineVariable;  
                        }
                    }
                }
                break;
            case DefineFunction:
                ASNode node = (ASNode) info.nodes.peek();
                
                node.insert(node.insertIndex++, this);
                    
                int index = parent.indexOf(this);
                
                if (index != -1)
                    parent.remove(index);
                    
                info.nodes.push(this);
                break;
            case Function:
                if (sValue.equals("fscommand"))
                {
                    if (children[0].type == StringLiteral)
                        children[0].sValue = "FSCommand:" + children[0].sValue;
                }
                else if (sValue.equals("print"))
                {
                    ASNode c0 = children[0];
                    ASNode c1 = children[1];
                    
                    if (children[1].sValue.equals("bmovie"))
                        children[1].sValue = "print:";
                    else
                        children[1].sValue = "print:#" + children[1].sValue;
                    
                    children[0] = c1;
                    children[1] = c0;
                }
                else if (sValue.equals("printAsBitmap"))
                {
                    ASNode c0 = children[0];
                    ASNode c1 = children[1];
                    
                    if (children[1].sValue.equals("bmovie"))
                        children[1].sValue = "printasbitmap:";
                    else
                        children[1].sValue = "printasbitmap:#" + children[1].sValue;

                    children[0] = c1;
                    children[1] = c0;
                }
                break;
        }
        
        /*
         * reorder any child nodes before reordering any binary operators to 
         * ensure any interger literals are evaluated first. 
         */
        int count = count();
        
        for (int i=0; i<count; i++)
            children[i].reorder(info);
            
        switch (type)
        {
            case Array:
            case Add:
            case Sub:
            case Mul:
            case Div:
            case Mod:
                if (count() == 2)
                {
                    if (children[0].getType() == IntegerLiteral && children[1].getType() == IntegerLiteral)
                    {
                        switch (type)
                        {
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
                                if (children[0].iValue / children[1].iValue == 0)
                                {
                                    type = DoubleLiteral;
                                    dValue = ((double)children[0].iValue) / ((double)children[1].iValue);
                                }
                                else if (children[0].iValue % children[1].iValue != 0)
                                {
                                    type = DoubleLiteral;
                                    dValue = ((double)children[0].iValue) / ((double)children[1].iValue);
                                }
                                else 
                                {
                                    type = IntegerLiteral;
                                    iValue = children[0].iValue/ children[1].iValue;
                                }
                                break;
                            case Mod: 
                                type = IntegerLiteral;
                                iValue = children[0].iValue % children[1].iValue; 
                                break;
                        }
                        remove(0);
                        remove(0);
                    }
                    else if (children[0].getType() == DoubleLiteral && children[1].getType() == IntegerLiteral)
                    {
                        switch (type)
                        {
                            case Add: dValue = children[0].dValue + children[1].iValue; break;
                            case Sub: dValue = children[0].dValue - children[1].iValue; break;
                            case Mul: dValue = children[0].dValue * children[1].iValue; break;
                            case Div: dValue = children[0].dValue / children[1].iValue; break;
                            case Mod: dValue = children[0].dValue % children[1].iValue; break;
                        }
                        type = DoubleLiteral;
                        remove(0);
                        remove(0);
                    }
                    else if (children[0].getType() == IntegerLiteral && children[1].getType() == DoubleLiteral)
                    {
                        switch (type)
                        {
                            case Add: dValue = children[0].iValue + children[1].dValue; break;
                            case Sub: dValue = children[0].iValue - children[1].dValue; break;
                            case Mul: dValue = children[0].iValue * children[1].dValue; break;
                            case Div: dValue = children[0].iValue / children[1].dValue; break;
                            case Mod: dValue = children[0].iValue % children[1].dValue; break;
                        }
                        type = DoubleLiteral;
                        remove(0);
                        remove(0);
                    }
                    else if (children[0].getType() == DoubleLiteral && children[1].getType() == DoubleLiteral)
                    {
                        switch (type)
                        {
                            case Add: dValue = children[0].dValue + children[1].dValue; break;
                            case Sub: dValue = children[0].dValue - children[1].dValue; break;
                            case Mul: dValue = children[0].dValue * children[1].dValue; break;
                            case Div: dValue = children[0].dValue / children[1].dValue; break;
                            case Mod: dValue = children[0].dValue % children[1].dValue; break;
                        }
                        type = DoubleLiteral;
                        remove(0);
                        remove(0);
                    }
                }
                break;
            case ASR:
            case LSL:
            case LSR:
            case BitAnd:
            case BitOr:
            case BitXOr:
                if (count() == 2)
                {
                    if (children[0].getType() == IntegerLiteral && children[1].getType() == IntegerLiteral)
                    {
                        switch (type)
                        {
                            case ASR: iValue = children[0].iValue >> children[1].iValue; break;
                            case LSL: iValue = children[0].iValue << children[1].iValue; break;
                            case LSR: iValue = children[0].iValue >>> children[1].iValue; break;
                            case BitAnd: iValue = children[0].iValue & children[1].iValue; break;
                            case BitOr: iValue = children[0].iValue | children[1].iValue; break;
                            case BitXOr: iValue = children[0].iValue ^ children[1].iValue; break;
                        }
                        type = IntegerLiteral;
                        remove(0);
                        remove(0);
                   }
                }
                break;

            case And:
            case Or:
                if (count() == 2)
                {
                    if (children[0].getType() == BooleanLiteral && children[1].getType() == BooleanLiteral)
                    {
                        switch (type)
                        {
                            case And:
                                type = BooleanLiteral;
                                bValue = children[0].bValue && children[1].bValue; 
                                break;
                            case Or:
                                type = BooleanLiteral;
                                bValue = children[0].bValue || children[1].bValue; 
                                break;
                        }
                        remove(0);
                        remove(0);
                    }
                    else if (children[0].getType() == BooleanLiteral && children[1].getType() == IntegerLiteral)
                    {
                        switch (type)
                        {
                            case And:
                                type = BooleanLiteral;
                                bValue = children[0].bValue && (children[1].iValue != 0); 
                                break;
                            case Or:
                                type = IntegerLiteral;
                                iValue = children[1].iValue; 
                                break;
                        }
                        remove(0);
                        remove(0);
                    }
                    else if (children[0].getType() == IntegerLiteral && children[1].getType() == BooleanLiteral)
                    {
                        switch (type)
                        {
                            case And:
                                type = BooleanLiteral;
                                bValue = (children[0].iValue != 0) && children[1].bValue; 
                                break;
                            case Or:
                                type = IntegerLiteral;
                                iValue = ((children[0].iValue != 0) || children[1].bValue) ? 1 : 0; 
                                break;
                        }
                        remove(0);
                        remove(0);
                    }
                    else if (children[0].getType() == IntegerLiteral && children[1].getType() == IntegerLiteral)
                    {
                        boolean a = children[0].iValue != 0;
                        boolean b = children[1].iValue != 0;
                        
                        switch (type)
                        {
                            case And:
                                type = IntegerLiteral;
                                iValue = a ? children[1].iValue : 0; 
                                break;
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
                if (count() == 1)
                {
                    if (children[0].getType() == BooleanLiteral)
                    {
                        type = BooleanLiteral;
                        bValue = !children[0].bValue;
                        remove(0);
                    }
                    else if (children[0].getType() == IntegerLiteral)
                    {
                        type = BooleanLiteral;
                        bValue = children[0].iValue == 0;
                        remove(0);
                    }
                }
                break;
            case BitNot:
                if (count() == 1)
                {
                    if (children[0].getType() == IntegerLiteral)
                    {
                        type = IntegerLiteral;
                        iValue = ~children[0].iValue;
                        remove(0);
                    }
                }
                break;
            default:
                break;
        }
    
        switch (type)
        {
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
    * 
    * @param info is an ASInfo object that passes context information between
    * nodes.
    */
    private void findStrings(ASInfo info)
    {
        int count = count();
        
        if (type == Function)
            info.context.push(sValue);
        else
            info.context.push(nodeNames[type]);

        switch (type)
        {
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
                else if (propertyNames.containsKey(sValue))
                {
                    if (info.context.contains("getProperty"))
                        break;
                    else if (info.context.contains("setProperty"))
                        break;
                    else if (info.context.contains("With"))
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

                for (int i=count-1; i>=0; i--)
                    children[i].findStrings(info);

                if (sValue.length() > 0)
                    info.addString(sValue);
                break;
            case Function:
                 if (sValue != null && functions.containsKey(sValue) == false)
                 {
                    for (int i=0; i<count; i++)
                        children[i].findStrings(info);

                    if (sValue.length() > 0)
                        info.addString(sValue);
                 }
                 else
                 {
                     if (sValue != null && sValue.equals("fscommand"))
                     {
                        info.addString("FSCommand:");

                        for (int i=0; i<count; i++)
                            children[i].findStrings(info);
                     }
                     else if (sValue != null && sValue.equals("getURL"))
                     {
                         if (count > 0)
                             children[0].findStrings(info);

                         if (count > 1)
                             children[1].findStrings(info);
                            
                         if (count == 1 && children[0].type != StringLiteral)
                             info.addString("");
                            
                         break;
                     }
                     else if (sValue != null && sValue.equals("gotoAndPlay"))
                     {
                         children[1].findStrings(info);
                         break;
                     }
                     else if (sValue != null && sValue.equals("gotoAndStop"))
                     {
                         children[1].findStrings(info);
                         break;
                    }
                    else if (sValue != null && sValue.equals("loadMovie"))
                    {
                        if (count > 0)
                            children[0].findStrings(info);

                        if (count > 1)
                            children[1].findStrings(info);
                            
                        if (count == 1)
                            info.addString("");
                            
                        break;
                    }
                    else if (sValue != null && sValue.equals("loadVariables"))
                    {
                        if (count > 0)
                            children[0].findStrings(info);

                        if (count > 1)
                            children[1].findStrings(info);
                            
                        if (count == 1)
                            info.addString("");
                            
                        break;
                    }
                    else
                    {
                        for (int i=0; i<count; i++)
                            children[i].findStrings(info);
                    }
                 }
                break;
            case DefineFunction:
                children[count-1].findStrings(info);
                break;
            case DefineArray:
                for (int i=count-1; i>=0; i--)
                    children[i].findStrings(info);
                break;
            case Value:
                if (count > 0)
                {
                    if (children[0].sValue != null && classes.containsKey(children[0].sValue))
                    {
                        boolean containsClass = false;
                        
                        for (Iterator i=info.strings.iterator(); i.hasNext();)
                        {
                            if (i.next().toString().equals(children[0].sValue))
                            {
                                containsClass = true;
                                break;
                            }
                        }
                        // Swap the name of the function and the class to
                        // simplify verification during testing.
                        
                        if (containsClass == false)
                        {
                            int index = info.strings.size();
                            
                            for (int i=0; i<count; i++)
                                children[i].findStrings(info);
                            
                            info.strings.set(index, info.strings.get(index+1));
                            info.strings.set(index+1, children[0].sValue);
                        }
                        else
                        {
                            for (int i=0; i<count; i++)
                                children[i].findStrings(info);                            
                        }
                    }
                    else
                    {
                        for (int i=0; i<count; i++)
                            children[i].findStrings(info);
                    }
                }
                break;
            default:
                for (int i=0; i<count; i++)
                    children[i].findStrings(info);
                break;
         }
        info.context.pop();
    }
    
    /*
     * generate 'compiles' ActionScript statements that this node and all 
     * child nodes represent into the set of actions that will be executed by the 
     * Flash Player.
     *
     * @param info an ASInfo object that is used to pass context and context 
     * information between nodes. This should be the same object used when 
     * preprocessing modes.
     * 
     * @param actions an array that the compiled actions will be added to.
     */
    private void generate(ASInfo info, ArrayList actions)
    {
        if (type == Function)
            info.context.push(sValue);
        else
            info.context.push(nodeNames[type]);
        
           switch (type)
        {
            case Array:
            case Button:
            case MovieClip:
                generateScript(info, actions);
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
            case OnClipEvent:
                generateOnClipEvent(info, actions);
                break;
            case On:
                generateOn(info, actions);
                break;
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
                generateUnary(info, actions);
                break;
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
            case And:
            case Or:
            case InstanceOf:
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
    
    private void generateScript(ASInfo info, ArrayList actions)
    {
        ArrayList array = new ArrayList();
        int count = count();
                
           switch (type)
        {
            case Array:
                if (type == Array && info.useStrings)
                    array.add(new Table(info.strings));
            
                for (int i=0; i<count; i++)
                    children[i].discardValues();

                for (int i=0; i<count; i++)
                    children[i].generate(info, array);
                    
                actions.add(new ASArray(Coding.Frame, array));
                break;
            case Button:
                for (int i=0; i<count; i++)
                    children[i].generate(info, array);
                                
                actions.add(new ASArray(Coding.Button, array));
                break;
            case MovieClip:
                for (int i=0; i<count; i++)
                    children[i].generate(info, array);
                                
                actions.add(new ASArray(Coding.MovieClip, array));
                break;
            default:
                break;
        }
    }
    
    private void generateList(ASInfo info, ArrayList actions)
    {
        int count = count();

        for (int i=0; i<count; i++)
            children[i].generate(info, actions);
    }
    
    private void generateIf(ASInfo info, ArrayList actions)
    {
        int count = count();
        boolean addJump = false;

        ArrayList trueActions = new ArrayList();
        int offsetToNext = 0;

        ArrayList falseActions = new ArrayList();
        int offsetToEnd = 0;
        
        if (count > 1)
        {
            children[1].discardValues();
            children[1].generate(info, trueActions);
            offsetToNext = actionLength(trueActions);                                
        }
            
        if (count == 3)
        {
            children[2].discardValues();
            children[2].generate(info, falseActions);
            offsetToEnd = actionLength(falseActions);
            
            addJump = offsetToEnd != 0;                    
        }
        
        if (trueActions.size() > 0)
        {
            int actionType = ((Action)trueActions.get(trueActions.size()-1)).type;
            
            if (actionType == 256 || actionType == 257)
            {    
                addJump = true;
                
                if (falseActions.size() == 0)
                    offsetToNext -= 5;
            }
        }
        
        if (addJump)
            offsetToNext += 5; // Length of jump tag
            
        children[0].generate(info, actions);

        addAction(actions, Action.Not);
        actions.add(new ValueAction(Action.If, offsetToNext));
        
        actions.addAll(trueActions);
        
        if (addJump == true && offsetToEnd > 0)
            actions.add(new ValueAction(Action.Jump, offsetToEnd));
            
        actions.addAll(falseActions);
    }
    
    private void generateDo(ASInfo info, ArrayList actions)
    {
        int count = (children != null) ? children.length : 0;

        ArrayList blockActions = new ArrayList();
        int blockLength = 0;

        ArrayList conditionActions = new ArrayList();
        int conditionLength = 0;
               
        children[0].discardValues();
        children[0].generate(info, blockActions);

        children[1].generate(info, conditionActions);
    
        blockLength = actionLength(blockActions);                                
        conditionLength = actionLength(conditionActions);                                
        
        conditionLength += 5; // include following if statement
        
        conditionActions.add(new ValueAction(Action.If, -(blockLength+conditionLength))); // includes if

        int currentLength = 0;
    
        // Replace any break and continue place holders with jump statements.
    
        for (int i=0; i<blockActions.size(); i++)
        {
            Action currentAction = (Action)blockActions.get(i);
            
            currentLength += currentAction.length(info.version, info.encoding);
       
            if (currentAction.type == 256)
                blockActions.set(i, new ValueAction(Action.Jump, blockLength-currentLength+conditionLength));

            if (currentAction.type == 257)
                blockActions.set(i, new ValueAction(Action.Jump, blockLength-currentLength));
        }
        actions.addAll(blockActions);
        actions.addAll(conditionActions);
    }

    private void generateWhile(ASInfo info, ArrayList actions)
    {
        int count = (children != null) ? children.length : 0;

        ArrayList blockActions = new ArrayList();
        int blockLength = 0;

        ArrayList conditionActions = new ArrayList();
        int conditionLength = 0;
              
        if (count == 2) 
        {
            children[1].discardValues();
            children[1].generate(info, blockActions);
        }
        blockLength = actionLength(blockActions);                                

        children[0].generate(info, conditionActions);
        addAction(conditionActions, Action.Not);
        conditionActions.add(new ValueAction(Action.If, blockLength+5)); // includes loop jump
        conditionLength = actionLength(conditionActions);                                
        
        blockActions.add(new ValueAction(Action.Jump, -(conditionLength+blockLength+5)));
        blockLength += 5;

        int currentLength = conditionLength;
    
        // Replace any break and continue place holders with jump statements.
    
        for (int i=0; i<blockActions.size(); i++)
        {
            Action currentAction = (Action)blockActions.get(i);
        
            currentLength += currentAction.length(info.version, info.encoding);

            if (currentAction.type == 256)
                blockActions.set(i, new ValueAction(Action.Jump, (blockLength+conditionLength)-currentLength));

            if (currentAction.type == 257)
                blockActions.set(i, new ValueAction(Action.Jump, -currentLength));
        }
        actions.addAll(conditionActions);
        actions.addAll(blockActions);
    }

    private void generateFor(ASInfo info, ArrayList actions)
    {
        int count = (children != null) ? children.length : 0;

        ArrayList initializeActions = new ArrayList();
        ArrayList conditionActions = new ArrayList();
        ArrayList iteratorActions = new ArrayList();
        ArrayList blockActions = new ArrayList();
    
        int initializeLength = 0;
        int conditionLength = 0;
        int blockLength = 0;
        int iteratorLength = 0;
        
        if (children[0].type != NoOp)
        {
            children[0].generate(info, initializeActions);
            initializeLength = actionLength(initializeActions);                                
        }
        if (children[1].type != NoOp)
        {
            children[1].generate(info, conditionActions);
            conditionLength = actionLength(conditionActions);                                
        }
        if (children[2].type != NoOp)
        {
            children[2].discardValues();
            children[2].generate(info, iteratorActions);
            iteratorLength = actionLength(iteratorActions);                                
        }            
        if (children[3].type != NoOp)
        {
            children[3].discardValues();
            children[3].generate(info, blockActions);
            blockLength = actionLength(blockActions);                                
        }
           
        // Add the if test with jump to end if false. Jump include block and 
        // iterator actions plus a jump at the end to go back to the condition actions

        if (conditionActions.size() > 0)
        {
            Action lastAction = (Action)conditionActions.get(conditionActions.size()-1);

            if (lastAction.type == Action.Push)
            {
                ArrayList values = ((Push)lastAction).getValues();
                int lastIndex = values.size()-1;
                Object lastValue = values.get(lastIndex);

                if (lastValue instanceof Boolean)
                {
                    if (((Boolean)lastValue).booleanValue())
                    {
                        values.set(lastIndex, new Boolean(false));
                        conditionActions.add(new ValueAction(Action.If, blockLength+iteratorLength+5));
                        conditionLength += 5;
                    }
                }
                else if (lastValue instanceof Integer)
                {
                    if (((Integer)lastValue).intValue() > 0)
                    {
                        values.set(lastIndex, new Integer(0));
                        conditionActions.add(new ValueAction(Action.If, blockLength+iteratorLength+5));
                        conditionLength += 5;
                    }
                }
                else if (lastValue instanceof Double)
                {
                    if (((Double)lastValue).doubleValue() > 0.0)
                    {
                        values.set(lastIndex, new Double(0));
                        conditionActions.add(new ValueAction(Action.If, blockLength+iteratorLength+5));
                        conditionLength += 5;
                    }
                }
                else if (lastValue instanceof String)
                {
                    if (((String)lastValue).equals("0") == false)
                    {
                        values.set(lastIndex, "0");
                        conditionActions.add(new ValueAction(Action.If, blockLength+iteratorLength+5));
                        conditionLength += 5;
                    }
                }
            }
            else
            {
                conditionActions.add(new Action(Action.Not));
                conditionActions.add(new ValueAction(Action.If, blockLength+iteratorLength+5));
                conditionLength += 6;
            }
        }

        // Add the jump to the start of the condition block
    
        iteratorLength += 5;
        iteratorActions.add(new ValueAction(Action.Jump, -(conditionLength+blockLength+iteratorLength)));
    
        // Replace any break and continue place holders with jump statements.
    
        int currentLength = conditionLength;
    
        for (int i=0; i<blockActions.size(); i++)
        {
            Action currentAction = (Action) blockActions.get(i);
        
            currentLength += currentAction.length(info.version, info.encoding);
        
            if (currentAction.type == 256)
                blockActions.set(i, new ValueAction(Action.Jump, (blockLength+conditionLength)-currentLength+iteratorLength));
            
            if (currentAction.type == 257)
                blockActions.set(i, new ValueAction(Action.Jump, (blockLength+conditionLength)-currentLength));
        }
    
        actions.addAll(initializeActions);
        actions.addAll(conditionActions);
        actions.addAll(blockActions);
        actions.addAll(iteratorActions);
    }

    private void generateForIn(ASInfo info, ArrayList actions)
    {
        int count = count();

        ArrayList conditionActions = new ArrayList();
        ArrayList blockActions = new ArrayList();
        
        int conditionLength = 0;
        int blockLength = 0;

        // Push all the attributes of the specified object onto the stack

        addReference(actions, info, children[1].sValue);
        actions.add(new Action(Action.Enumerate));

        // Set the enumerator variable with the current attribute

        addReference(blockActions, info, children[0].sValue);
        addLiteral(blockActions, new RegisterIndex(0));          
        blockActions.add(new Action(Action.SetVariable));
        
        // Translate the body of the for..in statement 
        
        if (count == 3)
        {
            children[2].discardValues();
            children[2].generate(info, blockActions);
        }
        
        // Calculate the length of the block in bytes
        
        blockLength = actionLength(blockActions);

        // Translate the clause of the for..in statement
        
        conditionActions.add(new ValueAction(Action.RegisterCopy, 0));
        addLiteral(conditionActions, new Null());
        addAction(conditionActions, Action.Equals);
        conditionActions.add(new ValueAction(Action.If, blockLength+5)); // includes loop jump
        
        // Calculate the length of the condition actions in bytes
        
        conditionLength = actionLength(conditionActions);
            
        // Add the jump to the start of the condition block
        
        blockActions.add(new ValueAction(Action.Jump, -(conditionLength+blockLength+5)));
        blockLength += 5;
        
        // Replace any break and continue place holders with jump statements.
        
        int currentLength = conditionLength;
        
        for (int i=0; i<blockActions.size(); i++)
        {
            Action currentAction = (Action)blockActions.get(i);
            
            currentLength += currentAction.length(info.version, info.encoding);
            
            if (currentAction.type == 256)
                blockActions.set(i, new ValueAction(Action.Jump, blockLength-currentLength));

            if (currentAction.type == 257)
                blockActions.set(i, new ValueAction(Action.Jump, -(conditionLength+currentLength)));
        }
        
        actions.addAll(conditionActions);
        actions.addAll(blockActions);
    }
    
    private void generateWith(ASInfo info, ArrayList actions)
    {
        ArrayList array = new ArrayList();
        int count = count();
        int length = 0;
        
        for (int i=1; i<count; i++)
            children[i].discardValues();

        for (int i=1; i<count; i++)
            children[i].generate(info, array);
            
        length = actionLength(array);
            
        children[0].generate(info, actions);

        actions.add(new ValueAction(Action.With, length));
        actions.addAll(array);
    }
    
    private void generateOnClipEvent(ASInfo info, ArrayList actions)
    {
        ArrayList array = new ArrayList();
        int count = count();
                        
        if (info.useStrings)
            array.add(new Table(info.strings));
            
        for (int i=0; i<count; i++)
            children[i].discardValues();

        for (int i=0; i<count; i++)
            children[i].generate(info, array);
                            
        actions.add(new ASEvent(Coding.MovieClip, iValue, array));
    }

    private void generateOn(ASInfo info, ArrayList actions)
    {
        ArrayList array = new ArrayList();
        int count = count();
                        
        if (info.useStrings)
            array.add(new Table(info.strings));
            
        for (int i=0; i<count; i++)
            children[i].discardValues();

        for (int i=0; i<count; i++)
            children[i].generate(info, array);
                            
        actions.add(new ASEvent(Coding.Button, iValue, array));
    }

    private void generateReturn(ASInfo info, ArrayList actions)
    {
        int count = count();

        switch (type) 
        {
            case Break:
                actions.add(new ValueAction(256, 0));
                break;
            case Continue:
                actions.add(new ValueAction(257, 0));
                break;
            case Return:
                if (count == 0)
                {
                    addLiteral(actions, new Void());
                }
                else
                {
                    for (int i=0; i<count; i++)
                        children[i].generate(info, actions);
                }
                addAction(actions, Action.Return);
                break;
            default:
                break;
        }
    }
    
    private void generateValue(ASInfo info, ArrayList actions)
    {
        int count = count();

        switch (type) 
        {
            case Value:
                /*
                 * If any of the children is a method call then generate the 
                 * actions for the method arguments. This ensures that the 
                 * arguments will be popped off the stack in the correct order.
                 */
                for (int i=count-1; i>0; i--)
                {
                    if (children[i].type == Function || children[i].type == Method)
                    {
                        ASNode[] grandChildren = children[i].children;
                    
                        if (grandChildren != null)
                        {
                            int numGrandChildren = grandChildren.length;
                    
                            for (int j=numGrandChildren-1; j>=0; j--)
                                grandChildren[j].generate(info, actions);
                                
                            addLiteral(actions, numGrandChildren);
                        }
                        else
                        {
                            addLiteral(actions, 0);
                        }
                    }
                }                 

                /*
                 * Now generate the actions for each node that returns
                 * a value. Note that below methods do not generate 
                 * actions for their children since the parent node is 
                 * always a Value. Functions only do so if the parent 
                 * node is not a Value.
                 */
                children[0].generate(info, actions);

                for (int i=1; i<count; i++)
                {
                    if (children[i].type == Function)
                    {
                        Action last = (Action)actions.get(actions.size()-1);
                        
                        if (last.type == Action.GetAttribute)
                        {
                            actions.remove(actions.size()-1);
                            addAction(actions, Action.ExecuteMethod);
                        }
                        else
                        {
                            addAction(actions, Action.ExecuteFunction);
                        }
                    }
                    else
                        children[i].generate(info, actions);
                }
                
                if (discardValue)
                    addAction(actions, Action.Pop);
                break;
            case BooleanLiteral:
                addLiteral(actions, new Boolean(bValue));
                
                if (discardValue)
                    addAction(actions, Action.Pop);
                break;
            case IntegerLiteral:
                addLiteral(actions, iValue);
                
                if (discardValue)
                    addAction(actions, Action.Pop);
                break;
            case DoubleLiteral:
                int val = (int)dValue;
                
                if (val == dValue)
                    addLiteral(actions, new Integer(val));
                else
                    addLiteral(actions, new Double(dValue));
                
                if (discardValue)
                    addAction(actions, Action.Pop);
                break;
            case StringLiteral:
                addReference(actions, info, sValue);
                
                if (discardValue)
                    addAction(actions, Action.Pop);
                break;
            case NullLiteral:
                addLiteral(actions, new Null());
                
                if (discardValue)
                    addAction(actions, Action.Pop);
                break;
            case Identifier:
                if (constants.containsKey(sValue))
                    addLiteral(actions, constants.get(sValue));
                else if (propertyNames.containsKey(sValue))
                {
                    int pVal = ((Integer)propertyNames.get(sValue)).intValue();
                    
                    if (info.context.contains("With"))
                    {
                        addReference(actions, info, sValue);
                        addAction(actions, Action.GetVariable);
                    }
                    else if (info.context.contains("setProperty"))
                    {
                        if (pVal >= 16 && pVal <= 21)
                            addLiteral(actions, new Integer(pVal));                
                        else                            
                            addLiteral(actions, new Property(((Integer)earlyPropertyNames.get(sValue)).intValue()));                
                    }
                    else
                    {
                        addReference(actions, info, "");
                        if (pVal >= 0 && pVal <= 21)
                            addLiteral(actions, new Integer(pVal));                
                        else                            
                            addLiteral(actions, new Property(pVal));                
                        addAction(actions, Action.GetProperty);
                    }
                }
                else
                {
                    addReference(actions, info, sValue);
                    addAction(actions, Action.GetVariable);                    
                }
                if (discardValue)
                    addAction(actions, Action.Pop);
                break;
            case Attribute:
                addReference(actions, info, sValue);
                addAction(actions, Action.GetAttribute);
                if (discardValue)
                    addAction(actions, Action.Pop);
                break;
            case Method:
                addReference(actions, info, sValue);
                addAction(actions, Action.ExecuteMethod);
                break;
            case NewObject:
                for (int i=count-1; i>=0; i--)
                    children[i].generate(info, actions);
                addLiteral(actions, count);
                addReference(actions, info, sValue);
                addAction(actions, Action.NamedObject);
                break;
            case Subscript:
                children[0].generate(info, actions);
                addAction(actions, Action.GetAttribute);
                break;
            default:
                break;
        }
    }
    
    private void generateDefinition(ASInfo info, ArrayList actions)
    {
        int count = count();
        int last = count-1;

        switch (type) 
        {
            case DefineArray:
                for (int i=last; i>=0; i--)
                    children[i].generate(info, actions);
                addLiteral(actions, count);
                addAction(actions, Action.NewArray);
                break;
            case DefineObject:
                for (int i=0; i<count; i++)
                    children[i].generate(info, actions);
                addLiteral(actions, count);
                addAction(actions, Action.NewObject);
                break;
            case DefineFunction:
                ArrayList functionArguments = new ArrayList();
                ArrayList functionActions = new ArrayList();
            
                if (count() == 2)
                {
                    if (children[0].type == List)
                    {
                        count = children[0].count();
                    
                        for (int i=0; i<count; i++)
                            functionArguments.add(children[0].children[i].sValue);
                    }
                    else
                    {
                        functionArguments.add(children[0].sValue);
                    }
                }
                children[last].discardValues();
                children[last].generate(info, functionActions);    
                            
                actions.add(new NewFunction(sValue, functionArguments, functionActions, info.version, info.encoding));
                break;
            case DefineMethod:
                ArrayList methodArguments = new ArrayList();
                ArrayList methodActions = new ArrayList();

                if (count() == 2)
                {
                    if (children[0].type == List)
                    {
                        count = children[0].count();
                    
                        for (int i=0; i<count; i++)
                            methodArguments.add(children[0].children[i].sValue);
                    }
                    else
                    {
                        methodArguments.add(children[0].sValue);
                    }
                }
                children[last].discardValues();
                children[last].generate(info, methodActions);
                                
                actions.add(new NewFunction("", methodArguments, methodActions, info.version, info.encoding));
                break;
            case DefineAttribute:
                children[0].generate(info, actions);    
                actions.remove(actions.size()-1);    
                children[1].generate(info, actions);                
                break;
            case DefineVariable:
                addReference(actions, info, sValue);
                addAction(actions, Action.InitVariable);
                break;
            default:
                break;
        }
    }
    
    private void generateUnary(ASInfo info, ArrayList actions)
    {
        int count = count();
        Action lastAction = null;

        switch (type) 
        {
            case PreInc:
                children[0].generate(info, actions);
                actions.remove(actions.size()-1);
                children[0].generate(info, actions);
                lastAction = (Action)actions.get(actions.size()-1);
                addAction(actions, Action.Increment);

                if (discardValue == false)
                    actions.add(new ValueAction(Action.RegisterCopy, 0));

                if (lastAction.type == Action.GetAttribute)
                    addAction(actions, Action.SetAttribute);
                else
                    addAction(actions, Action.SetVariable);
                
                if (discardValue == false)
                    addLiteral(actions, new RegisterIndex(0));          

                break;
            case PreDec:
                children[0].generate(info, actions);
                actions.remove(actions.size()-1);
                children[0].generate(info, actions);
                lastAction = (Action)actions.get(actions.size()-1);
                addAction(actions, Action.Decrement);

                if (discardValue == false)
                    actions.add(new ValueAction(Action.RegisterCopy, 0));

                if (lastAction.type == Action.GetAttribute)
                    addAction(actions, Action.SetAttribute);
                else
                    addAction(actions, Action.SetVariable);

                if (discardValue == false)
                    addLiteral(actions, new RegisterIndex(0));          

                break;
            case PostInc:
                if (discardValue == false)
                    children[0].generate(info, actions);

                children[0].generate(info, actions);
                actions.remove(actions.size()-1);
                children[0].generate(info, actions);
                lastAction = (Action)actions.get(actions.size()-1);
                addAction(actions, Action.Increment);
                if (lastAction.type == Action.GetAttribute)
                    addAction(actions, Action.SetAttribute);
                else
                    addAction(actions, Action.SetVariable);
                break;
            case PostDec:
                if (discardValue == false)
                    children[0].generate(info, actions);

                children[0].generate(info, actions);
                actions.remove(actions.size()-1);
                children[0].generate(info, actions);
                lastAction = (Action)actions.get(actions.size()-1);
                addAction(actions, Action.Decrement);
                if (lastAction.type == Action.GetAttribute)
                    addAction(actions, Action.SetAttribute);
                else
                    addAction(actions, Action.SetVariable);
                break;
            case Plus:
                if (children[0].type == BooleanLiteral)
                {
                    children[0].generate(info, actions);
                    addLiteral(actions, 0);
                    addAction(actions, Action.Add);
                }
                else if (children[0].type == IntegerLiteral)
                {
                    addLiteral(actions, children[0].iValue);
                }
                else if (children[0].type == StringLiteral)
                {
                    children[0].generate(info, actions);
                    addLiteral(actions, 0);
                    addAction(actions, Action.Add);
                }
                else if (children[0].type == NullLiteral)
                {
                    children[0].generate(info, actions);
                    addLiteral(actions, 0);
                    addAction(actions, Action.Add);
                }
                else
                {
                    children[0].generate(info, actions);
                }
                if (discardValue)
                    addAction(actions, Action.Pop);
                break;
            case Minus:
                if (children[0].type == BooleanLiteral)
                {
                    addLiteral(actions, 0);
                    children[0].generate(info, actions);
                    addAction(actions, Action.Subtract);
                }
                else if (children[0].type == IntegerLiteral)
                {
                    addLiteral(actions, -children[0].iValue);
                }
                else if (children[0].type == StringLiteral)
                {
                    addLiteral(actions, 0);
                    children[0].generate(info, actions);
                    addAction(actions, Action.Subtract);
                }
                else if (children[0].type == NullLiteral)
                {
                    addLiteral(actions, 0);
                    children[0].generate(info, actions);
                    addAction(actions, Action.Subtract);
                }
                else
                {
                    addLiteral(actions, 0);
                    children[0].generate(info, actions);
                    addAction(actions, Action.Subtract);
                }
                if (discardValue)
                    addAction(actions, Action.Pop);
                break;
            case BitNot:
                children[0].generate(info, actions);
                addLiteral(actions, new Double(Double.longBitsToDouble(0x41EFFFFFFFE00000L)));
                addAction(actions, Action.BitwiseXOr);

                if (discardValue)
                    addAction(actions, Action.Pop);
                break;
            case Not:
                children[0].generate(info, actions);
                addAction(actions, Action.Not);

                if (discardValue)
                    addAction(actions, Action.Pop);
                break;
            case Delete:
                children[0].generate(info, actions);
                actions.remove(actions.size()-1);
                addAction(actions, Action.Delete);

                if (discardValue)
                    addAction(actions, Action.Pop);
                break;
            default:
                break;
        }
    }
    
    private void generateBinary(ASInfo info, ArrayList actions)
    {
        ArrayList array = new ArrayList();
        
        int count = count();
        int offset = 0;
                    
        /*
         * For most node types we want to generate the actions for 
         * the child nodes (if any) before adding the actions for 
         * node type.
         */
        
        switch (type) 
        {
            // > and <= are synthesised using < and !, see below.
            case GreaterThan:
            case LessThanEqual:
                for (int i=count-1; i>=0; i--)
                    children[i].generate(info, actions);
                break;
            // Code Logical And/Or generated using if actions, see below.
            case And: 
            case Or:
                break;
            default:
                for (int i=0; i<count; i++)
                    children[i].generate(info, actions);
                break;
        }

        switch (type) 
        {
            case Add:
                addAction(actions, Action.Add);
                break;
            case Sub:
                addAction(actions, Action.Subtract);
                break;
            case Mul:
                addAction(actions, Action.Multiply);
                break;
            case Div:
                addAction(actions, Action.Divide);
                break;
            case Mod:
                addAction(actions, Action.Modulo);
                break;
            case BitAnd:
                addAction(actions, Action.BitwiseAnd);
                break;
            case BitOr:
                addAction(actions, Action.BitwiseOr);
                break;
            case BitXOr:
                addAction(actions, Action.BitwiseXOr);
                break;
            case LSL:
                addAction(actions, Action.LogicalShiftLeft);
                break;
            case LSR:
                addAction(actions, Action.LogicalShiftRight);
                break;
            case ASR:
                addAction(actions, Action.ArithmeticShiftRight);
                break;
            case Equal:
                addAction(actions, Action.Equals);
                break;
            case NotEqual:
                addAction(actions, Action.Equals);
                addAction(actions, Action.Not);
                break;
            case LessThan:
                addAction(actions, Action.Less);
                break;
            case GreaterThan:
                addAction(actions, Action.Less);
                break;
            case LessThanEqual:
                addAction(actions, Action.Less);
                addAction(actions, Action.Not);
                break;
            case GreaterThanEqual:
                addAction(actions, Action.Less);
                addAction(actions, Action.Not);
                break;
            case And:
                addAction(array, Action.Pop);
                    
                children[1].generate(info, array);
                offset = actionLength(array);
        
                children[0].generate(info, actions);

                addAction(actions, Action.Duplicate);                    
                addAction(actions, Action.Not);
                        
                actions.add(new ValueAction(Action.If, offset));                
                actions.addAll(array);
                break;
            case Or:
                addAction(array, Action.Pop);
                
                children[1].generate(info, array);
                offset = actionLength(array);
    
                children[0].generate(info, actions);
                addAction(actions, Action.Duplicate);
                
                actions.add(new ValueAction(Action.If, offset));                
                actions.addAll(array);
                break;
            case InstanceOf:
                addAction(actions, Action.InstanceOf);
                break;
            default:
                break;
        }
        if (discardValue)
            addAction(actions, Action.Pop);
    }
    
    private void generateSelect(ASInfo info, ArrayList actions)
    {
        ArrayList trueActions = new ArrayList();
        int offsetToNext = 0;

        ArrayList falseActions = new ArrayList();
        int offsetToEnd = 0;
        
        children[2].generate(info, falseActions);
        
        offsetToNext = actionLength(falseActions);
        offsetToNext += 5; // Length of jump tag
            
        children[1].generate(info, trueActions);
        
        offsetToEnd = actionLength(trueActions);

        children[0].generate(info, actions);

        actions.add(new ValueAction(Action.If, offsetToNext));
        actions.addAll(falseActions);
        
        actions.add(new ValueAction(Action.Jump, offsetToEnd));
            
        actions.addAll(trueActions);

        if (discardValue)
            addAction(actions, Action.Pop);
    }
    
    private void generateAssignment(ASInfo info, ArrayList actions)
    {
        children[0].generate(info, actions);
        
        Action lastAction = (Action) actions.get(actions.size()-1);
        
        if (lastAction.type == Action.GetVariable)
            actions.remove(actions.size()-1);
        else if (lastAction.type == Action.GetAttribute)
            actions.remove(actions.size()-1);
        else if (lastAction.type == Action.GetProperty)
            actions.remove(actions.size()-1);
        else if (lastAction.type == Action.InitVariable)
            actions.remove(actions.size()-1);

        if (type != Assign)
            children[0].generate(info, actions);

        children[1].generate(info, actions);

        switch (type)
        {
            case AssignAdd:
                addAction(actions, Action.Add);
                break;
            case AssignSub:
                addAction(actions, Action.Subtract);
                break;
            case AssignMul:
                addAction(actions, Action.Multiply);
                break;
            case AssignDiv:
                addAction(actions, Action.Divide);
                break;
            case AssignMod:
                addAction(actions, Action.Modulo);
                break;
            case AssignBitAnd:
                addAction(actions, Action.BitwiseAnd);
                break;
            case AssignBitOr:
                addAction(actions, Action.BitwiseOr);
                break;
            case AssignBitXOr:
                addAction(actions, Action.BitwiseXOr);
                break;
            case AssignLSL:
                addAction(actions, Action.LogicalShiftLeft);
                break;
            case AssignLSR:
                addAction(actions, Action.LogicalShiftRight);
                break;
            case AssignASR:
                addAction(actions, Action.ArithmeticShiftRight);
                break;
            default:
                break;
        }
        
        if (type == Assign && parent != null && (parent.type == List || parent.type == Assign))
        {
            if (children[0].type != DefineVariable)
            {
                actions.add(new ValueAction(Action.RegisterCopy, 0));
            }
        }

        if (lastAction.type == Action.GetProperty)
            addAction(actions, Action.SetProperty);
        else if (lastAction.type == Action.GetAttribute)
            addAction(actions, Action.SetAttribute);
        else if (lastAction.type == Action.GetVariable)
            addAction(actions, Action.SetVariable);
        else if (lastAction.type == Action.InitVariable)
            addAction(actions, Action.InitVariable);
        

        if (type == Assign && parent != null && (parent.type == List || parent.type == Assign))
        {
            if (children[0].type != DefineVariable)
            {
                addLiteral(actions, new RegisterIndex(0));
                
                if (parent.type== List)
                    addAction(actions, Action.Pop);
            }
        }

    }
    
    /*
     * generateFunction is used to add either a predefined action if the function 
     * call is to one of Flash's built-in functions. A separate method is used 
     * to make the code in the generate method more readable.
     */
    private void generateFunction(ArrayList actions, ASInfo info, Object value)
    {
        String name = (value == null) ? "" : (String) value;
        int count = count();
        
        if (functions.containsKey(name))
        {
            if (sValue.equals("delete"))
            {
                children[0].generate(info, actions);

                Action lastAction = (Action) actions.get(actions.size()-1);

                if (lastAction.type == Action.GetVariable)
                    actions.remove(actions.size()-1);

                addAction(actions, Action.Delete);
            }
            else if (sValue.equals("duplicateMovieClip"))
            {
                children[0].generate(info, actions);
                children[1].generate(info, actions);

                if (children[2].type == IntegerLiteral && children[2].sValue == null)
                {
                    int level = 16384;

                    level += children[2].iValue;

                    addLiteral(actions, level);
                }
                else
                {
                    addLiteral(actions, 16384);

                    children[2].generate(info, actions);

                    addAction(actions, Action.Add);
                }
                addAction(actions, Action.CloneSprite);
            }
            else if (sValue.equals("eval"))
            {
                children[0].generate(info, actions);
                addAction(actions, Action.GetVariable);
            }
            else if (sValue.equals("fscommand"))
            {
                boolean isCommandString = children[0].type == StringLiteral &&  children[0].sValue != null;
                boolean isArgumentString = children[1].type == StringLiteral &&  children[1].sValue != null;

                if (isCommandString && isArgumentString)
                {
                    String url = children[0].sValue;
                    String target = children[1].sValue;
                    
                    actions.add(new ValueAction(Action.GetUrl, url, target));
                }
                else
                {
                    if (isCommandString)
                    {
                        addReference(actions, info, children[0].sValue);
                    }
                    else
                    {
                        addReference(actions, info, "FSCommand:");
                        children[0].generate(info, actions);
                        addAction(actions, Action.StringAdd);
                    }
                    children[1].generate(info, actions);
    
                    actions.add(new ValueAction(Action.GetUrl2, Action.MovieToLevel));
                }
            }
            else if (sValue.equals("getProperty"))
            {
                String propertyName = children[1].sValue;
                int pVal = ((Integer)propertyNames.get(propertyName)).intValue();

                children[0].generate(info, actions);
                if (pVal >= 1 && pVal <= 21)
                    addLiteral(actions, new Integer(pVal));
                else if (pVal == 0)
                    addLiteral(actions, new Double(pVal));
                else                            
                    addLiteral(actions, new Property(pVal));                
                addAction(actions, Action.GetProperty);
            }
            else if (sValue.equals("getURL"))
            {
                switch(count)
                {
                    case 1:        
                        if (children[0].type == StringLiteral && children[0].sValue != null)
                        {
                            actions.add(new ValueAction(Action.GetUrl, children[0].sValue, ""));
                        }
                        else
                        {
                            children[0].generate(info, actions);
                            addReference(actions, info, "");
                            actions.add(new ValueAction(Action.GetUrl2, Action.MovieToLevel));
                        }
                        break;
                    case 2:
                        if (children[0].type == StringLiteral && children[0].sValue != null && children[1].type == StringLiteral && children[1].sValue != null)
                        {
                            actions.add(new ValueAction(Action.GetUrl, children[0].sValue, children[1].sValue));
                        }
                        else 
                        {
                            children[0].generate(info, actions);
                            children[1].generate(info, actions);
                            actions.add(new ValueAction(Action.GetUrl2, Action.MovieToLevel));
                        }
                        break;
                    case 3:
                        children[0].generate(info, actions);
                        children[1].generate(info, actions);
                        
                        if (children[2].sValue.toLowerCase().equals("get"))
                            actions.add(new ValueAction(Action.GetUrl2, Action.MovieToLevelWithGet));
                        else if (children[2].sValue.toLowerCase().equals("post"))
                            actions.add(new ValueAction(Action.GetUrl2, Action.MovieToLevelWithPost));
                        else
                            actions.add(new ValueAction(Action.GetUrl2, Action.MovieToLevel));
                        break;
                    default:
                        break;
                }
            }
            else if (sValue.equals("getVersion"))
            {
                addLiteral(actions, "/:$version");
                addAction(actions, Action.GetVariable);
            }
            else if (sValue.equals("gotoAndPlay"))
            {
                int index = count-1;
            
                if (children[index].sValue == null)
                {
                    int frameNumber = children[index].iValue - 1;
   
                    actions.add(new ValueAction(Action.GotoFrame, frameNumber));
                    addAction(actions, Action.Play);
                }
                else if (children[index].sValue.toLowerCase().startsWith("frame "))
                {
                    String frame = children[index].sValue.substring(6);
                    int frameNumber = 0;
                    
                    try
                    {
                        frameNumber = Integer.valueOf(frame).intValue()-1;
                    }
                    catch (NumberFormatException e)
                    {
                        
                    }
   
                    if (frameNumber == 1)
                    {
                        children[index].generate(info, actions);                        
                        actions.add(new ValueAction(Action.GotoFrame2, 1));
                    }
                    else
                    {
                        actions.add(new ValueAction(Action.GotoFrame, frameNumber));
                        addAction(actions, Action.Play);
                    }
                }
                else
                {
                    children[index].generate(info, actions);    
                    actions.add(new ValueAction(Action.GotoFrame2, 1));
                }
            }
            else if (sValue.equals("gotoAndStop"))
            {
                int index = count-1;
            
                if (children[index].sValue == null)
                {
                    int frameNumber = children[index].iValue - 1;
    
                    actions.add(new ValueAction(Action.GotoFrame, frameNumber));
                }
                else if (children[index].sValue.toLowerCase().startsWith("frame "))
                {
                    String frame = children[index].sValue.substring(6);
                    int frameNumber = 0;
                    
                    try
                    {
                        frameNumber = Integer.valueOf(frame).intValue()-1;
                    }
                    catch (NumberFormatException e)
                    {
                        
                    }
   
                    if (frameNumber == 1)
                    {
                        children[index].generate(info, actions);                        
                        actions.add(new ValueAction(Action.GotoFrame2, 0));
                    }
                    else
                    {
                        actions.add(new ValueAction(Action.GotoFrame, frameNumber));
                    }
                }
                else
                {
                    children[index].generate(info, actions);
    
                    actions.add(new ValueAction(Action.GotoFrame2, 0));
                }
            }
            else if (sValue.equals("hitTest"))
            {
                for (int i=count-1; i>=0; i--)
                    children[i].generate(info, actions);

                addLiteral(actions, count);
                addReference(actions, info, name);
                addAction(actions, Action.ExecuteFunction);
            }
            else if (sValue.equals("loadMovie"))
            {
                switch(count)
                {
                    case 2:
                        if (children[0].sValue != null && children[1].sValue == null)
                        {
                            String url = children[0].sValue;
                            String target = "_level" + children[1].iValue;
                            
                            actions.add(new ValueAction(Action.GetUrl, url, target));
                        }
                        else 
                        {
                            children[0].generate(info, actions);
                            children[1].generate(info, actions);
                            
                            actions.add(new ValueAction(Action.GetUrl2, Action.MovieToTarget));
                        }
                        break;
                    case 3:
                        children[0].generate(info, actions);
                        children[1].generate(info, actions);

                        if (children[2].sValue.toLowerCase().equals("get"))
                            actions.add(new ValueAction(Action.GetUrl2, Action.MovieToTargetWithGet));
                        else
                            actions.add(new ValueAction(Action.GetUrl2, Action.MovieToTargetWithPost));
                        break;
                    default:
                        break;
                }
            }
            else if (sValue.equals("loadVariables"))
            {
                switch(count)
                {
                    case 2:
                        children[0].generate(info, actions);
                        children[1].generate(info, actions);
                        
                        actions.add(new ValueAction(Action.GetUrl2, Action.VariablesToTarget));
                        break;
                    case 3:
                        children[0].generate(info, actions);
                        children[1].generate(info, actions);

                        if (children[2].sValue.toLowerCase().equals("get"))
                            actions.add(new ValueAction(Action.GetUrl2, Action.VariablesToTargetWithGet));
                        else
                            actions.add(new ValueAction(Action.GetUrl2, Action.VariablesToTargetWithPost));
                        break;
                    default:
                        break;
                }
            }
            else if (sValue.equals("nextFrame"))
            {
                addAction(actions, Action.NextFrame);
            }
            else if (sValue.equals("nextScene"))
            {
                actions.add(new ValueAction(Action.GotoFrame, 0));
            }
            else if (sValue.equals("Number"))
            {
                children[0].generate(info, actions);
                
                addAction(actions, Action.ToNumber);
            }
            else if (sValue.equals("play"))
            {
                addAction(actions, Action.Play);
            }
            else if (sValue.equals("prevFrame"))
            {
                addAction(actions, Action.PrevFrame);
            }
            else if (sValue.equals("prevScene"))
            {
                actions.add(new ValueAction(Action.GotoFrame, 0));
            }
            else if (sValue.equals("print"))
            {
                children[0].generate(info, actions);
                addReference(actions, info, children[1].sValue);
                addAction(actions, Action.GetVariable);
                actions.add(new ValueAction(Action.GetUrl2, Action.MovieToLevel));
            }
            else if (sValue.equals("printAsBitmap"))
            {
                children[0].generate(info, actions);
                addReference(actions, info, children[1].sValue);
                addAction(actions, Action.GetVariable);
                actions.add(new ValueAction(Action.GetUrl2, Action.MovieToLevel));
            }
            else if (sValue.equals("random"))
            {
                children[0].generate(info, actions);
                addAction(actions, Action.RandomNumber);
            }
            else if (sValue.equals("removeMovieClip"))
            {
                for (int i=0; i<count; i++)
                    children[i].generate(info, actions);

                addAction(actions, Action.RemoveSprite);
            }
            else if (sValue.equals("set"))
            {
                for (int i=0; i<count; i++)
                    children[i].generate(info, actions);

                addAction(actions, Action.SetVariable);
            }
            else if (sValue.equals("setProperty"))
            {
                for (int i=0; i<count; i++)
                    children[i].generate(info, actions);

                addAction(actions, Action.SetProperty);
            }
            else if (sValue.equals("startDrag"))
            {
                if (count > 2) {
                    children[2].generate(info, actions);
                    children[3].generate(info, actions);
                    children[4].generate(info, actions);
                    children[5].generate(info, actions);
                    addLiteral(actions, 1);
                    
                    if (children[1].getType() == BooleanLiteral)
                    {
                        addLiteral(actions, children[1].bValue ? 1 : 0);
                    }
                    else
                    {
                        children[1].generate(info, actions);
                    }
                }
                else if (count == 2) {
                    addLiteral(actions, 0);
                    
                    if (children[1].getType() == BooleanLiteral)
                    {
                        addLiteral(actions, children[1].bValue ? 1 : 0);
                    }
                    else
                    {
                        children[1].generate(info, actions);
                    }
                }
                else
                {
                    addLiteral(actions, 0);
                    addLiteral(actions, 0);
                }
                children[0].generate(info, actions);

                addAction(actions, Action.StartDrag);
            }
            else if (sValue.equals("stop"))
            {
                addAction(actions, Action.Stop);
            }
            else if (sValue.equals("stopAllSounds"))
            {
                addAction(actions, Action.StopSounds);
            }
            else if (sValue.equals("stopDrag"))
            {
                addAction(actions, Action.EndDrag);
            }
            else if (sValue.equals("String"))
            {
                children[0].generate(info, actions);
                
                addAction(actions, Action.ToString);
            }
            else if (sValue.equals("targetPath"))
            {
                for (int i=0; i<count; i++)
                    children[i].generate(info, actions);

                addAction(actions, Action.GetTarget);
            }
            else if (sValue.equals("toggleHighQuality"))
            {
                addAction(actions, Action.ToggleQuality);
            }
            else if (sValue.equals("trace"))
            {
                for (int i=0; i<count; i++)
                    children[i].generate(info, actions);

                addAction(actions, Action.Trace);
            }
            else if (sValue.equals("typeof"))
            {
                for (int i=0; i<count; i++)
                    children[i].generate(info, actions);

                addAction(actions, Action.GetType);
            }
            else if (sValue.equals("unloadMovie"))
            {
                if (children[0].sValue == null)
                {
                    actions.add(new ValueAction(Action.GetUrl, "", "_level" + children[0].iValue));
                }
                else
                {
                    addLiteral(actions, "");
                    children[0].generate(info, actions);
                    actions.add(new ValueAction(Action.GetUrl2, Action.MovieToTarget));
                }
            }
            else if (sValue.equals("void"))
            {
                for (int i=0; i<count; i++)
                    children[i].generate(info, actions);

                addAction(actions, Action.Pop);
                addLiteral(actions, new Void());
            }
            else 
            {
                for (int i=0; i<count; i++)
                    children[i].generate(info, actions);

                addReference(actions, info, name);
                addAction(actions, Action.ExecuteFunction);
            }
        }
        else
        {
            if (sValue.equals("parseInt"))
            {
                for (int i=count-1; i>=0; i--)
                    children[i].generate(info, actions);

                addLiteral(actions, count);
                addReference(actions, info, name);
                addAction(actions, Action.ExecuteFunction);
            }
            else
            {
                for (int i=0; i<count; i++)
                    children[i].generate(info, actions);

                addLiteral(actions, count);
                addReference(actions, info, name);
                addAction(actions, Action.ExecuteFunction);

                if (valueFunctions.containsKey(name) == false)
                {
                    if (discardValue)
                        addAction(actions, Action.Pop);
                }
            }
        }
            
        if (valueFunctions.containsKey(name))
        {
            if (discardValue)
                addAction(actions, Action.Pop);
        }
    }

    private void addReference(ArrayList actions, ASInfo info, Object literal)
    {
        if (info.useStrings && info.strings.contains(literal))
            literal = new TableIndex(info.strings.indexOf(literal));

        if (literal instanceof Integer)
        {
            int value = ((Integer)literal).intValue();
            
            if (value == 0)
                literal = new Double(0.0);
        }
        
        if (actions.size() > 0)
        {
            Action action = (Action) actions.get(actions.size()-1);
        
            if (action.type == Action.Push)
                ((Push)action).getValues().add(literal);
            else
                actions.add(new Push(literal));
        }
        else
        {
            actions.add(new Push(literal));
        }
    }

    private void addLiteral(ArrayList actions, int value)
    {
        Object number = null;
        
        if (value == 0)
            number = new Double(0.0);
        else
            number = new Integer(value);

        if (actions.size() > 0)
        {
            Action action = (Action) actions.get(actions.size()-1);
       
            if (action.type == Action.Push)
                ((Push)action).getValues().add(number);
            else
                actions.add(new Push(number));
        }
        else
        {
            actions.add(new Push(number));
        }
    }

    private void addLiteral(ArrayList actions, Object literal)
    {
        Action action = null;
        
        if (literal instanceof Integer)
        {
            int value = ((Integer)literal).intValue();
            
            if (value == 0)
                literal = new Double(0.0);
        }
        
        if (actions.size() > 0)
            action = (Action) actions.get(actions.size()-1);
        
        if (action != null && action.type == Action.Push)
            ((Push)action).getValues().add(literal);
        else
            actions.add(new Push(literal));
    }

    private void addAction(ArrayList actions, int type)
    {
        actions.add(new Action(type));
    }
    
    private int actionLength(ArrayList array)
    {
        int length = 0;
        
        for (Iterator i=array.iterator(); i.hasNext();)
        {
            Action action = (Action) i.next();
                
            length += action.length(0, "");
        }
        return length;
    }
    
    private void discardValues()
    {
        discardValue = true;
        
        if (type == List || type == StatementList)
        {
            int count = count();
            
            for (int i=0; i<count; i++)
                children[i].discardValues();
        }
    }
    
    /*
     * validate is used to provide additional error checking not covered in the parser
     * grammar.
     *
     */
    void validate() throws ParseException
    {
        boolean reportError = false;
        int count = count();
        ASNode node = this;
        
        switch (type)
        {
            case Button:
                /*
                 * Check scripts for button only contain on() statements.
                 */
                for (int i=0; i<count; i++)
                {
                    if (children[i].type != On)
                        reportError = true;
                }
                if (reportError)
                    reportError("OnOnly", number);
                break;
            case MovieClip:
                /*
                 * Check scripts for movie clips only contain onClipEvent() statements.
                 */
                for (int i=0; i<count; i++)
                {
                    if (children[i].getType() != OnClipEvent)
                        reportError = true;
                }
                if (reportError)
                    reportError("OnClipEventOnly", number);
                break;
            case Break:
                reportError = true;
                while (node != null)
                {
                    if (node.type == For || node.type == ForIn || node.type == Do || node.type == While)
                        reportError = false;
                        
                    node = node.parent;
                }
                if (reportError)
                    reportError("CannotUseBreak", number);
                break;
            case Continue:
                reportError = true;
                while (node != null)
                {
                    if (node.type == For || node.type == ForIn || node.type == Do || node.type == While)
                        reportError = false;
                        
                    node = node.parent;
                }
                if (reportError)
                    reportError("CannotUseContinue", number);
                break;
            case Return:
                reportError = true;
                while (node != null)
                {
                    if (node.type == DefineFunction || node.type == DefineMethod)
                        reportError = false;
                        
                    node = node.parent;
                }
                if (reportError)
                    reportError("CannotUseReturn", number);
                break;
            case Function:
                /*
                 * Check the number of arguments are supplied to built in Flash functions.
                 * Some addition checking of attributes is also carried out on a per function
                 * basis.
                 */
                String error = null;
                
                if (sValue.equals("delete"))
                {
                    if (count != 1)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("duplicateMovieClip"))
                {
                    if (count != 3)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("escape"))
                {
                    if (count != 1)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("eval"))
                {
                    if (count != 1)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("fscommand"))
                {
                    if (count != 2)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("getProperty"))
                {
                    if (count != 2)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("getURL"))
                {
                    if (count < 1 || count > 3)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("getVersion"))
                {
                    if (count != 0)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("gotoAndPlay"))
                {
                    if (count != 2)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("gotoAndStop"))
                {
                    if (count != 2)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("hitTest"))
                {
                    if (count < 1 || count > 3)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("isFinite"))
                {
                    if (count != 1)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("isNaN"))
                {
                    if (count != 1)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("loadMovie"))
                {
                    if (count < 1 || count > 3)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("loadVariables"))
                {
                    if (count < 1 || count > 3)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("nextFrame"))
                {
                    if (count != 0)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("nextScene"))
                {
                    if (count != 0)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("Number"))
                {
                    if (count != 1)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("parseInt"))
                {
                    if (count < 1 || count > 2)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("parseFloat"))
                {
                    if (count != 1)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("play"))
                {
                    if (count != 0)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("prevFrame"))
                {
                    if (count != 0)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("prevScene"))
                {
                    if (count != 0)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("print"))
                {
                    if (count != 2)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("printAsBitmap"))
                {
                    if (count != 2)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("removeMovieClip"))
                {
                    if (count != 1)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("set"))
                {
                    if (count != 2)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("setProperty"))
                {
                    if (count != 3)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("startDrag"))
                {
                    if ((count == 1 || count == 2 || count == 6) == false)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("stop"))
                {
                    if (count != 0)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("stopAllSounds"))
                {
                    if (count != 0)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("stopDrag"))
                {
                    if (count != 0)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("String"))
                {
                    if (count != 1)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("targetPath"))
                {
                    if (count != 1)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("toggleHighQuality"))
                {
                    if (count != 0)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("trace"))
                {
                    if (count != 1)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("typeof"))
                {
                    if (count != 1)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("unescape"))
                {
                    if (count != 1)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("unloadMovie"))
                {
                    if (count != 1)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("updateAfterEvent"))
                {
                    if (count != 1)
                        reportError("IncorrectArgumentCount", number);
                }
                else if (sValue.equals("void"))
                {
                    if (count != 1)
                        reportError("IncorrectArgumentCount", number);
                }
                break;
        }
        
        for (int i=0; i<count; i++)
            children[i].validate();
    }

    private void reportError(String errorKey, int number) throws ParseException
    {
        ParseException parseError = new ParseException(errorKey);
        
        parseError.currentToken = new Token();
        parseError.currentToken.beginLine = number;

        throw parseError;
    }
}

