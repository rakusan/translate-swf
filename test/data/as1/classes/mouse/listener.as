/*
 * Listener handlers
 */

listener = new Object();

listener.onMouseDown = function() 
{
	trace("mouse button down);
};

listener.onMouseUp = function() 
{
	trace("mouse button up);
};

listener.onMouseMOve = function() 
{
	trace("mouse moving);
};

listener.onMouseWheel = function(delta, target) 
{
	trace("mouse button down);
};

Mouse.addListener(listener);
Mouse.removeListener(listener);