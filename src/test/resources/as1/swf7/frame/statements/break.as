
/*
 * Test a do..while loop with break.
 */
 
a = 0;
b = 10;

do {
    if (a == 5)
        break;
        
} while (++a < b);

/*
 * Test nested do..while loops with break.
 */
 
a = 0;
b = 10;

do {
    c = 0;
    d = 10;
    
    do {
        if (c == 5)
            break;
    } while (++c < d);

} while (++a < b);
