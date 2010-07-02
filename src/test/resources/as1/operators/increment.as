// simple expressions

a = 10;
x = 0;

x = a++;
x = ++a;
x = a+++1;
x = (a)+++1;
x = a++ + ++b;

// discard unused values

a = 10;

a++;
++a;

// while loop

a = 0;
b = [];

while (a++ < 5)
    b.push(a);

// for loop

a = 0;
b = [];

for (a=0; a < 5; ++a)
    b.push(a);

// object attributes

obj = {count : 10, limit : 20 };

obj.count++;
++obj.limit;

