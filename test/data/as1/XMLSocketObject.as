//frame
//
//  XMLSocket.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  XMLSocket Object.

/*
 * Test the constructor.
 */
 
a = new XMLSocket();

/*
 * Test the methods.
 */
 
a.close();
a.connect("xmlserver.flagstonesoftware.com", 556);

function closeFunction() {}
a.onClose = closeFunction;
a.onClose();

function connectFunction() {}
a.onConnect = connectFunction;
a.onConnect(result);

function xmlFunction() {}
a.onXML = xmlFunction;
a.onXML();

a.send(xmlObject);
