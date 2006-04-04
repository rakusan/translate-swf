/*
 * Tool.java
 * Translate AS1
 *
 * A compiler for ActionScript 1.x
 * Copyright (c) 2003-2006 Flagstone Software Ltd. All rights reserved.
 *
 @license@
 */
package com.flagstone.translate.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

public abstract class Tool implements FilenameFilter
{
    protected HashMap options = new HashMap();
    
    protected String extension = null; 
    
    public Tool(String[] args)
    {
        getOptions(args);
    }
    public void getOptions(String[] args)
    {
        String optionName = null;
        String optionValue = null;
        
        for (int i=0; i<args.length; i++)
        {
            if (args[i].length() >= 2 && args[i].substring(0, 2).equals("--"))
            {
                optionName = args[i].substring(2);
                optionValue = "";
                
                if (i+1 < args.length)
                    optionValue = args[i+1];
                    
                options.put(optionName, optionValue);
            }
        }
    }
    public String getOption(String name, String defaultValue)
    {
        String value = defaultValue;
        
        if (options.containsKey(name))
            value = (String)options.get(name);
    
        return value;
    }
    public int getOption(String name, int defaultValue)
    {
        int value = defaultValue;
        
        if (options.containsKey(name))
        {
            try {
                value = new Integer((String)options.get(name)).intValue();
            }
            catch (NumberFormatException e) {
            }
        }
        return value;
    }
    public String[] getFiles(String dirname)
    {
        File dir = new File(dirname);
        String filenames[] = null;
         
        if (dir.isDirectory())
            filenames = dir.list(this);

        return filenames;
    }
    public boolean accept(File dir, String name)
    {
        return name.endsWith(extension);
    }
}