//frame
//
//  Arithmetic.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  Arithmetic expressions.

a = 1;
b = 2;
c = 3;

d = a + b;
d = a - b;
d = b * c;
d = c / b;
d = c % b;
d = (a + b) * c;
d = a + (b / c);
d = ((a + b) * c) / ((b * c) + 1);

/*
 * Test operators where value should be discarded.
 */
 
a + b;
a - b;
b * c;
c / b;
c % b;
(a + b) * c;
a + (b / c);
((a + b) * c) / ((b * c) + 1);