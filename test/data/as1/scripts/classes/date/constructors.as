/*
 * Create a Date object representing the current date and time.
 */
 
a = new Date();

/*
 * Create a Date object specifying the number of milliseconds after the
 * start of time: 12.00am on Jan 1 1970 GMT.
 */
 
a = new Date(10000);

/*
 * Create a Date object specifying the relative year and month after 1900.
 */
 
a = new Date(2, 1);

/*
 * Create a Date object specifying the year and month.
 */
 
a = new Date(1902, 2);

/*
 * Create a Date object specifying the year, month and day.
 */
 
a = new Date(1902, 2, 28);

/*
 * Create a Date object specifying the year, month, day and hour.
 */
 
a = new Date(1902, 2, 28, 4);

/*
 * Test 7. Create a Date object specifying the year, month, day, hour and minute.
 */
 
a = new Date(1902, 2, 28, 4, 25);

/*
 * Create a Date object specifying the year, month, day, hour, minute and seconds.
 */
 
a = new Date(1902, 2, 28, 4, 25, 45);

/*
 * Check the UTC method.
 */

a = Date.UTC(2002, 9, 10, 12, 30, 23, 100);


