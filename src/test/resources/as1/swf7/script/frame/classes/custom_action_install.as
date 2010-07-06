// Install a custom action

doc = new XML();

doc.ignoreWhite = true;

doc.onLoad = function(success) 
{
    CustomActions.install("class", this.firstChild);
};

doc.load("action.xml");