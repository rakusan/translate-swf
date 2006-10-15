// event handlers

loader = new LoadVars();

loader.onData = function(source) 
{
    if (source == undefined) 
    {
		trace("Error loading variables.");
    }
};

loader.onLoad = function(result) 
{
    if (result == true) 
    {
		trace("Variables loaded.");
    }
};

loader.load("http://www.flagstonesoftware.com/variables.txt");