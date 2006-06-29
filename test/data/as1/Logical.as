//frame
//  Logical.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  Logical expressions.

a = true;
b = false;

x = a && b;
x = a && !b;
x = !a && b;
x = !a && !b;
x = !(a && b);

x = a || b;
x = a || !b;
x = !a || b;
x = !a || !b;
x = !(a || b);

/*
 * Test operators on integer literals.
 */
 
x = 1 && 2;
x = 1 && !2;
x = !1 && 2;
x = !1 && !2;
x = !(1 && 2);

x = 1 || 2;
x = 1 || !2;
x = !1 || 2;
x = !1 || !2;
x = !(1 || 2);

/*
 * Test operators where value should be discarded.
 */
 
a && b;
a && !b;
!a && b;
!a && !b;
!(a && b);

a || b;
a || !b;
!a || b;
!a || !b;
!(a || b);
