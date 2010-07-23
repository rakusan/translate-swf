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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.flagstone.transform.action.Action;
import com.flagstone.translate.as.ASParser;
import com.flagstone.translate.as.ParseException;
import com.flagstone.translate.as.Token;

public final class ASCompiler {

	/** Version of actionscript to be compiled. */
	private transient int scriptVersion = 1;
	/** Version of Flash to generate objects for. */
	private transient int flashVersion = 5;
	/** Runtime platform targetted. */
	private transient PlayerType player = PlayerType.DEFAULT;
	/** The character encoding used in the scripts. */
	private transient String encoding = "UTF-8";
	/** List directories searched when looking for included file. */
	private transient final ArrayList<String> pathNames =
		new ArrayList<String>();
	/** List of errors that occurred while compiling the scripts. */
	private transient final List<ScriptError> errors =
		new ArrayList<ScriptError>();
	/** Table of parsed scripts, indexed by source file name. */
	private transient final Map<String, Node> scripts =
		new LinkedHashMap<String, Node>();
	/** Table of definitions, indexed by name. */
	private transient final Map<String, Node> defines =
		new LinkedHashMap<String, Node>();

	private transient String path = "";

	/**
	 * Set the version of actionscript that the script will be in.
	 *
	 * @param version
	 *            the version of actionscript used in the code.
	 */
	public void setScriptVersion(int version) {
		if (version != 1) {
			throw new IllegalArgumentException(
					"Unsupported actionscript version.");
		}
		scriptVersion = version;
	}

	/**
	 * Set the version of Flash that the compiled actions will be generated for.
	 *
	 * @param version
	 *            the target version of Flash that the code will be compiled
	 *            for.
	 */
	public void setFlashVersion(int version) {
		flashVersion = version;
	}

	/**
	 * Set the target runtime environment that the actions will be generated
	 * for.
	 *
	 * @param player
	 *            the runtime environment that code will be compiled for.
	 */
	public void setPlayer(PlayerType type) {
		player = type;
	}

	/**
	 * Sets the encoding scheme used in scripts.
	 *
	 * @param enc the character encoding used for strings.
	 */
	public void setEncoding(final String enc) {
		encoding = enc;
	}

	/**
	 * Add a directory to the list that will be searched for #included
	 * files.
	 *
	 * @param dir
	 *            the path to a directory.
	 */
	public void add(File dir) {
		if (!dir.exists()) {
			throw new IllegalArgumentException("Directory does not exist.");
		} else if (!dir.isDirectory()) {
			throw new IllegalArgumentException("Not a directory.");
		}
		pathNames.add(path);
	}

	/**
	 * Compiles ActionScript read from a file.
	 *
	 * @param file
	 *            a File containing the ActionScript statements to parse.
	 *
	 * @return list of Actions that the script compiles to.
	 *
	 * @throws IOException
	 *             if the file cannot be found, opened or if the file cannot be
	 *             read.
	 *
	 * @throws ScriptException
	 *             if one or more errors are found in the script.
	 */
	public List<Action> compile(File file) throws IOException, ScriptException {
		List<Action> list = new ArrayList<Action>();
		FileInputStream stream = null;
		try {
			path = file.getPath();
			stream = new FileInputStream(file);
			list = compile(stream);
//			list = compile(contentsOfFile(file));
		} finally {
			if (stream != null) {
				stream.close();
			}
			path = "";
		}
		return list;
	}

	private List<Action> compile(final String script) throws ScriptException {
		List<Action> list = new ArrayList<Action>();

		try {
			ASContext context = new ASContext(encoding, flashVersion);
			ASParser parser = new ASParser();
			ASNode node = parser.parse(script);
			list = node.compile(context);
		} catch (ParseException e) {
			Token token = e.currentToken;
			errors.add(new ScriptError(ScriptError.Type.SCRIPT_PARSE_ERROR,
					path, token.beginLine, token.endLine,
					token.beginColumn, token.endColumn));
		}

		if (!errors.isEmpty()) {
			throw new ScriptException(errors);
		}

		return list;
	}

    private String contentsOfFile(File file)
    		throws FileNotFoundException, IOException {
        String script = "";
        byte[] fileIn = new byte[(int)file.length()];
        FileInputStream fileContents = new FileInputStream(file);
        fileContents.read(fileIn);
        script = new String(fileIn);
        fileContents.close();
        return script;
    }

	/**
	 * Compiles ActionScript read from a stream.
	 *
	 * @param file
	 *            a File containing the ActionScript statements to parse.
	 *
	 * @return list of Actions that the script compiles to.
	 *
	 * @throws IOException
	 *             if an error occurs while reading the stream.
	 *
	 * @throws ScriptException
	 *             if one or more errors are found in the script.
	 */
	public List<Action> compile(final InputStream stream)
			throws IOException, ScriptException {
		List<Action> list = new ArrayList<Action>();

		Context context = new Context(encoding, flashVersion);
		Generator registry = GeneratorRegistry.getGenerator(scriptVersion);
		Parser parser = ParserRegistry.getParser(scriptVersion);

		try {
			errors.clear();
			scripts.clear();

			parser.setErrors(errors);

			loadScript(parser, scripts, path, stream);
			compile(list, registry, context, scripts.get(path));
		} finally {
			path = "";
		}

		if (!errors.isEmpty()) {
			throw new ScriptException(errors);
		}
		return list;
	}

	private void loadScript(final Parser parser,
			final Map<String, Node>map,
			final String key,
			final InputStream stream) {

		parser.setPath(path);
		Node node = parser.parse(stream);
	    map.put(key, node);

	    List<Node> includes = new ArrayList<Node>();
	    findNodes(includes, node, NodeType.INCLUDE);

	    String path;
	    File file;

	    for (Node include : includes) {
	    	path = include.get(0).getValue();

		    if (!map.containsKey(path)) {
		    	try {
		            file = findFile(path);
			        loadScript(parser, map, path, new FileInputStream(file));
		    	} catch (FileNotFoundException e) {
		    		errors.add(new ScriptError(
		    				ScriptError.Type.SCRIPT_NOT_FOUND, path));
		    	} catch (IOException e) {
		    		errors.add(new ScriptError(
		    				ScriptError.Type.SCRIPT_READ_ERROR, path));
		    	}
			}
		}
	}

	private void compile(final List<Action> actions, final Generator registry,
			final Context context, final Node node) {
		CodeGenerator generator = registry.getGenerator(node.getType());
		generator.search(registry, context, node);
		generator.reorder(registry, context, node);
		generator.generate(registry, context, node, actions);
	}

	private void findNodes(final List<Node> list, final Node node, final NodeType type) {
		int count = node.count();

		if (node.getType() == type) {
			list.add(node);
		}

		for (int i = 0; i < count; i++) {
			findNodes(list, node.get(i), type);
		}
	}

	private File findFile(String fileName)
			throws FileNotFoundException, IOException {

		boolean fileFound = false;
		File file = null;

		for (String path : pathNames) {
			file = new File(path, fileName);

			if (file.exists()) {
				fileFound = true;
				break;
			}
		}

		if (fileFound == false)
			throw new FileNotFoundException(fileName);

		return file;
	}
}
