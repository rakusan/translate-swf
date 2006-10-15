/*
 * Test the constructors.
 */

a = new Boolean();
a = new Boolean(true);
a = new Boolean(false);
a = new Boolean(0);
a = new Boolean(1);
a = new Boolean("x");
a = new Boolean("0");
a = new Boolean(new Object());
a = new Boolean(null);

/*
 * Test the toString method.
 */

a = new Boolean(true);

a.toString();


/*
 * Test the valueOf method.
 */

a = new Boolean(true);

a.valueOf();
