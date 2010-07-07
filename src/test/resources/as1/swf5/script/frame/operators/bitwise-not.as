// simple expressions

a = 1;
x = 0;

x = ~a;
x = ~(a+a);
x = ~~a;

// discard unused values

a = 0;

~a;

// literals

x = ~0;
x = ~1;
x = ~true;
x = ~false;
x = ~null;
x = ~(1 && 0);

// object attributes

obj = {value:0};

x = ~obj.value;


