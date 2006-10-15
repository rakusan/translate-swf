/*
 * Test a typical do..while loop.
 */
 
a = 0;
b = 10;

do {
    a += 1;
} while (a < b);

/*
 * Test a do..while loop with a single statement.
 */
 
a = 0;
b = 10;

do
    a += 1;
while (a < b);

/*
 * Test a do..while loop with an empty statement.
 */
 
a = 0;
b = 10;

do
    ;
while (a < b);

/*
 * Test nested do..while loops.
 */
 
a = 0;
b = 10;

do {
    c = 0;
    d = 10;
    
    do {
        c += 1;
    } while (c < d);

    a += 1;
} while (a < b);

/*
 * Test a do..while loop with break and continue.
 */
 
a = 0;
b = 10;

do {
    if (a == 5)
        break;
    else
        continue;
        
    a += 1;
} while (a < b);

/*
 * Test nested do..while loops with break and continue.
 */
 
a = 0;
b = 10;

do {
    c = 0;
    d = 10;
    
    do {
        if (c == 5)
            break;
        else
            continue;

        c += 1;
    } while (c < d);

    a += 1;
} while (a < b);
