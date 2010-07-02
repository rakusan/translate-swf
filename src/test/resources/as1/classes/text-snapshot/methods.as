// TextSnapshot Methods

text = clip.getTextSnapshot();


pos = text.findText(0, "www", true);

count = text.getCount();

isSelected = text.getSelected(0, text.getCount()-1);

str = text.getSelectedText(false);

str = text.getText(0, text.getCount()-1, true);

index = text.hitTestTextNearPos(_xmouse, _ymouse, 0);

text.setSelectColor(0xCC9966);

text.setSelected(0, text.getCount()-1, true);