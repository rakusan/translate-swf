/*
 * Test the sortOn method.
 */

function descending(a, b) 
{
    if (a < b)
        return 1;
    else if (a > b)
        return -1;
    else
        return 0;
}

a = new Array();

a.push({name: "Alice", age: 24});
a.push({name: "Bob", age: 25});
a.push({name: "Jane", age: 34});
a.push({name: "John", age: 35});


a.sortOn("name", 1);
a.sortOn("name", Array.CASEINSENSITIVE);

a.sortOn("age", 2);
a.sortOn("age", Array.DESCENDING);

a.sortOn("name", 4);
a.sortOn("name", Array.UNIQUE);

a.sortOn(["name", "age"], 2 | 16);
a.sortOn(["name", "age"], Array.DESCENDING | Array.NUMERIC);


