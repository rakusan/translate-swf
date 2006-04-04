//frame
//
//  With.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  With statement.

/*
 * Test a typical with statement using properties.
 */
 
a = new Object();

with (a)
{
    _width = 10;
    _width = 20;
}

/*
 * Test a typical with statement using attributes.
 */
 
a = new Object();

with (a)
{
    x = 10;
    y = 20;
}

/*
 * Test a typical with statement using attributes.
 */
 
a = new Object();

with (a)
{
    x = 10;
    y = 20;
}

/*
 * Test a with statement using a dereference expression.
 */
 
a = new Object();
a.x = new Object();

with (a.x)
{
    y = 10;
}

/*
 * Test a with statement using an empty statement.
 */
 
a = new Object();

with (a);

/*
 * Test nested with statements.
 */
 
a = new Object();
b = new Object();

with (a)
{
	x = 1;
	
	with (b)
	{
		y = 2;
	}
}
