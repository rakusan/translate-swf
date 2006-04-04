//frame
//
//  ArrayAccessor.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  Array Accessor expressions.

/*
 * Access the array elements.
 */

a = [1, 2, 3];

a[0];
a[1];
a[2];

/*
 * Access a two-dimensional array.
 */

a = [[1, 2, 3], [4, 5, 6], [7, 8, 9]];

a[0][0];
a[1][1];
a[2][2];

/*
 * Assign values to the array elements.
 */

a = [1, 2, 3];

a[0] = 4;
a[1] = 5;
a[2] = 6;

/*
 * Assign values to the two-dimensional array.
 */

a = [[1, 2, 3], [4, 5, 6], [7, 8, 9]];

a[0][0] = 9;
a[0][1] = 8;
a[0][2] = 7;
a[1][0] = 6;
a[1][1] = 5;
a[1][2] = 4;
a[2][0] = 3;
a[2][1] = 2;
a[2][2] = 1;

/*
 * Access an object's properties.
 */

function Ship(name, purpose)
{
    this.name = name;
    this.purpose = purpose;
}

aShip = new Ship("Marie Celeste", "GhostShip");

aShip[name];
aShip[purpose];

/*
 * Subscript an anonymous array
 */
 
a = [1, 2, 3, 4][2];

/*
 * Subscript a evaluated array name
 */
 
a = "b";
b = [1, 2, 3, 4];

c = eval(a)[2];
