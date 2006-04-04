//frame
//
//  While.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  While loop.

/*
 * Test a typical while loop.
 */
 
a = 0;
b = 10;

while (a < b)
{
    a += 1;
}

/*
 * Test a while loop with a single statement.
 */
 
a = 0;
b = 10;

while (a < b)
    a += 1;

/*
 * Test a while loop with an empty statement.
 */
 
a = 0;
b = 10;

while (a < b)
    ;

/*
 * Test nested while loops.
 */
 
a = 0;
b = 10;

while (a < b)
{
    c = 0;
    d = 10;
    
    while (c < d) {
        c += 1;
    }

    a += 1;
}

/*
 * Test a while loop with break and continue.
 */
 
a = 0;
b = 10;

while (a < b)
{
    if (a == 5)
        break;
    else
        continue;

    a += 1;
}

/*
 * Test nested while loops with break and continue.
 */
 
a = 0;
b = 10;

while (a < b)
{
    c = 0;
    d = 10;
    
    while (c < d) 
    {
        if (c == 5)
            break;
        else
            continue;
        
        c += 1;
    }
    a += 1;
}
