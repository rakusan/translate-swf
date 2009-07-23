/*
 * Actionscript.java
 * Translate SWF
 * 
 * A compiler for ActionScript 1.x
 * Copyright (c) 2003-2009 Flagstone Software Ltd. All rights reserved.
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
package com.flagstone.translate.test;

import java.io.*;
import java.util.*;

import com.flagstone.translate.ASNode;
import com.flagstone.translate.ASParser;
import com.flagstone.translate.ParseException;

import com.flagstone.transform.FSMovie;
import com.flagstone.transform.FSDoAction;
import com.flagstone.transform.FSPlaceObject2;
import com.flagstone.transform.FSDefineButton2;

import junit.framework.TestCase;
import junit.framework.Assert;

public class Actionscript extends TestCase
{
    private ASParser parser;
    private ASNode root;

    private FSMovie frame;
    private FSDoAction frameObj;

    private FSMovie button;
    private FSDefineButton2 buttonObj;

    private FSMovie movieclip;
    private FSPlaceObject2 movieclipObj;
    
    protected void setUp() throws Exception {
        parser = new ASParser();
        
        File templateDir = new File("test/data/as1/templates/");

        frame = new FSMovie(new File(templateDir, "frame.swf").toString());
        frameObj = (FSDoAction)frame.getObjects().get(1);
            
        button = new FSMovie(new File(templateDir, "button.swf").toString());
        buttonObj = (FSDefineButton2)button.getObjects().get(3);
        
        movieclip = new FSMovie(new File(templateDir, "movieclip.swf").toString());
        movieclipObj = (FSPlaceObject2)movieclip.getObjects().get(3);
    }

    public void testScripts() throws Exception 
    {
        String sourceDir = new File("test/data/as1/scripts").getAbsolutePath();
        String destDir = new File("test/results/as1/swf").getAbsolutePath();

        String refSrcDir = new File("test/data/as1/swf").getAbsolutePath();
        String refDestDir = new File("test/results/as1/ref").getAbsolutePath();
        
        ArrayList files = new ArrayList();
        findFiles(files, new File(sourceDir), ".as");

        String fileIn;
        String fileOut;
        String refIn;
        String refOut;
        
        for (Iterator iter=files.iterator(); iter.hasNext(); ) {
            fileIn = ((File) iter.next()).getAbsolutePath();
            
            fileOut = destDir + fileIn.substring(sourceDir.length());
            fileOut = fileOut.replaceAll("\\.as$", ".swf");
            
            refIn = refSrcDir + fileIn.substring(sourceDir.length());
            refIn = refIn.replaceAll("\\.as$", ".swf");

            refOut = refDestDir + refIn.substring(refSrcDir.length());
            refOut = refOut.replaceAll("\\.swf$", ".txt");
            
            File dir = new File(fileOut).getParentFile();
            
            if (!dir.exists()) {
                Assert.assertTrue("Count not create directory: "+dir, dir.mkdirs());
            }
            compile(fileIn, 7, fileOut);
            
            toText(fileOut, fileOut.replaceAll("\\.swf$", ".txt"));

            dir = new File(refOut).getParentFile();
            
            if (!dir.exists()) {
                Assert.assertTrue("Count not create directory: "+dir, dir.mkdirs());
            }
            
            if (new File(refIn).exists()) {
                toText(refIn, refOut);
            }
        }
    }

    private void compile(String fileIn, int version, String fileOut)
    {
        try 
        {
            root = parser.parse(new File(fileIn));
            byte[] data = root.encode(version);
            
            if (root.getType() == ASNode.Array) {
                frameObj.setEncodedActions(data);
                frame.encodeToFile(fileOut);
            }
            else if (root.getType() == ASNode.Button) {
                buttonObj.setEncodedEvents(data);                
                button.encodeToFile(fileOut);
            }
            else if (root.getType() == ASNode.MovieClip) {             
                movieclipObj.setEncodedEvents(data);                           
                movieclip.encodeToFile(fileOut);
            }
            else {
                throw new Exception("Unknown node type");
            }
        }
        catch (ParseException e) {
            System.err.println(parser.getError());
            System.err.println("    File: " + parser.getFilename() + ", Line: " + parser.getLineNumber());
            System.err.println("    " + parser.getLine());
            
            Assert.fail(e.getMessage());
        }
        catch (Throwable e) {
            Assert.fail(e.toString());
        }
    }

    public void toText(String src, String dst)
    {
        try
        {
            StringBuffer buffer = new StringBuffer();
            FSMovie movie = new FSMovie(src);
            movie.appendDescription(buffer, 100);
            
            PrintWriter writer = new PrintWriter(dst);
            String contents = buffer.toString().replaceAll("\\{", "{\n");
            contents = contents.replaceAll(";", ";\n");
            writer.print(contents.replaceAll("\\};", "};\n"));
            writer.print(contents.replaceAll("\\},", "},\n"));
            writer.flush();
            writer.close();
        }
        catch (Throwable e)
        {
            System.err.println(src);
            e.printStackTrace();
            //Assert.fail(e.toString());
        }
    }
    
    private void findFiles(ArrayList list, File directory, final String ext)
    {
        FilenameFilter filter = new FilenameFilter() 
        {
            public boolean accept(File dir, String name) {
                boolean accept = false;
                
                File file = new File(dir, name);
                
                if (name.endsWith(ext)) {
                    accept = true;
                } else if (file.isDirectory() && name.equals("..") == false && name.equals(".") == false) {
                    accept = true;
                }
                return accept;
            }
        };
        
        String[] files = directory.list(filter);
        
        for (int i=0; i<files.length; i++) {
            File file = new File(directory, files[i]);
        
            if (file.isDirectory()) {
                findFiles(list, file, ext);               
            }
            else {
                list.add(file);
            }
        }
    }

}
