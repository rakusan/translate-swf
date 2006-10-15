/*
 * Test using each of the pre-defined button events
 */
 
on (press) { a += 1; }
on (release) { a += 1; }
on (releaseOutside) { a += 1; }
on (rollOver) { a += 1; }
on (rollOut) { a += 1; }
on (dragOut) { a += 1; }
on (dragOver) { a += 1; }
on (keyPress "a") { a += 1; }
