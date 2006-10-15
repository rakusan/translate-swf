// TextField Stylesheet methods

stylesheet = new TextField.StyleSheet();

stylesheet.onLoad = function(success) 
{
    if (success) 
    {
        trace("Stylesheet loaded");
        
		styles = stylesheet.getStyleNames();
		
        trace(styles_array.join(newline));
    } 
};

stylesheet.load("styles.css");

stylesheet.getStyle("heading");

styleSheet.parseCSS(".heading { font-family: Arial; font-size: 12px }");

styleSheet.setStyle("heading", "{ font-family: Arial; font-size: 12px }");

stylesheet.transform(sheet.getStyle("heading"));

stylesheet.clear();