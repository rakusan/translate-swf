/*
 * Actionscript.java
 * Translate AS1
 * 
 * A compiler for ActionScript 1.x
 * Copyright (c) 2003-2006 Flagstone Software Ltd. All rights reserved.
 *
 @license@
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
import com.flagstone.transform.tools.TextDump;

public class Actionscript
{
    ASParser parser = new ASParser();
    private ASNode root = null;

    private File scriptDir = null;
    private File sourceDir = null;
    private File templateDir = null;
    private File resultDir = null;
    
    private FSMovie frame = null;
    private FSDoAction frameObj = null;

    private FSMovie button = null;
    private FSDefineButton2 buttonObj = null;

    private FSMovie movieclip = null;
    private FSPlaceObject2 movieclipObj = null;
    
    private int version = 0;
    
    public Actionscript()
    {
    }
    /**
     * @testng.configuration beforeTest = "true" alwaysRun = "true" 
     * @testng.parameters value = "script template source result swf"
     */
    public void configure(String script, String template, String source, String result, String swf)
    {
        try
        {
            scriptDir = new File(script);
            templateDir = new File(template);
            sourceDir = new File(source);
            resultDir = new File(result);
                   
            frame = new FSMovie(new File(templateDir, "frame.swf").toString());
            frameObj = (FSDoAction)frame.getObjects().get(1);
                
            button = new FSMovie(new File(templateDir, "button.swf").toString());
            buttonObj = (FSDefineButton2)button.getObjects().get(3);
            
            movieclip = new FSMovie(new File(templateDir, "movieclip.swf").toString());
            movieclipObj = (FSPlaceObject2)movieclip.getObjects().get(3);

            version = Integer.parseInt(swf);
        }
        catch (Exception e)
        {
            assert false : "Cannot configure test suite";
        }
   }
    /**
     * @testng.test dataProvider="actionscript-files"
     */
    public void compile(String file)
    {
        try 
        {
            File srcFile = new File(scriptDir, file);
            File destFile = new File(resultDir, file.substring(0, file.length()-2)+"swf");
            
            if (destFile.getParentFile().exists() == false)
                assert destFile.getParentFile().mkdirs() : "Count not create directory: "+resultDir;

            root = parser.parse(srcFile);
            byte[] data = root.encode(version);
            
            if (root.getType() == ASNode.Array)
            {
                frameObj.setEncodedActions(data);
                frame.encodeToFile(destFile.toString());
            }
            else if (root.getType() == ASNode.Button)
            {
                buttonObj.setEncodedEvents(data);                
                button.encodeToFile(destFile.toString());
            }
            else if (root.getType() == ASNode.MovieClip)
            {             
                movieclipObj.setEncodedEvents(data);                           
                movieclip.encodeToFile(destFile.toString());
            }
            else
            {
                throw new Exception("Unknown node type");
            }
        }
        catch (ParseException e)
        {
            System.err.println(parser.getError());
            System.err.println("    File: " + parser.getFilename() + ", Line: " + parser.getLineNumber());
            System.err.println("    " + parser.getLine());
            
            assert false;
        }
        catch (Throwable e)
        {
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
            file = file.substring(sourceDir.toString().length());
            
            File srcFile = new File(sourceDir, file);
            File dstFile = new File(resultDir, file.substring(0, file.length()-3)+"txt");
            
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
        ArrayList files = new ArrayList();
        
        findFiles(files, sourceDir, ".swf");
                
        Object[][] parameters = new Object[files.size()][1];
        
        for (int i=0; i<files.size(); i++)
        {
            String file = files.get(i).toString();
            
            file = file.substring(sourceDir.toString().length());
            file = file.substring(0, file.length()-3);
            
            parameters[i] = new Object[] { file+"as" };
        }
        return parameters;
    }
    /**
     * @testng.data-provider name="swf-files"
     */
    public Object[][] findSWFFiles()
    {
        ArrayList files = new ArrayList();
        
        findFiles(files, sourceDir, ".swf");
                
        Object[][] parameters = new Object[files.size()][1];
        
        for (int i=0; i<files.size(); i++)
            parameters[i] = new Object[] { files.get(i).toString() };
        
        return parameters;
    }
    
    private void findFiles(ArrayList list, File directory, final String ext)
    {
        FilenameFilter filter = new FilenameFilter() 
        {
            public boolean accept(File dir, String name) 
            {
                boolean accept = false;
                
                File file = new File(dir, name);
                
                if (name.endsWith(ext))
                    accept = true;
                else if (file.isDirectory() && name.equals("..") == false && name.equals(".") == false)
                    accept = true;
                
                return accept;
            }
        };
        
        String[] files = directory.list(filter);
        
        for (int i=0; i<files.length; i++)
        {
            File file = new File(directory, files[i]);
        
            if (file.isDirectory()) 
            {
                findFiles(list, file, ext);               
            }
            else {
                list.add(file);
            }
        }
    }

}
