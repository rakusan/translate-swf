/*
 * DebugScript.java
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
package debug;

import static org.junit.Assert.assertEquals;
import static utils.ReplaceReferences.replaceReferences;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.zip.DataFormatException;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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

@RunWith(Parameterized.class)
public class DebugScript {

	public static void main(final String[] args) {

		ASCompiler compiler = new ASCompiler();
		compiler.setScriptVersion(Integer.valueOf(args[1]));
		compiler.setFlashVersion(Integer.valueOf(args[2]));
		compiler.setPlayer(PlayerType.fromName(args[3]));

		List<Action> expected = new ArrayList<Action>();
		List<Action> actual = new ArrayList<Action>();

		try {
			String path = args[0];
			File reference = new File(path.replace(".as", ".swf"));

			extractActions(expected, reference);
			replaceReferences(expected);

			wrap(actual, compiler.compile(new File(path)));
			replaceReferences(actual);

			assertEquals(path, format(expected), format(actual));

		} catch (Exception e) {
			e.printStackTrace();
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
