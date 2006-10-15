// properties

loader = new LoadVars();

// decode values 

loader.decode("name=John&age=29");

for (property in loader) 
{
    trace(loader[prop]);
}

// track progress of loader

this.createEmptyMovieClip("progress", 999);

progress.onEnterFrame = function() 
{
    bytesLoaded = loader.getBytesLoaded();
    bytesTotal = loader.getBytesTotal();
    
    if (bytesTotal != undefined) {
        trace("Loaded "+bytesLoaded+" of "+bytesTotal+" bytes.");
    }
};

loader.onLoad = function(success) 
{
    delete progress.onEnterFrame;

    if (success)
    {
       trace("Variables loaded.");
    } 
};

loader.load("http://www.flagstonesoftware.com/variables.txt");

// send variables

loader.name = "John";
loader.age = "29";

loader.send("http://www.flagstonesoftware.com", "_self", "GET");
loader.send("http://www.flagstonesoftware.com", "_blank", "POST");

result = new LoadVars();

loader.sendAndLoad("http://www.flagstonesoftware.com", result, "POST");

// to string

loader = new LoadVars();

loader.name = "John";
loader.age = "29";

loader.toString();


