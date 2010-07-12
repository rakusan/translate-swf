// simple expressions

a = 2;
b = 3;
c = 4;

x = a & b;
x = a & b & c;

// discard unused values

a & b;

// using numeric literals

x = 3 & 2;
x = 3 & 0;
x = 3 & ~0;

// TODO - Generated code is correct but does not take literal values into accoumt.
// x = 4.3 & 1.4;

