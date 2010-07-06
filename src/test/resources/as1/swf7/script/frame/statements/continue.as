/*
 * Test for loop with break and continue
 */
 
a = 0;

for (i=0; i<10; i++)
{
    if (i == 5) 
        break;
    else 
        continue;

    a += 1;
}

/*
 * Test a while loop with break and continue.
 */
 
a = 0;
b = 10;

while (a < b)
{
    if (a == 5)
        break;
    else
        continue;

    a += 1;
}
