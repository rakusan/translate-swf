// Load a movie clip to a specific level

loadMovieNum("clip.swf", 9);

playButton.onRelease = function() 
{
   _level9.gotoAndPlay(0);
};

stopButton.onRelease = function() 
{
   _level9.stop();
};