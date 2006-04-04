//
//  On.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  Button event statements.

/*
 * Test a typical on statement.
 */
 
on (press)
{
    a += 1;
}

/*
 * Test using each of the pre-defined button events
 */
 
on (press) { a += 1; }
on (release) { a += 1; }
on (releaseOutside) { a += 1; }
on (rollOver) { a += 1; }
on (rollOut) { a += 1; }
on (dragOver) { a += 1; }
on (keyPress "a") { a += 1; }
