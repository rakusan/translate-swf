// TextFormat Methods

format = new TextFormat();
wrapWidth = 50;

properties = format.getTextExtent("This is a string");
properties = format.getTextExtent("This is a string", wrapWidth);

properties.width;
properties.height;
properties.ascent;
properties.descent;
properties.textFieldHeight; 
properties.textFieldWidth;
