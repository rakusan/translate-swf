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


function menuHandler(obj, menu) 
{
    if(obj instanceof MovieClip) {
        trace("Movie clip: " + obj);
    }
    else if(obj instanceof TextField) {
        trace("Text field: " + obj);
    }
    else if(obj instanceof Button) {
        trace("Button: " + obj);
    }
}

menu.onSelect = menuHandler;

button.menu = menu;
clip.menu = menu;
textField.menu = menu;