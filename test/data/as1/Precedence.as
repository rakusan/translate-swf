//
//  Precedence.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
// 
//  ParseFloat is used to convert strings and numbers to a floating-point value.
//

a = 1;
b = 2;
c = 3;

d = a[1]++;
d = -(a + b);

d = -a * b;
d = c / +d;

d = a * b++;
d = --a / b;

d = a * b + c;
d = a - c / d;

d = a + b << c;
d = a >> b - c;

d = a << b < c;
d = a > b >> c;

d = a < b != c;
d = a == b >= c;

d = a == b & c;
d = a | b != c;

d = a & b && c;
d = a || b | c;

d = a && b ? a | b : a || b;
