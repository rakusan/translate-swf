//frame
//
//  Date.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  Date Object.

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
trace(a.toString());

/*
 * Test 7. Create a Date object specifying the year, month, day, hour and minute.
 */
 
a = new Date(1902, 2, 28, 4, 25);

/*
 * Create a Date object specifying the year, month, day, hour, minute and seconds.
 */
 
a = new Date(1902, 2, 28, 4, 25, 45);

/*
 * Test the accessor methods.
 */
 
a = new Date();

a.getFullYear();
a.getMonth();
a.getDate();
a.getDay();
a.getHours();
a.getMinutes();
a.getSeconds();
a.getMilliseconds();
a.getTime();
a.getYear();
a.getTimezoneOffset();

/*
 * Check the UTC accessor methods.
 */
 
a.getUTCFullYear();
a.getUTCMonth();
a.getUTCDate();
a.getUTCDay();
a.getUTCHours();
a.getUTCMinutes();
a.getUTCSeconds();
a.getUTCMilliseconds();

/*
 * TCheck the set methods.
 */
 
a = new Date();

a.setFullYear(1900);
a.setMonth(0);
a.setDate(0);
a.setHours(0);
a.setMinutes(0);
a.setSeconds(0);
a.setMilliseconds(0);

a.setTime(1000);
a.setYear(1);

/*
 * Check the UTC set methods.
 */

a = new Date();

a.setUTCFullYear(1900);
a.setUTCMonth(0);
a.setUTCDate(0);
a.setUTCHours(0);
a.setUTCMinutes(0);
a.setUTCSeconds(0);
a.setUTCMilliseconds(0);

/*
 * Check the UTC method.
 */

a = Date.UTC(2002, 9, 10, 12, 30, 23, 100);

