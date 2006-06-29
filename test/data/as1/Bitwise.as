//frame
//
//  Bitwise.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  Bitwise expressions.

a = 1;
b = 2;
c = 3;
d = -1;

x = a & c;
x = a | b;
x = b ^ c;
x = c << b;
x = d >> b;
x = d >>> b;

x = 1 & 2;
x = 1 | 2;
x = 1 ^ 2;
x = 1 << 2;
x = 1 >> 2;
x = 1 >>> 2;

/*
 * Test operators where value should be discarded.
 */
 
a & c;
a | b;
b ^ c;
c << b;
d >> b;
d >>> b;
