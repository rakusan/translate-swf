/*
 * ASCompiler.java
 * Translate
 *
 * Copyright (c) 2010 Flagstone Software Ltd. All rights reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.flagstone.transform.coder.Context;

public final class ASCompiler {

    private final boolean DEBUG = false;

    /**
     * Keys that identify the different types of error generated while #include
     * directives are being processed and the script is being parsed. The key
     * can be used with a ResourceBundle object to generate a localized string
     * that describes the error in detail.
     */
    public static String[] ERRORS =  {
        /**
        * A FileNotFound error is reported when the file referenced in  a #include
        * directive cannot be opened.
        */
        "FileNotFound",
        /**
         * ReadError is reported when an error occurs while reading the file referenced
         * in a #include directive.
         */
           "ReadError",
        /**
         * A #include directive must be the only ActionScript present on a given line
         * (though the directive may be split over two lines). If other statements or
         * directives are present then a SingleDirective error is reported.
         */
        "SingleDirective",
        /**
         * A QuoteFileName error is reported if the filename refernced in a #include
         * directive is not enclosed in double quotes.
         */
        "QuoteFileName",
           /**
         * A ParseError error is used to report any exception thrown by the parser
         * while parsing a script.
         */
        "ParseError",
        /**
         * An OnOnly error occurs when on statements are mixed with other statements
         * in a script.
         */
        "OnOnly",
        /**
         * An OnClipEventOnly error occurs when onClipEvent statements are mixed with
         * other statements in a script.
         */
        "OnClipEventOnly",
        /**
         * An UnknownMovieClipEvent error occurs when an unknown movie clip event
         * name is found in an OnClipEvent() statement.
         */
        "UnknownMovieClipEvent",
        /**
         * An UnknownButtonEvent error occurs when an unknown button event name is
         * in an On() statement.
         */
        "UnknownButtonEvent",
        /**
         * A IncorrectArgumentCount error occurs when the wrong number of arguments are
         * supplied to one of the built-in functions in Flash.
         */
        "IncorrectArgumentCount",
        /**
         * A CannotUseBreak error occurs when a break statement is used outside of a
         * loop statement.
         */
           "CannotUseBreak",
        /**
         * A CannotUseContinue error occurs when a continue statement is used outside of a
         * loop statement.
         */
        "CannotUseContinue",
        /**
         * A CannotUseReturn error occurs when a return statement is used outside of a
         * function definition.
         */
        "CannotUseReturn",
    };

    private final static String separator = System.getProperty("line.separator");

    /*
     * The following attributes are used to report errors that occur while processing
     * directives or parsing a script.
     *
     * _filename contains the name of the file which contains the line of code that
     * triggered the error. Note if an error occurs because the file specified in the
     * #include directive cannot be found or an error occurs then _filename is the
     * name of the file containing the directive and not the name of the file being
     * included.
     *
     * _lineNumber is the line in the file referenced in _filename which contains the
     * code that triggered the error.
     *
     * _line is the line of code that triggered the error.
     *
     * _error is a keyword that identifies the type of error reported. The key may be
     * used with a ResourceBundle to generate a localized description, reporting the
     * error in full.
     */
    private String _filename = "";
    private int _lineNumber = 0;
    private String _line = "";
    private String _error = "";

    /*
     * The following arrays are used to map the line number reported when a
     * parseException is thrown (after all #include directives have been processed)
     * to the original file, line number and line of code. The arrays are updated
     * using the processDirectives method.
     */
    private final List<String> files = new ArrayList<String>();
    private final List<Integer> lines = new ArrayList<Integer>();
    private final List<String> codes = new ArrayList<String>();

    /** Version of actionscript to be compiled. */
    private int actionscriptVersion;
    /** Version of Flash to generate objects for. */
    private int flashVersion;
    /** List directories searched when looking for am included file. */
    private ArrayList<String> pathNames = new ArrayList<String>();

    /**
     * Constructs a new compiler object.
     */
    public ASCompiler() {
    }

    /**
     * Get the version of actionscript that the script will be in.
     * @return the version of actionscript used in the code to be compiled.
     */
    public int getActionscriptVersion() {
        return actionscriptVersion;
    }

    /**
     * Set the version of actionscript that the script will be in.
     * @param version the version of actionscript used in the code.
     */
    public void setActionscriptVersion(int version) {
        actionscriptVersion = version;
    }

    /**
     * Get the version of Flash that the compiled actions will be generated for.
     * @return the target version of Flash that the code will be compiled for.
     */
    public int getFlashVersion() {
        return flashVersion;
    }

    /**
     * Set the version of Flash that the compiled actions will be generated for.
     * @param version the target version of Flash that the code will be compiled
     * for.
     */
    public void setFlashVersion(int version) {
        flashVersion = version;
    }

    /**
     * Returns the array of path names used when searching for a file.
     *
     * @return an array of strings containing the names of directories to search.
     */
    public ArrayList<String> getPaths()
    {
        return pathNames;
    }

    /**
     * Sets the array of path names used when searching for a file.
     *
     * @param paths an array of strings containing the names of directories to search.
     */
    public void setPaths(ArrayList<String> paths)
    {
        pathNames = paths;
    }

    /**
     * Sets the array of path names used when searching for a file. The string
     * should contains paths which contain the system-dependent separator and
     * pathSeparator characters.
     *
     * @param paths
     *            a string containing the names of directories to search.
     */
    public void setPaths(String paths) {
        StringTokenizer pathTokenizer = new StringTokenizer(paths,
                File.pathSeparator, false);
        pathNames.clear();
        while (pathTokenizer.hasMoreTokens())
            pathNames.add(pathTokenizer.nextToken());
    }

    /**
     * Add a path to the array of pathnames. The path should contain the
     * system-dependent separator.
     *
     * @param path
     *            a string containing the path to a directory.
     */
    public void add(String path) {
        pathNames.add(path);
    }

    /**
     * Returns the name of the file that contained the line of code that
     * generated an error while parsing a script.
     *
     * @return the name of the file which contained the line of code or an empty
     *         string if the line was in the 'root' script.
     */
    public String getFilename() {
        return _filename;
    }

    /**
     * Returns the number of the the line of code that generated an error
     * parsing a script.
     *
     * @return the number of the line which that triggered the error.
     */
    public int getLineNumber() {
        return _lineNumber;
    }

    /**
     * Returns the line of code that generated an error while parsing a script.
     *
     * @return the line which that triggered the error.
     */
    public String getLine() {
        return _line;
    }

    /**
     * Returns the key identifying the type of error that occurred while
     * of parsing a script.
     *
     * @return the line which that triggered the error.
     */
    public String getError()
    {
        return _error;
    }

    /**
     * Parses the ActionScript string, script. Any nested files specified
     * using #include directives are loaded before the complete script is
     * parsed. The filenames and line numbers of #include'd scripts are
     * tracked so any syntax errors are reported accurately.
     *
     * The character used used in the script is assumed to be UTF-8.
     *
     * @param script a String containing the ActionScript code to parse.
     *
     * @throws ParseException if a parsing error occurs.
     */
    public List<Object> compile(String script) throws ParseException
    {
        List<Object> list = new ArrayList<Object>();
        ASParser parser = new ASParser();
        ASContext info = new ASContext();
        info.setEncoding("UTF-8");
        info.put(Context.VERSION, 5);
        ASNode root;

        files.clear();
        lines.clear();
        codes.clear();

        try
        {
            if (script != null && script.length() > 0)
            {
                StringBuffer buffer = new StringBuffer();

                processDirectives("", script, buffer);
                root = parser.parse(script);
                list = root.compile(info);
            }
        }
        catch (ParseException e)
        {
            /*
             * Check the correct error key was used.
             */
            if (DEBUG)
            {
                boolean foundKey = false;
                String errorKey = (e.tokenImage != null) ? "ParseError" : e.getMessage();

                for (int i=0; i<ERRORS.length; i++)
                {
                    if (errorKey.equals(ERRORS[i]))
                        foundKey = true;
                }
                if (foundKey == false)
                    System.err.println("Cannot find error key: " + errorKey);

                e.printStackTrace();
            }

            int errorLine = e.currentToken.beginLine;

            if (errorLine == 0)
                errorLine = 1;

            /*
             * If the exception was generated by the parser then the arrays
             * of tokens encountered and tokens expected will not be null,
             * allowing them to be differentiated from exceptions reported
             * using the reportError() method.
             */
            _error = (e.tokenImage != null) ? "ParseError" : e.getMessage();
            _filename = files.get(errorLine-1);
            _lineNumber = (lines.get(errorLine-1)).intValue();
            _line = codes.get(errorLine-1);

            throw e;
        }
        return list;
    }

    /**
     * Compiles the file containing ActionScript. Any nested files specified
     * using #include directives are loaded before the complete script is
     * parsed. The filenames and line numbers of #include'd scripts are
     * tracked so any syntax errors are reported accurately.
     *
     * The character used used in the script is assumed to be UTF-8.
     *
     * @param file a File containing the ActionScript statements to parse.
     *
     * @throws ParseException if a parsing error occurs.
     */
    public List<Object> compile(File file) throws ParseException
    {
        List<Object> list = new ArrayList<Object>();

        try
        {
            byte[] fileIn = new byte[(int)file.length()];

            FileInputStream fileContents = new FileInputStream(file);
            fileContents.read(fileIn);

            String script = new String(fileIn, "UTF-8");
            fileContents.close();

            list = compile(script);
        }
        catch (FileNotFoundException e)
        {
            _error = "FileNotFound";
            _filename = file.getPath();

            throw new ParseException("FileNotFound");
        }
        catch (IOException e)
        {
            _error = "ReadError";
            _filename = file.getPath();

            throw new ParseException("ReadError");
        }
        return list;
    }

    /*
     * processDirectives is used to resolve #include directives defined in a set of
     * ActionScript statements.
     *
     * If an error occurs when including files the name of the file, the line number
     * and the code which triggered the error is recorded. These may be retrieved
     * using the getFilename(), getLineNumber() and getLine() methods respectively.
     * Depending on the type of error either an IOException or ParseException is
     * thrown. The exception message contains a key that identifies the exact error
     * that occurs. The key may be used in conjunction with an instance of the
     * ResourceBundle class to generated a localized string describing the error.
     *
     * @param fileName is the name of file from which the script was loaded or an
     * empty string if the script was entered directly.
     *
     * @param script a string containing the ActionScript statements to be parsed.
     *
     * @param out a StringBuffer which will contain the 'flattened' scripts with all
     * #include directives replaced by the contents of the file they reference.
     *
     * @throws ParseException unless a line contains a single #include directive.
     *
     * @throws IOException if a #included file cannot be found or an error occurs when
     * including it.
     */
    private void processDirectives(String fileName, String script, StringBuffer out)
        throws ParseException
    {
        String[] statements = script.split("\u005c\u005cr?\u005c\u005cn|\u005c\u005cr\u005c\u005cn?");

        int currentLine = 1;
        int lineNumber = 0;

        /*
         * Boolean flags are used to signal when a directive has been found rather
         * immediately reading the following token to process the directive. This
         * allows a directive to be split over two lines (valid ActionScript) and
         * still be processed correctly.
         */
        boolean includeFile = false;

        for (int i=0; i<statements.length; i++)
        {
            String line = statements[i];

            if (line.indexOf("#include") != -1 || includeFile)
            {
                /*
                 * Split the line containing a directive into individual words
                 */
                String[] words = line.split("\u005c\u005cs");

                for (int j=0; j<words.length; j++)
                {
                    String token = words[j];

                    if (token.equals("#include"))
                    {
                        includeFile = true;
                        lineNumber = currentLine;
                    }
                    else
                    {
                        if (includeFile)
                        {
                            /*
                             * #include directives can only be followed by a string literal
                             * containing the name of a file. For all practical purposes
                             * having multiple directives on the same line is not a problem
                             * however Macromedia's Flash reports this as an error when
                             * encoding  Flash file, so this behaviour is maintained just to
                             * be compatible.
                             */
                            if (words.length > 2)
                                reportError("SingleDirective", fileName, lineNumber, line);

                            /*
                             * filenames must be enclosed in quotes.
                             */
                            if (token.startsWith("\u005c"") == false || token.endsWith("\u005c"") == false)
                                reportError("QuoteFileName", fileName, lineNumber, line);


                            /*
                             * If the contentsOfFile() method throws an IOException then
                             * change it into a ParseException so the filename, line number
                             * and line of code that triggered the error can be cirrectly
                             * reported.
                             */
                            try {
                                String filename = token.substring(1, token.length()-1);
                                processDirectives(filename, contentsOfFile(filename), out);
                            }
                            catch (FileNotFoundException e) {
                                reportError("FileNotFound", fileName, lineNumber, line);
                            }
                            catch (IOException e) {
                                reportError("ReadError", fileName, lineNumber, line);
                            }
                            includeFile = false;
                        }
                    }
                }
            }
            else
            {
                out.append(line);
                out.append(separator);

                files.add(fileName);
                lines.add(new Integer(currentLine++));
                codes.add(line);
            }
        }
    }

    /*
     * reportError is used to report any errors found when processing a directive. A
     * ParseException is created containing the key which identifies the error and the
     * line number which triggered it. The line mapping arrays are updated so that when
     * the exception is reported to the code using the parser the method: getFileName(),
     * getLineNumber() and getLine() return the correct information.
     *
     * @param errorKey a String that idenfities the type of error that occurred.
     * @param fileName the name of the file which contains the line that triggered the error.
     * @param lineNumber the number of the line that triggered the error.
     * @param line the line of code that triggered the error.
     *
     * @throws ParseException containing the errorKey and line number.
     */
    private void reportError(String errorKey, String fileName, int lineNumber, String line) throws ParseException
    {
        ParseException parseError = new ParseException(errorKey);

        parseError.currentToken = new Token();
        parseError.currentToken.beginLine = lineNumber;

        files.add(fileName);
        lines.add(new Integer(lineNumber));
        codes.add(line);

        throw parseError;
    }

    /*
     * Returns the contents of the file as a single string. The list of directories
     * in the pathNames attribute is searched for the file.
     *
     * @param fileName the name of the file to read.
     *
     * @return a String containing the contents of the file.
     *
     * @throws FileNotFoundException if the file could not be found.
     * @throws IOException if an error occurred while reading the file.
     */
    private String contentsOfFile(String fileName) throws FileNotFoundException, IOException
    {
        String script = "";

        boolean fileFound = false;

        for (Iterator<String> i = pathNames.iterator(); i.hasNext();)
        {
            File aFile = new File(i.next() + File.separator + fileName);

            if (aFile.exists())
            {
                byte[] fileIn = new byte[(int)aFile.length()];

                FileInputStream fileContents = new FileInputStream(aFile);
                fileContents.read(fileIn);

                script = new String(fileIn);

                fileContents.close();
                fileFound = true;
            }
        }

        if (fileFound == false)
            throw new FileNotFoundException();

        return script;
    }
}
