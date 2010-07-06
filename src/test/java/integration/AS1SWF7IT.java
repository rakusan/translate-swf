/*
 * AS1IT.java
 * Translate SWF
 *
 * Copyright (c) 2010 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
import java.util.List;
import java.util.zip.DataFormatException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.flagstone.transform.DoAction;
import com.flagstone.transform.Movie;
import com.flagstone.transform.MovieTag;
import com.flagstone.transform.Place2;
import com.flagstone.transform.button.DefineButton2;
import com.flagstone.transform.coder.SWFEncodeable;
import com.flagstone.transform.tools.MovieWriter;
import com.flagstone.translate.ASCompiler;

@RunWith(Parameterized.class)
public class AS1SWF7IT {

    private static final File SRCDIR = 
        new File("src/test/resources/as1/swf7/script");
    private static final File DSTDIR = 
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

        List<File> files = new ArrayList<File>();
        findFiles(files, SRCDIR, filter);

        Object[][] collection = new Object[files.size()][1];
        for (int i = 0; i < files.size(); i++) {
            collection[i][0] = files.get(i);
        }
        return Arrays.asList(collection);
    }

    private static void findFiles(List<File> list, File directory, FilenameFilter filter) {
        String[] files = directory.list(filter);

        for (int i = 0; i < files.length; i++) {
            File file = new File(directory, files[i]);

            if (file.isDirectory()) {
                findFiles(list, file, filter);
            } else {
                list.add(file);
            }
        }
    }

    private final File script;
    private final File reference;

    private ASCompiler compiler;
    private final List<SWFEncodeable> expected;

    public AS1SWF7IT(final File file) {
        script = file;

        String path = file.getPath().replace(SRCDIR.getPath(), DSTDIR.getPath());
        reference = new File(path.substring(0, path.lastIndexOf('.')) + ".swf");

        expected = new ArrayList<SWFEncodeable>();
    }

    @Test
    public void compile() {

        try {
            compiler = new ASCompiler();
            extractActions(reference);

            final List<SWFEncodeable>list = compiler.compile(script);

            final MovieWriter writer = new MovieWriter();
            StringWriter scriptWriter;
            StringWriter expectedWriter;

            Object src;
            Object dst;

            assertEquals(expected.size(), list.size());

            for (int i = 0; i < expected.size(); i++) {
                src = expected.get(i);
                dst = list.get(i);

                if (!src.toString().equals(dst.toString())) {
                    scriptWriter = new StringWriter();
                    expectedWriter = new StringWriter();

                    writer.write(src, scriptWriter);
                    writer.write(dst, expectedWriter);

                    assertEquals(scriptWriter.toString(),
                            expectedWriter.toString());
                }
           }

        } catch (Exception e) {
            if (System.getProperty("test.trace") != null) {
                e.printStackTrace();
            }
            fail(script.getPath());
        }
    }

    private void extractActions(final File file) throws IOException, DataFormatException {
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
}
