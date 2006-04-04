/*
 * ASParserTest.java
 * Translate AS1
 * 
 * A compiler for ActionScript 1.x
 * Copyright (c) 2003-2006 Flagstone Software Ltd. All rights reserved.
 *
 @license@
 */
package com.flagstone.translate.test;

import java.io.*;

import com.flagstone.transform.tools.TextDump;
import com.flagstone.translate.ASNode;
import com.flagstone.translate.ASParser;
import com.flagstone.translate.ParseException;

public class ASParserTest
{
    ASParser parser = new ASParser();
    private ASNode root = null;

    private File sourceDir = null;
    private File destDir = null;
    
    public ASParserTest()
    {
    }
    /**
     * @testng.configuration beforeTest = "true" alwaysRun = "true" 
     * @testng.parameters value = "srcDir dstDir"
     */
    public void configure(String srcDir, String dstDir)
    {
        sourceDir = new File(srcDir);
        destDir = new File(dstDir);
    }
    /**
     * @testng.test dataProvider="actionscript-files"
     */
    public void compile(String file)
    {
        try 
        {
            File srcFile = new File(sourceDir, file);
            File destFile = new File(destDir, file.substring(0, file.length()-2)+"swf");
            
            if (destDir.exists() == false)
                assert destDir.mkdirs() : "Count not create directory: "+destDir;

            root = parser.parse(srcFile);
            
            if (root.getType() == ASNode.Array)
                frameScript(destFile);
            else if (root.getType() == ASNode.Button)
                buttonScript(destFile);
            else if (root.getType() == ASNode.MovieClip)
                movieClipScript(destFile);
            else
                throw new Exception("Unknown node type");
        }
        catch (ParseException e)
        {
            System.err.println(parser.getError());
            System.err.println("    File: " + parser.getFilename() + ", Line: " + parser.getLineNumber());
            System.err.println("    " + parser.getLine());
            
            assert false : file+": "+e.toString();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            
            assert false : file+": "+e.toString();
        }
   }
    /**
     * @testng.test dataProvider="swf-files"
     */
    public void toText(String file)
    {
        try
        {
            File srcFile = new File(sourceDir, file);
            File dstFile = new File(destDir, file.substring(0, file.length()-3)+"txt");
            
            new TextDump(new String[] {
                            "--fileIn", srcFile.getPath(), 
                            "--fileOut", dstFile.getPath()});
        }
        catch (Throwable e)
        {
            assert false : file+": "+e.toString();
        }
    }
    /**
     * @testng.data-provider name="actionscript-files"
     */
    public Object[][] findASFiles()
    {
        FilenameFilter filter = new FilenameFilter() 
        {
            public boolean accept(File directory, String name) {
                return name.endsWith(".as");
            }
        };
        
        String[] files = sourceDir.list(filter);       

        Object[][] parameters = new Object[files.length][1];
        
        for (int i=0; i<files.length; i++)
            parameters[i] = new Object[] { files[i] };
        
        return parameters;
    }
    /**
     * @testng.data-provider name="swf-files"
     */
    public Object[][] findFiles()
    {
        FilenameFilter filter = new FilenameFilter() 
        {
            public boolean accept(File directory, String name) {
                return name.endsWith(".swf");
            }
        };
        
        String[] files = sourceDir.list(filter);       

        Object[][] parameters = new Object[files.length][1];
        
        for (int i=0; i<files.length; i++)
            parameters[i] = new Object[] { files[i] };
        
        return parameters;
    }
    /*
     * Generates a sample swf file containing a single frame which executes the 
     * compiled actions when displayed.
     */
    private void frameScript(File file)
    {
        try
        {
            FileOutputStream out = new FileOutputStream(file);
                    
            byte[] data = root.encode(5);
                    
            int fileLength = 35 + data.length + 1;
            int actionLength = data.length + 1;

            /*
             * The signature identifies the file as containing Flash 5.
             */
            int[] signature = { 0x46, 0x57, 0x53, 0x05 };
                    
            for (int i=0; i<signature.length; i++)
                out.write(signature[i]);

            // Write out length of file
                    
            for (int i=0; i<4; i++, fileLength >>>= 8)
                out.write(fileLength);
                
            /*
             * The rest of the header sets the frame size to 200 x 200 
             * pixels, plays at 12 frames per second, contains 1 frame,
             * sets the background colour to be white and writes out
             * the first 2 bytes of the DoAction tag.
             */
                     
            int[] header = { 
                0x68, 0x00, 0x1f, 0x40, 0x00, 0x07, 0xd0, 0x00,
                0x00, 0x0c, 0x01, 0x00, 0x43, 0x02, 0xff, 0xff,
                0xff, 0x3f, 0x03
            };
                    
            for (int i=0; i<header.length; i++)
                out.write(header[i]);
                        
            for (int i=0; i<4; i++, actionLength >>>= 8)
                out.write(actionLength);

            out.write(data);
            out.write(0);
                    
            // ShowFrame
                    
            out.write(64);
            out.write(0);
                    
            // End of Movie
                    
            out.write(0);
            out.write(0);
            out.close();
        }
        catch (IOException e)
        {
            assert false: "Could not write Flash file: "+file;            
        }
    }    
    /*
     * Generates a sample swf file containing the compiled actions for a button
     * No button is displayed in the movie however the file may be browsed by 
     * a suitable viewer or parsed using Transform to inspect the byte-codes and
     * actions generated when the script is compiled.
     */
    private void buttonScript(File file)
    {
        try
        {
            FileOutputStream out = new FileOutputStream(file);
                    
            byte[] data = root.encode(5);
                    
            int fileLength = 47 + data.length;
            int actionLength = data.length + 13;

            /*
             * The signature identifies the file as containing Flash 5.
             */
            int[] signature = { 0x46, 0x57, 0x53, 0x05 };
                    
            for (int i=0; i<signature.length; i++)
                out.write(signature[i]);

            // Write out length of file
                    
            for (int i=0; i<4; i++, fileLength >>>= 8)
                out.write(fileLength);
                
            /*
             * The rest of the header sets the frame size to 200 x 200 
             * pixels, plays at 12 frames per second, contains 1 frame,
             * sets the background colour to be white and writes out
             * the first bytes of the DefineButton2 tag.
             */
                     
            int[] header = { 
                0x68, 0x00, 0x1f, 0x40, 0x00, 0x07, 0xd0, 0x00,
                0x00, 0x0c, 0x01, 0x00, 0x43, 0x02, 0xff, 0xff,
                0xff, 0xbf, 0x08, 
                
            };
                    
            for (int i=0; i<header.length; i++)
                out.write(header[i]);
                        
            for (int i=0; i<4; i++, actionLength >>>= 8)
                out.write(actionLength);

            int[] body = { 
                 0x02, 0x00, 0x00, 0x0A, 0x00, 0x0F, 0x01, 0x00,
                 0x01, 0x00, 0x00, 0x04, 0x00,
            };
            
            for (int i=0; i<body.length; i++)
                out.write(body[i]);        
            
            out.write(data);
                    
            // ShowFrame
                    
            out.write(64);
            out.write(0);
                    
            // End of Movie
                    
            out.write(0);
            out.write(0);
            out.close();
        }
        catch (IOException e)
        {
            assert false: "Could not write Flash file: "+file;            
        }
    }
    /*
     * Generates a sample swf file containing the compiled actions for a movie
     * clip. Nothing is displayed in the movie however the file may be browsed by 
     * a suitable viewer or parsed using Transform to inspect the byte-codes and
     * actions generated when the script is compiled.
     */
    private void movieClipScript(File file)
    {
        try
        {
            FileOutputStream out = new FileOutputStream(file);
                    
            byte[] data = root.encode(5);
                    
            int fileLength = 38 + data.length;
            int actionLength = data.length + 7;

            /*
             * The signature identifies the file as containing Flash 5.
             */
            int[] signature = { 0x46, 0x57, 0x53, 0x05 };
                    
            for (int i=0; i<signature.length; i++)
                out.write(signature[i]);

            // Write out length of file
                    
            for (int i=0; i<4; i++, fileLength >>>= 8)
                out.write(fileLength);
                
            /*
             * The rest of the header sets the frame size to 200 x 200 
             * pixels, plays at 12 frames per second, contains 1 frame,
             * sets the background colour to be white and writes out
             * the first bytes of the PlaceObject2 tag.
             */
                     
            int[] header = { 
                0x68, 0x00, 0x1f, 0x40, 0x00, 0x07, 0xd0, 0x00,
                0x00, 0x0c, 0x01, 0x00, 0x43, 0x02, 0xff, 0xff,
                0xff, 0xbf, 0x06, 
                
            };
                    
            for (int i=0; i<header.length; i++)
                out.write(header[i]);
                        
            for (int i=0; i<4; i++, actionLength >>>= 8)
                out.write(actionLength);

            int[] body = { 
                 0x82, 0x01, 0x00, 0x01, 0x00, 
            };
            
            for (int i=0; i<body.length; i++)
                out.write(body[i]);        
            
            out.write(data);
                    
            out.write(0);
            out.write(0);

            // ShowFrame
                    
            out.write(64);
            out.write(0);
                    
            // End of Movie
                    
            out.write(0);
            out.write(0);
            out.close();
        }
        catch (IOException e)
        {
            assert false: "Could not write Flash file: "+file;            
        }
    }
}
