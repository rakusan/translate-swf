// context menu

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

button.menu = menu;
button.trackAsMenu = true;