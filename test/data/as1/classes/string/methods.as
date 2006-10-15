/*
 * Test the methods
 */

a = new String("A String");

a.charAt(0);
a.charCodeAt(0);
a.concat("def");

space = String.fromCharCode(32);

a.indexOf("b");
a.indexOf("b", 1);

a.lastIndexOf("b");
a.lastIndexOf("b", 1);

a.slice(1, 2);
a.split("b");

a.substr(1);
a.substr(1, 1);

a.substring(1, 3);

a.toLowerCase();
a.toUpperCase();
