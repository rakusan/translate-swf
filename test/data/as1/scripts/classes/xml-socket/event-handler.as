/*
 * Test the constructor.
 */
 
doc = new XMLSocket();


doc.onClose = function() 
{
    trace("Connection closed.");
};

doc.onConnect = function(success) 
{
    trace("Connection opened.");
};

doc.onData = function(doc) 
{
    trace("File loaded.");
};

doc.onXML = function(doc) 
{
    trace("XML Parsed.");
};

