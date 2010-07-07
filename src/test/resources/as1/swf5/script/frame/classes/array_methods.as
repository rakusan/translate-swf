/*
 * Test the concat method.
 */
 
a = [1, 2, 3];

x = a.concat();
x = a.concat(4, 5, 6);

/*
 * Test the join method.
 */
 
a = [1, 2, 3];

a.join();

a = [1, 2, 3];

a.join("--");

/*
 * Test the pop method.
 */

a = [1, 2, 3];

a.pop();

/*
 * Test the push method.
 */

a = [1, 2, 3];

a.push(4);
a.push(5, 6, 7);


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
a.slice(-1);

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

a.toString();

/*
 * Test the unshift method.
 */

a = new Array("a", "b", "c", "d", "e", "f");

a.unshift("g");
