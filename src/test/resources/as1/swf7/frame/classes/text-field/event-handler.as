// TextField Event Handlers

clip.createTextField("title", this.getNextHighestDepth(), 100, 100, 250, 25);

title.onChanged = function()
{
	trace("text field changed");
};

title.onScroller = function(field)
{
	trace("text field changed");
};

title.onSetFocus = function(field)
{
	trace("text field has focus");
};

title.onKillFocus = function(field)
{
	trace("text field lost focus");
};

