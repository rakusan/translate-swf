/*
 * Translate.java
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
package com.flagstone.translate;

/**
 * The %Translate class defines constants and methods used throughout the %Translate package. 
 */
public final class Translate 
{
    // Package information  
    static final boolean DEBUG = false;

    /** 
     * MAJOR is used to identify the current version of the framework. This
     * is incremented for each new version of Flash supported.
     */
    public static final int MAJOR = 2;
    /** 
     * MINOR is used to identify the current minor version of the framework. This
     * is incremented when new functionality is added or API changes are made.
     */
    public static final int MINOR = 0;
    /** 
     * The RELEASE number is used to differentiate between different releases. 
     * This number is incremented when an enhancement or bug fix has been made 
     * and the API is unchanged.
     */
    public static final int RELEASE = 2;

    /** 
     * The main method reports basic information about the package.
     */    
    public static void main(String args[]) 
    {
        String version = MAJOR + "." + MINOR + "." + RELEASE;

        System.out.println("/**");

        System.out.println(
            " * Translate for ActionScript 1.0, Version " + version);
        
        if (Translate.DEBUG)
            System.out.println(" * Debug Edition.");
                    
        System.out.println(" * ");
        System.out.println(" * Copyright Flagstone Software Limited, 2001-2004.");
        System.out.println(" * All Rights Reserved.");
        System.out.println(" * ");
        System.out.println(" * Use of this software is subject to the terms in the license");
        System.out.println(" * that accompanied the software.");
        System.out.println(" * ");
        System.out.println(" */");
    }
}