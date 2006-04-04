//frame
//
//  ObjectDereference.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  Dereferencing objects.

a = new Object();
a.attr = 0;

a.attr = a.attr + 1;

a = 1;

_root.a = 1;

a = _root._x;

/*
 * Test whether objects can be dereferenced to an arbitrary level.
 */ 
 
a.b = 1;
a.b.c = 2;
a.b.c.d = 3;
a.b().c = 4;
a.b().c(x, y).d = 5;
