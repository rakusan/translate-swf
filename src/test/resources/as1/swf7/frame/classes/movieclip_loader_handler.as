// event handlers

listener = new Object();

listener.onLoadInit = function(clip) 
{
    trace("Initialising load.");
};

listener.onLoadStart = function(clip) 
{
    trace("Starting load.");
};

listener.onLoadProgress = function(clip, bytes, total) 
{
    trace(bytes+" loaded.");
};

listener.onLoadComplete = function(clip) 
{
    trace("Clip loaded.");
};

listener.onLoadError = function(clip, error) 
{
    trace("An error occurred.");
};


this.createEmptyMovieClip("clip", this.getNextHighestDepth());

loader = new MovieClipLoader();

loader.addListener(listener);

loader.loadClip("http://www.flagstonesoftware.com/clip.swf", "clip");

loader.removeListener(listener);
