/*
 * Translate.java
 * Translate AS1
 *
 * A compiler for ActionScript 1.x
 * Copyright (c) 2003-2006 Flagstone Software Ltd. All rights reserved.
 *
 @license@
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