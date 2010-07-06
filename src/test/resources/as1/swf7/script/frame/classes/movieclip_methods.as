/*
 * Test the methods with the specified movie clip.
 */

clip.attachMovie("clipa", "subclip", this.getNextHighestDepth());
clip.attachMovie("clipb", "subclip", this.getNextHighestDepth(), {_x:100, _y:100});

clip.duplicateMovieClip("subclip", this.getNextHighestDepth());
clip.duplicateMovieClip("subclip", this.getNextHighestDepth(), {_x:100, _y:100});

clip.getBounds(this);
clip.getBytesLoaded();
clip.getBytesTotal();

clip.getSWFVersion();

clip.getURL("http://www.flagstonesoftware.com");
clip.getURL("http://www.flagstonesoftware.com", _self);
clip.getURL("http://www.flagstonesoftware.com", _self, "get");
clip.getURL("http://www.flagstonesoftware.com/cgi-bin/issue", _self, "post");

clip.globalToLocal({x : _root._xmouse, y : _root._ymouse});

clip.gotoAndPlay(1);
clip.gotoAndPlay("Frame 1");

clip.gotoAndStop(1);
clip.gotoAndStop("Frame 1");

clip.hitTest(100, 100, true);
clip.hitTest(100, 100, false);
clip.hitTest(Clip2);

clip.loadMovie("Data.swf");
clip.loadMovie("Data.swf", "get");
clip.loadMovie("Data.swf", "POST");

clip.loadVariables("variables.txt");
clip.loadVariables("variables.txt", "get");
clip.loadVariables("variables.txt", "POST");

clip.localToGlobal({x : this._xmouse, y : this._ymouse});

clip.nextFrame();
clip.nextScene();
clip.play();
clip.prevFrame();
clip.prevScene();

clip.removeMovieClip();

clip.startDrag(true);
clip.startDrag(true, 0, 100, 100, 0);
clip.stop();
clip.stopDrag();

clip.swapDepths(2);
clip.swapDepths(Clip2);

clip.unloadMovie();

