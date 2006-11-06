// Install a custom action

xml = new XML();

xml.ignoreWhite = true;

xml.onLoad = function(success) 
{
    CustomActions.install("class", this.firstChild);
};

xml.load("action.xml");