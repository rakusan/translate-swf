//frame
//
//  For.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  For.

/*
 * Test a typical for statement
 */
 
a = 0;

for (i=0; i<10; i++)
{
    a += 1;
}

/*
 * Test for loop with a single statement
 */
 
a = 0;

for (i=0; i<10; i++)
    a += 1;

/*
 * Test for loop with an empty statement
 */
 
a = 0;

for (i=0; i<10; i++);

/*
 * Test nested for loops
 */
 
a = 0;

for (i=0; i<10; i++)
{
    b = 0;
    
    for (j=0; j<10; j++)
    {
        b += 1;
    }
    a += 1;
}

/*
 * Test for loop with break and continue
 */
 
a = 0;

for (i=0; i<10; i++)
{
    if (i == 5) 
        break;
    else 
        continue;

    a += 1;
}

/*
 * Test nested for loops with break and continue
 */
 
a = 0;

for (i=0; i<10; i++)
{
    b = 0;
    
    for (j=0; j<10; j++)
    {
        if (j == 5) 
            break;
        else 
            continue;

        b += 1;
    }
    a += 1;
}

/*
 * Test for loop without an initialization statement
 */
 
a = 0; 
i = 0;

for (; i<10; i++)
{
    a += 1;
}

/*
 * Test for loop without an end of loop statement
 */
 
a = 0; 

for (i=0; i<10;)
{
    a += 1;
    i += 1; 
}

/*
 * Test for loop without an initialization and end of loop statements
 */
 
a = 0; 
i = 0;

for (; i<10;)
{
    a += 1;
    i += 1; 
}

/*
 * Test for loop with no statements
 */
 
a = 0; 
i = 0;

for (;;)
{
    a += 1;
    
    if (a == 5) break;
}

/*
 * Test for loop with an infinite loop
 */
 
a = 0; 
i = 0;

for (; true;)
{
    a += 1;
}
