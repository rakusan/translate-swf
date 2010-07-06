// context menu items

separatorBefore = new ContextMenuItem("Cut", menuHandler, true);

enabled = new ContextMenuItem("Copy", menuHandler, false, true);

visible = new ContextMenuItem("Paste", menuHandler, false, true, false);

// copy a menu item

copy = enabled.copy();

// menu item properties

menu = new ContextMenu();

menu.customItems.push(new ContextMenuItem("Cut", handler));
menu.customItems.push(new ContextMenuItem("Copy", handler));
menu.customItems.push(new ContextMenuItem("Paste", handler));

function handler(obj, item) 
{
	trace(item.caption+":");
	
	if (item.enabled) 
	{
	    trace(" enabled");
	}
	if (item.separatorBefore) 
	{
	    trace(", has separator");
	}
	if (item.separatorBefore) 
	{
	    trace(", visible");
	}
}
