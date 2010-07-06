// simple expressions

a = new Object();

a.attr = 0;
a.attr += a.attr;

// find variables as member of the _root property
 
a = 1;

_root.a = 1;
a = _root._x;

// Test whether objects can be dereferenced to an arbitrary level.
 
a.b = 1;
a.b.c = 2;
a.b.c.d = 3;
a.b().c = 4;
a.b().c(x, y).d = 5;
