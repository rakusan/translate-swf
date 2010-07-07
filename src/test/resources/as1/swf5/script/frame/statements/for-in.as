/*
 * Test a typical for..in loop
 */
 
a = new Object();

a.x = "x";
a.y = "y";
a.z = "z";

for (i in a)
{
    x = eval("a." + i);
}

/*
 * Test a for..in loop with a single statement
 */
 
a = new Object();

a.x = "x";
a.y = "y";
a.z = "z";

for (i in a)
    x = eval("a." + i);

/*
 * Test a for..in loop with an empty statement
 */
 
a = new Object();

a.x = "x";
a.y = "y";
a.z = "z";

for (i in a)
    ;
    
/*
 * Test nested for..in loops
 */
 
a = new Object();

a.x = "x";
a.y = "y";
a.z = "z";

for (i in a)
{
    b = new Object();

    b.x = "x";
    b.y = "y";
    b.z = "z";

    for (j in b)
    {
        y = eval("a." + i);
    }
    
    x = eval("a." + i);
}

/*
 * Test for..in loop with break and continue
 */
 
a = new Object();

a.x = "x";
a.y = "y";
a.z = "z";

for (i in a)
{
    if (eval("a." + i) == "y")
        break;
    else 
        continue;

    x = eval("a." + i);
}

/*
 * Test nested for..in loops with break and continue
 */
 
a = new Object();

a.x = "x";
a.y = "y";
a.z = "z";

for (i in a)
{
    b = new Object();

    b.x = "x";
    b.y = "y";
    b.z = "z";

    for (j in b)
    {
        if (eval("b." + i) == "y")
            break;
        else 
            continue;

        y = eval("b." + i);
    }
    x = eval("a." + i);
}

;