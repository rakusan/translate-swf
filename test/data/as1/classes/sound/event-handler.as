/*
 * Sound object event handlers

snd = new Sound();

snd.onID3 = function() 
{
    trace("onID3 event");
    
    for (var prop in this.id3) 
    {
        trace(prop + " = " + this.id3[prop]);
    }
};

snd.onLoad = function(loaded) 
{
    if (loaded) 
    {
        snd.start();
    } 
    else 
    {
        trace("Could not load sound");
    }
};

snd.onSoundComplete = function() 
{
    trace("Sound finished");
};