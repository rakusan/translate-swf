// create a context menu

function copy(menu, obj) {
	trace("cut");
}

function cut(menu, obj) {
	trace("copy");
}

function paste(menu, obj) {
	trace("paste");
}

menu = new ContextMenu();

menu.customItems.push(new ContextMenuItem("Cut", cut));
menu.customItems.push(new ContextMenuItem("Copy", copy));
menu.customItems.push(new ContextMenuItem("Paste", paste));

// Disable all built-in items

menu.hideBuiltInItems();

// Enable built-in items

properties = menu.builtInItems;

properties.save = true;
properties.zoom = true;
properties.quality = true;
properties.play = true;
properties.loop = true;
properties.rewind = true;
properties.forward_back = true;
properties.print = true;


// assign the menu to an object

button.menu = menu;

// create a copy

clip.menu = menu.copy();