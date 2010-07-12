// TextField Listeners

clip.createTextField("title", this.getNextHighestDepth(), 100, 100, 250, 25);

listener = new Object();

listener.onChanged = function()
{
	trace("text field changed");
};

listener.onScroller = function(field)
{
	trace("text field changed");
};

title.addListener(listener);
title.removeListener(listener);