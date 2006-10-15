/*
 * Listener
 */

listener = new Object();

listener.onSetFocus = function(old, new) 
{
    old.border = false;
    new.border = true;
};

Selection.addListener(listener);
Selection.removeListener(listener);
