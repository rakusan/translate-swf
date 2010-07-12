// simple expressions

a = true;
b = false;

x = a and b;
x = a and !b;
x = !a and b;
x = !a and !b;
x = !(a and b);

//TODO x = 1 and 2;
//TODO x = 1 and !2;
//TODO x = !1 and 2;
//TODO x = !1 and !2;
//TODO x = !(1 and 2);

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

//TODO x = 1 and 1;
//TODO x = 2 and 2;
//TODO x = 1 and 0;

//TODO x = 2.0 and 2.0;
//TODO x = 1.1 and 0;