/*
 * Listener
 */

listener = new Object();

listener.onSetFocus = function(previous, next) 
{
    previous.border = false;
    next.border = true;
};

Selection.addListener(listener);
Selection.removeListener(listener);
