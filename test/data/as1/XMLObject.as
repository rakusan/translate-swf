//frame
//
//  XML.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  XML Object.

/*
 * Test the constructors.
 */
 
a = new XML();
a = new XML("variables.xml");

/*
 * Test the object read-only properties.
 */
 
a.docTypeDecl;
a.firstChild;
a.lastChild;
a.loaded;
a.nextSibling;
a.nodeName;
a.nodeType;
a.nodeValue;
a.parentNode;
a.previousSibling;
a.status;
a.xmlDecl;

a.attributes;
a.childNodes;

/*
 * Test the object read-write properties.
 */

a = new XML();
 
function loadedXML() {}

a.onLoad = loadedXML;


/*
 * Test the object methods.
 */
 
a.appendChild(element);
a.cloneNode(true);
a.createElement("elementName");
a.createTextNode("elementName");
a.hasChildNodes();
a.insertBefore(newNode, existingNode);
a.load("variables.xml");
a.onLoad(result);
a.parseXML("variables.xml");
a.removeNode();

a.send("variables.xml");
a.send("variables.xml", _self);

a.sendAndLoad("serverScript", xmlObject);
a.toString();
