// simple expressions
 
a = 1;
b = 2;
c = 3;

x = (a == 1) ? b : c;

// Test the nested statements
 
x = (a == 1) ? ((b == 1) ? a : c) : c;

x = ((b == 1) ? 1 : 0) ? ((b == 1) ? a : c) : c;

// Test operators where value should be discarded.
 
(a == 1) ? ((b == 1) ? a : c) : c;
