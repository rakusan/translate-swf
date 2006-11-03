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

x = a[0];
x = a[1];
x = a[2];
 
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

a.sort(1);
a.sort(Array.CASEINSENSITIVE);

a.sort(2);
a.sort(Array.DESCENDING);

a.sort(4);
a.sort(Array.UNIQUE);

a.sort(8);
a.sort(Array.RETURNINDEXEDARRAY);

a.sort(16);
a.sort(Array.NUMERIC);

a.sort(2 | 16);
a.sort(Array.DESCENDING | Array.NUMERIC);

a.sort(descending, Array.DESCENDING | Array.NUMERIC);


/*
 * Test the sortOn method.
 */

a = new Array();

a.push({name: "Alice", age: 24});
a.push({name: "Bob", age: 25});
a.push({name: "Jane", age: 34});
a.push({name: "John", age: 35});


a.sort(descending);

a.sortOn("name", 1);
a.sortOn("name", Array.CASEINSENSITIVE);

a.sortOn("age", 2);
a.sortOn("age", Array.DESCENDING);

a.sortOn("name", 4);
a.sortOn("name", Array.UNIQUE);

a.sortOn(["name", "age"], 2 | 16);
a.sortOn(["name", "age"], Array.DESCENDING | Array.NUMERIC);


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
