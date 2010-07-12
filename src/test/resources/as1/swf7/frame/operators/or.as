// simple expressions

a = true;
b = false;

x = a or b;
x = a or !b;
x = !a or b;
x = !a or !b;
x = !(a or b);

// TODO x = 1 or 2;
// TODO x = 1 or !2;
// TODO x = !1 or 2;
// TODO x = !1 or !2;
// TODO x = !(1 or 2);

// discard unused values

a or b;

// using boolean literals

// TODO x = a or true;
// TODO x = a or false;
// TODO x = true or true;
// TODO x = true or false;

// using numeric literals

// TODO x = a or 1;
// TODO x = a or 0;

// TODO x = 1 or 1;
// TODO x = 2 or 2;
// TODO x = 1 or 0;

// TODO x = 2.0 or 2.0;
// TODO x = 1.1 or 0;