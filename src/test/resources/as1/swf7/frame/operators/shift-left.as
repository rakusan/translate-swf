// simple expressions.

a = 1;
b = 2;
c = 3;

x = a << b;
x = a << b << c;

// Test operators where value should be discarded.
 
a << b;

// using literals

x = a << -1;
x = a << 0;
x = a << 1;
x = a << 2;

x = a << "1";
