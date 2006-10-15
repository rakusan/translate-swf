// event handler for menu items

function handler(obj, item) 
{
	trace(item.caption);
}

menu = new ContextMenu();

start = new ContextMenuItem("Start");
start.onSelect = handler;

menu.customItems.push(start);

stop = new ContextMenuItem("Stop");
stop.onSelect = handler;

menu.customItems.push(stop);

button.menu = menu;
