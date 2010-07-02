// TextField Methods

clip.createTextField("title", this.getNextHighestDepth(), 100, 100, 250, 25);

fonts = title.getFontList();
depth = title.getDepth();
format = title.getNewTextFormat();

format = title.getTextFormat();
format = title.getTextFormat(3);
format = title.getTextFormat(0, 5);

title.removeTextField();
title.replaceSel("New Characters");
title.replaceText(0, n, "replacement string");

title.setNewTextFormat(format);

format = title.setTextFormat(format);
format = title.setTextFormat(3, format);
format = title.setTextFormat(0, 5, format);
