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
