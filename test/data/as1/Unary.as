//frame
//  Unary.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  Unary expressions.

a = 1;
b = 2;

a = -1;
b = -2;

a = +1;
b = +2;

x = ++a;
x = a + b++;
x = --b;
x = a + b--;
x = -a;
x = ~a;
x = !b;

/*
 * Test operators where value should be discarded.
 */
 
++a;
a++;
--a;
a--;
-a;
~a;
!a;

/* 
 * Test operators on object attributes.
 */

a.b++;
++a.b;

a.b--;
--a.b;

-a.b;
~a.b;
!a.b;