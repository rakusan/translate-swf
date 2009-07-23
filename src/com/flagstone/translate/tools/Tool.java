/*
 * Tool.java
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