//frame
//
//  Literals.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//

/*
 * Decimal integers
 */
 
// plus or minus sign prefixes
 
-0;
 0;
+0;

-1;
 1;
+1;

// leading zeroes

  01;
 001;
0001;

// largest positive and negative 32-bit values

 2147483647;
-2147483648;

/* 
 * Floating-point Literals
 */

// signed numbers

-0.0;
 0.0;
+0.0;

// leading zeroes (more than 1 leading zero is an error)
 
 .0;
0.0;

// optional or signed exponents

1.0e;
1.0e-1;
1.0e1;
1.0e+1;

-1.0e;
-1.0e-1;
-1.0e1;
-1.0e+1;

+1.0e;
+1.0e-1;
+1.0e1;
+1.0e+1;

// large and small floating point numbers

2.225e-308;
1.798e308;

/*
 * String Literals
 */

"";
"abcd";

'';
'abcd';

'"';
"'";

// strings with control characters;

"\b";
"\f";
"\n";
"\r";
"\t";

"\b\f\n\r\t";

// escaped characters

//"\"";
//"\\";
//'\'';


// octal codes

//"\000";
//"\377";

//"\000\377";

// hexadecimal codes

//"\x00";
//"\xFF";
//"\xff";

//"\x00\xff";

// unicode character codes

//"\u0000";
//"\uFFFF";
//"\uffff";

//"\u0000\uffff";
//"\u0000\uFFFF";

