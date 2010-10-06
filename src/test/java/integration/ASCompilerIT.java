/*
 * ASCompilerIT.java
 * Translate SWF
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
package integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static utils.FindFiles.findFiles;
import static utils.FindFiles.getFilter;
import static utils.ReplaceReferences.replaceReferences;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.flagstone.transform.DoAction;
import com.flagstone.transform.EventHandler;
import com.flagstone.transform.Movie;
import com.flagstone.transform.MovieTag;
import com.flagstone.transform.Place2;
import com.flagstone.transform.action.Action;
import com.flagstone.transform.action.Push;
import com.flagstone.transform.button.DefineButton2;
import com.flagstone.transform.tools.MovieWriter;
import com.flagstone.translate.ASCompiler;
import com.flagstone.translate.PlayerType;
import com.flagstone.translate.Profile;

@RunWith(Parameterized.class)
public class ASCompilerIT {

    private static final String RESOURCE_DIR = "resources";

    private static final String ACTIONSCRIPT = "actionscript";
    private static final String PROFILE = "profile";
    private static final String FLASH = "flash";
    private static final String PLAYER = "player";
    private static final String TYPE = "type";
    private static final String FILE = "file";

    private static final String[] TYPES = { "frame", "button", "event" };

    @Parameters
	public static Collection<Object[]> files() throws IOException {

		Map<String,Object>map = new LinkedHashMap<String, Object>();
		List<String> files = new ArrayList<String>();
		List<Object[]>collection = new ArrayList<Object[]>();
		File dir;

        for (Profile profile : EnumSet.allOf(Profile.class)) {
            map.put(ACTIONSCRIPT, profile.getScriptVersion());
            map.put(FLASH, profile.getFlashVersion());
            map.put(PLAYER, profile.getPlayer());
            map.put(PROFILE, profile.name());

            for (String type : TYPES) {
        		map.put(TYPE, type);

        		dir = dirForProfile(map);

        		if (dir.exists()) {
            		files.clear();
            		findFiles(files, dir, getFilter(".as"));

            		for (String file : files) {
            			map.put(FILE, file);
            			collection.add(parametersForProfile(map));
            		}
        		}
        	}
        }
        return collection;
	}


    private static File dirForProfile(Map<String,Object> profile)
    		throws IOException {

        String name = (String)profile.get(PROFILE);
        String type = (String)profile.get(TYPE);

		String path = String.format("%s/%s", name, type);
		File dir = new File(RESOURCE_DIR, path);

		return dir;
    }

    private static Object[] parametersForProfile(Map<String,Object> profile) {
        int actionscript = (Integer)profile.get(ACTIONSCRIPT);
        int flash = (Integer)profile.get(FLASH);
        String player = profile.get(PLAYER).toString();
        String file = (String)profile.get(FILE);
        return new Object[] {actionscript, flash, player, file};
    }

	private final File script;
	private final ASCompiler compiler;

	private final List<Action> expected;
	private final List<Action> actual;

	public ASCompilerIT(final int actionscript, final int flash,
			final String player, final String path) {
		compiler = new ASCompiler();
		compiler.setProfile(Profile.fromValues(
				PlayerType.fromName(player), actionscript, flash));

		script = new File(path);
		expected = new ArrayList<Action>();
		actual = new ArrayList<Action>();
	}

	@Before
	public void setUp() throws IOException, DataFormatException {
		File reference = new File(script.getPath().replace(".as", ".swf"));
		extractActions(expected, reference);
		replaceReferences(expected);
		mergeActions(expected);
	}

	@Test
	public void compile() {

		try {
			wrap(actual, compiler.compile(script));
			replaceReferences(actual);
			mergeActions(actual);
			assertEquals(script.getPath(), format(expected), format(actual));

		} catch (Exception e) {
			if (System.getProperty("test.trace") != null) {
				e.printStackTrace();
			}
			fail(script.getPath());
		}
	}

	private static List<Action> wrap(List<Action> dst, List<Action> src) {
		List<Action> tmp = new ArrayList<Action>();

		for (Action action : src) {
			if (action instanceof EventHandler) {
				dst.add(action);
			} else {
				tmp.add(action);
			}
		}
		if (!tmp.isEmpty()) {
			dst.addAll(tmp);
		}
		return dst;
	}

	private static void extractActions(final List<Action>list, final File file)
			throws IOException, DataFormatException {
		final Movie movie = new Movie();
		movie.decodeFromFile(file);

		for (MovieTag tag : movie.getObjects()) {
			if (tag instanceof DoAction) {
				list.addAll(((DoAction) tag).getActions());
			} else if (tag instanceof DefineButton2) {
				list.addAll(((DefineButton2) tag).getEvents());
			} else if (tag instanceof Place2) {
				list.addAll(((Place2) tag).getEvents());
			}
		}
	}

	private static void mergeActions(final List<Action>list) {

		Action current;
		Action next;

		List<Object> merged = new ArrayList<Object>();

		for (int i=0; i < list.size() - 1;) {
			current = list.get(i);
			next = list.get(i+1);

			if (current instanceof Push && next instanceof Push) {
				merged.clear();

				merged.addAll(((Push)current).getValues());
				merged.addAll(((Push)next).getValues());

				list.set(i, new Push(merged));
				list.remove(i+1);
			} else {
				i++;
			}
		}
	}

	private static String format(List<Action> list) throws IOException {
		StringWriter writer = new StringWriter();
		MovieWriter formatter = new MovieWriter();
		for (Action action : list) {
			formatter.write(action, writer);
		}
		return writer.toString();
	}
}
