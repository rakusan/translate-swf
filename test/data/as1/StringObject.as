//frame
//
//  String.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  String Object.

/*
 * Test the class methods
 */

a = String.fromCharCode(97, 98, 99);

/*
 * Test the constructors
 */

a = new String();
a = new String("A String");
a = "A String";

/*
 * Test the methods
 */

a = "abc";

a.charAt(0);
a.charCodeAt(0);
a.concat("def");

a.indexOf("b");
a.indexOf("b", 1);

a.lastIndexOf("b");
a.lastIndexOf("b", 1);

a.length;
a.slice(1, 2);
a.split("b");

a.substr(1);
a.substr(1, 1);

a.substring(1, 3);
a.toLowerCase();
a.toUpperCase();
