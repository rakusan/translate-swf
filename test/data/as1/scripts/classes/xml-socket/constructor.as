/*
 * Test the constructor.
 */
 
doc = new XMLSocket();

doc.connect("xmlserver.flagstonesoftware.com", 556);
 
doc.send(xmlObject);

doc.close();
