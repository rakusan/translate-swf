/*
 * Listener handlers
 */

listener = new Object();

listener.onMouseDown = function() 
{
	trace("mouse button down");
};

listener.onMouseUp = function() 
{
	trace("mouse button up");
};

listener.onMouseMOve = function() 
{
	trace("mouse moving");
};

Mouse.addListener(listener);
Mouse.removeListener(listener);