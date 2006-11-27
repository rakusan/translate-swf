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


a.sortOn("name");
a.sortOn("age");
a.sortOn(["name", "age"]);

