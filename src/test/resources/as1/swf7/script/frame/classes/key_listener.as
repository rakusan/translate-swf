// Listener for key strokes

listener = new Object();

listener.onKeyDown = function () 
{
    trace ("key pressed: "+Key.getAscii());
};

listener.onKeyUp = function () 
{
    trace ("key released: "+Key.getAscii());
};

Key.addListener(listener);

// remove the listener object

Key.removeListener(listener);