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

//TODO x = ~0;
//TODO x = ~1;
//TODO x = ~true;
//TODO x = ~false;
//TODO x = ~null;
//TODO x = ~(1 && 0);

// object attributes

obj = {value:0};

x = ~obj.value;


