//frame
//
//  Object.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  User defined Object.

/*
 * Test the constructors
 */

a = {tree : "oak", bush : "laurel" }; 

a = new Object();
a = new Object(1);
a = new Object("1");

/*
 * Test the pre-defined methods.
 */

a.toString();
a.valueOf();

/*
 * Test adding user-defined methods.
 */

a = new Object();

a.methodA = function() { trace("Executing Method A"); };
a.methodB = function() { trace("Executing Method B"); };
a.methodC = function() { methodA(); };

a.methodA();
a.methodB();
a.methodC();

/*
 * Test more complicated constructors.
 */
 
a = {tree : "oak", 
     bush : "laurel",
     berry : [ "blackberry", "raspberry", "salmonberry"]
};

/*
 * Subscript an anonymous object
 */
 
a = {tree : "oak", bush : "laurel" }["bush"]; 

/*
 * Subscript a evaluated object name
 */
 
a = "b";
b = {tree : "oak", bush : "laurel" }; 

c = eval(a)["bush"];


/*
 * Create an evaluated object
 */
 
function Ship(name, purpose)
{
    this.name = name;
    this.purpose = purpose;
}

a = "Ship";

b = new a("Marie Celeste", "GhostShip");

/*
 * object identity - Flash 6+
 */

a instanceof String;

