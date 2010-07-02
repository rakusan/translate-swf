/*
 * Test the object methods.
 */
 
doc = new XML("variables.xml");

// set headers individually

doc.addRequestHeader("Content-Type", "text/plain");
doc.addRequestHeader("X-Client-Version", "2.1.4");

// set headers using an array

headers = ["Content-Type", "text/plain", "X-Client-Version", "2.1.4"];

doc.addRequestHeader(headers);

