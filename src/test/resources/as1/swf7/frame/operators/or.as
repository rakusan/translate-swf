// simple expressions

a = true;
b = false;

x = a or b;
x = a or !b;
x = !a or b;
x = !a or !b;
x = !(a or b);

x = 1 or 2;
x = 1 or !2;
x = !1 or 2;
x = !1 or !2;
x = !(1 or 2);

// discard unused values

a or b;

// using boolean literals

x = a or true;
x = a or false;
x = true or true;
x = true or false;

// using numeric literals

x = a or 1;
x = a or 0;

x = 1 or 1;
x = 2 or 2;
x = 1 or 0;

x = 2.0 or 2.0;
x = 1.1 or 0;