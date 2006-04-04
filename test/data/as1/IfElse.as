//frame
//
//  IfElse.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  if else statements.

/*
 * Test with a statement block
 */
 
a = 1;
b = 2;

if (a != b)
{
    a += 1;
}

/*
 * Test with a single statement
 */
 
a = 1;
b = 2;

if (a != b)
    a += 1;

/*
 * Test with an empty statement
 */
 
a = 1;
b = 2;

if (a != b)
    ;

/*
 * Test with if..else and block statements
 */
 
a = 1;
b = 2;

if (a != b)
{
    a += 1;
}
else
{
    b += 1;
}

/*
 * Test with if..else and single statements
 */
 
a = 1;
b = 2;

if (a != b)
    a += 1;
else
    b += 1;

/*
 * Test with if..else and empty statements
 */
 
a = 1;
b = 2;

if (a != b)
    ;
else
    ;

/*
 * Test with if..else if..else
 */

a = 1;

if (a == 1)
    a += 1;
else if (a == 2)
    a += 2;
else
    a += 3;

/*
 * Test with nested if..else statements
 */

a = 1;
b = 2;

if (a != b)
{
    if (a < b)
        a += 1;
    else
        b += 1;
}

/*
 * Test with nested if..else statements
 */

a = 1;
b = 2;

if (a != b)
{
    if (a < b)
        a += 1;
    else
        b += 1;
}
else
{
    a += 2;
    b += 2;
}
