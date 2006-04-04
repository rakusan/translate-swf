//frame
//
//  Array.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  Array Object.

/*
 * Constructors.
 */
 
a = new Array();
a = new Array(1, 2, 3);
a = new Array(4);
a = ["1", "2", "3"];
a = [];

/*
 * Access the array elements.
 */

a = [1, 2, 3];

a[0];
a[1];
a[2];
 
/*
 * Test the concat method.
 */
 
a = [1, 2, 3];
a.concat(4, 5, 6);

/*
 * Test the join method.
 */
 
a = [1, 2, 3];
a.join();
a.join("--");

/*
 * Test the length method.
 */

a = [1, 2, 3, 4, 5, 6];

for (i=0; i<a.length; i++)
    a[i];

/*
 * Test the pop method.
 */

a = [1, 2, 3];
a.pop();

/*
 * Test the push method.
 */

a = [1, 2, 3];
a.push(7);

/*
 * Test the reverse method.
 */

a = [1, 2, 3];
a.reverse();

/*
 * Test the shift method.
 */

a = [1, 2, 3];
a.shift();

/*
 * Test the slice method.
 */

a = [1, 2, 3, 4, 5, 6];
a.slice(3);
a.slice(2, 4);

/*
 * Test the sort method.
 */

a = [3, 1, 5, 6, 4, 2];
a.sort();

function descending(a, b) 
{
    if (a < b)
        return 1;
    else if (a > b)
        return -1;
    else
        return 0;
}

a.sort(descending);

/*
 * Test the splice method.
 */

a = new Array("a", "b", "c", "d", "e", "f");
a.splice(3, 2, "y", "z");

a = new Array("a", "b", "c", "d", "e", "f");
a.splice(3, 2);

a = new Array("a", "b", "c", "d", "e", "f");
a.splice(3, 0, "x", "y");

a = new Array("a", "b", "c", "d", "e", "f");
a.splice(3);

/*
 * Test the toString method.
 */

a = new Array("a", "b", "c", "d", "e", "f");

/*
 * Test the unshift method.
 */

a = new Array("a", "b", "c", "d", "e", "f");
a.unshift("g");
