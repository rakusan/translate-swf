/*
 * AS1SWF7IT.java
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
import java.io.FileInputStream;
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
import org.yaml.snakeyaml.Yaml;

import com.flagstone.transform.DoAction;
import com.flagstone.transform.Event;
import com.flagstone.transform.EventHandler;
import com.flagstone.transform.Movie;
import com.flagstone.transform.MovieTag;
import com.flagstone.transform.Place2;
import com.flagstone.transform.action.Action;
import com.flagstone.transform.button.DefineButton2;
import com.flagstone.transform.tools.MovieWriter;
import com.flagstone.translate.ASCompiler;
import com.flagstone.translate.PlayerType;
import com.flagstone.translate.Profile;

@RunWith(Parameterized.class)
public class ASCompilerIT {

    private static final String PROFILE_DIR = "src/test/resources/profiles";
    private static final String RESOURCE_DIR = "resources";

    private static final String ACTIONSCRIPT = "actionscript";
    private static final String FLASH = "flash";
    private static final String PLAYER = "player";
    private static final String TYPE = "type";
    private static final String FILE = "file";

    private static final String[] TYPES = { "frame", "button", "event" };

    @Parameters
    @SuppressWarnings("unchecked")
	public static Collection<Object[]> files() throws IOException {

	    File yamlFile = new File(PROFILE_DIR, "profiles.yaml");
		FileInputStream stream = new FileInputStream(yamlFile);
		Yaml yaml = new Yaml();
		Map<String,Object>map = new LinkedHashMap<String, Object>();

		List<String> files = new ArrayList<String>();
		List<Object[]>collection = new ArrayList<Object[]>();
		Profile profile;

        for (Object list : (List<Object>)yaml.load(stream)) {
            profile = Profile.fromName(list.toString());
            map.put(ACTIONSCRIPT, profile.getScriptVersion());
            map.put(FLASH, profile.getFlashVersion());
            map.put(PLAYER, profile.getPlayer());

            for (String type : TYPES) {
        		map.put(TYPE, type);
        		findFiles(files, dirForProfile(map), getFilter(".as"));
        		for (String file : files) {
        			map.put(FILE, file);
        			collection.add(parametersForProfile(map));
        		}
        	}
        }
        return collection;
	}


    private static File dirForProfile(Map<String,Object> profile)
    		throws IOException {

        int actionscript = (Integer)profile.get(ACTIONSCRIPT);
        int flash = (Integer)profile.get(FLASH);
        String player = (String)profile.get(PLAYER);
        String type = (String)profile.get(TYPE);

		String path = String.format("as%d/swf%d/%s/%s",
				actionscript, flash, player, type);
		File dir = new File(RESOURCE_DIR, path);

		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("Cannot create directory: " + dir.getPath());
		}
		return dir;
    }

    private static Object[] parametersForProfile(Map<String,Object> profile) {
        int actionscript = (Integer)profile.get(ACTIONSCRIPT);
        int flash = (Integer)profile.get(FLASH);
        String player = (String)profile.get(PLAYER);
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
		compiler.setScriptVersion(actionscript);
		compiler.setFlashVersion(flash);
		compiler.setPlayer(PlayerType.fromName(player));

		script = new File(path);
		expected = new ArrayList<Action>();
		actual = new ArrayList<Action>();
	}

	@Before
	public void setUp() throws IOException, DataFormatException {
		File reference = new File(script.getPath().replace(".as", ".swf"));
		extractActions(expected, reference);
		replaceReferences(expected);
	}

	@Test
	public void compile() {

		try {
			wrap(actual, compiler.compile(script));
			replaceReferences(actual);
			assertEquals(script.getName(), format(expected), format(actual));

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
			dst.add(new EventHandler(EnumSet.noneOf(Event.class), tmp));
		}
		return dst;
	}

	private static void extractActions(final List<Action>list, final File file)
			throws IOException, DataFormatException {
		final Movie movie = new Movie();
		movie.decodeFromFile(file);

		for (MovieTag tag : movie.getObjects()) {
			if (tag instanceof DoAction) {
				list.add(new EventHandler( EnumSet.noneOf(Event.class),
						((DoAction) tag).getActions()));
			} else if (tag instanceof DefineButton2) {
				list.addAll(((DefineButton2) tag).getEvents());
			} else if (tag instanceof Place2) {
				list.addAll(((Place2) tag).getEvents());
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
