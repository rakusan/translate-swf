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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
import com.flagstone.transform.action.NewFunction;
import com.flagstone.transform.action.Push;
import com.flagstone.transform.action.Table;
import com.flagstone.transform.action.TableIndex;
import com.flagstone.transform.button.DefineButton2;
import com.flagstone.transform.tools.MovieWriter;
import com.flagstone.translate.ASCompiler;

@RunWith(Parameterized.class)
public class AS1SWF7IT {

    private static final File SCRIPTDIR =
        new File("src/test/resources/as1/swf7/script");
    private static final File REFDIR =
        new File("src/test/resources/as1/swf7/compiled");

    @Parameters
    public static Collection<Object[]> files() {

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                boolean accept = false;

                File file = new File(dir, name);

                if (name.endsWith(".as")) {
                    accept = true;
                } else if (file.isDirectory() && name.equals("..") == false
                        && name.equals(".") == false) {
                    accept = true;
                }
                return accept;
            }
        };

        List<String> files = new ArrayList<String>();
        findFiles(files, SCRIPTDIR, filter);

        Object[][] collection = new Object[files.size()][1];
        for (int i = 0; i < files.size(); i++) {
            collection[i][0] = files.get(i);
        }
        return Arrays.asList(collection);
    }

    private static void findFiles(List<String> list, File directory,
            FilenameFilter filter) {
        String[] files = directory.list(filter);

        for (int i = 0; i < files.length; i++) {
            File file = new File(directory, files[i]);

            if (file.isDirectory()) {
                findFiles(list, file, filter);
            } else {
                list.add(file.getPath().replace(SCRIPTDIR.getPath(), ""));
            }
        }
    }

    private final File script;
    private final File reference;
    private final ASCompiler compiler;

    private final List<Object> expected;
    private List<Object> actual;

    public AS1SWF7IT(final String path) {
        compiler = new ASCompiler();
        compiler.setActionscriptVersion(1);
        compiler.setFlashVersion(7);
        script = new File(SCRIPTDIR, path);
        reference = new File(REFDIR, path.replace(".as", ".swf"));
        expected = new ArrayList<Object>();
    }

    @Before
    public void setUp() throws IOException, DataFormatException {
        extractActions(reference);
        replaceReferences(expected);
    }

    @Test
    public void compile() {

        try {
            actual = compiler.compile(script);
            replaceReferences(actual);
            assertEquals(format(expected), format(actual));
        } catch (Exception e) {
            if (System.getProperty("test.trace") != null) {
                e.printStackTrace();
            }
            fail(script.getPath());
        }
    }

    private String format(Object obj) throws IOException {
        final MovieWriter writer = new MovieWriter();
        final StringWriter printer = new StringWriter();
        writer.write(obj, printer);
        return printer.toString();
    }

    private void extractActions(final File file)
            throws IOException, DataFormatException {
        final Movie movie = new Movie();
        movie.decodeFromFile(file);

        for (MovieTag tag : movie.getObjects()) {
            if (tag instanceof DoAction) {
                expected.add(tag);
            } else if (tag instanceof DefineButton2) {
                expected.addAll(((DefineButton2) tag).getEvents());
            } else if (tag instanceof Place2) {
                expected.addAll(((Place2) tag).getEvents());
            }
        }
    }

    private void replaceReferences(final List<Object> list) {
        for (Object object : list) {
            replaceReferences(object);
        }
    }

    private void replaceReferences(final Object object) {
        if (object instanceof DoAction) {
            updateActions(((DoAction) object).getActions());
        } else if (object instanceof EventHandler) {
            updateActions(((EventHandler) object).getActions());
        }
    }

    private void updateActions(final List<Action> list) {
        updateActions(extractTable(list), list);
    }

    private Table extractTable(final List<Action> list) {
        Table table = new Table();
        Action action;

        for (Iterator<Action> iter = list.iterator(); iter.hasNext();) {
            action = iter.next();
            if (action instanceof Table) {
                table = (Table)action;
                iter.remove();
            }
        }
        return table;
    }

    private void updateActions(final Table table, final List<Action> list) {
        Action action;
        for (int i = 0; i < list.size(); i++) {
            action = list.get(i);
            if (action instanceof Push) {
                list.set(i, replaceAction(table, (Push) action));
            } else if (action instanceof NewFunction) {
                list.set(i, replaceAction(table, (NewFunction) action));
            }
        }
    }

    private Push replaceAction(final Table table, final Push action) {
        List<Object>values = action.getValues();
        replaceTableIndex(table, values);
        return new Push(values);
    }

    private NewFunction replaceAction(final Table table,
            final NewFunction action) {
        List<Action>actions = action.getActions();
        updateActions(table, actions);
        return new NewFunction(action.getName(),
                action.getArguments(), actions);
    }

    private void replaceTableIndex(final Table table,
            final List<Object> list) {
        Object value;
        int index;
        for (int j = 0; j < list.size(); j++) {
            value = list.get(j);
            if (value instanceof TableIndex) {
                index = ((TableIndex)value).getIndex();
                list.set(j, table.getValues().get(index));
            }
        }
    }
}
