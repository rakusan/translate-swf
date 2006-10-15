// simple expressions

a = true;
b = false;

x = a and b;
x = a and !b;
x = !a and b;
x = !a and !b;
x = !(a and b);

x = 1 and 2;
x = 1 and !2;
x = !1 and 2;
x = !1 and !2;
x = !(1 and 2);

// discard unused values

a and b;

// using boolean literals

x = a and true;
x = a and false;
x = true and true;
x = true and false;

// using numeric literals

x = a and 1;
x = a and 0;

x = 1 and 1;
x = 2 and 2;
x = 1 and 0;

x = 2.0 and 2.0;
x = 1.1 and 0;