/*
 * Test the object methods.
 */
 
doc = new XML("variables.xml");

// Access node tree

doc.appendChild(element);
doc.cloneNode(true);

doc.createElement("elementName");
doc.createTextNode("elementName");

doc.getBytesLoaded();
doc.getBYtesTotal();

doc.hasChildNodes();

doc.insertBefore(newNode, existingNode);

doc.load("variables.xml");
doc.parseXML("variables.xml");

doc.removeNode();

doc.send("variables.xml");
doc.send("variables.xml", _self);

doc.sendAndLoad("serverScript", xmlObject);

doc.toString();
