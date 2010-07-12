// simple expressions

a = true;
b = false;

x = a || b;
x = a || !b;
x = !a || b;
x = !a || !b;
x = !(a || b);

// TODO - Generated code is correct but does not take literal values into account
//x = 1 || 2;
//x = 1 || !2;
//x = !1 || 2;
//x = !1 || !2;
//x = !(1 || 2);

// discard unused values

a || b;

// using boolean literals

x = a || true;
x = a || false;
x = true || true;
x = true || false;

// using nmeric literals

x = a || 1;
x = a || 0;

// TODO - Generated code is correct but does not take literal values into account
//x = 1 || 1;
//x = 2 || 2;
//x = 1 || 0;

//x = 2.0 || 2.0;
//x = 1.1 || 0;