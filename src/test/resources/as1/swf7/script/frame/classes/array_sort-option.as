/*
 * Test the sort method.
 */

a = [3, 1, 5, 6, 4, 2];

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


