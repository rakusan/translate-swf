//
//
//  MovieClip.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  MovieClip Object.


/*
 * Test the methods with the specified movie clip.
 */
 
Clip.attachMovie(1, "Clip1", 1);
Clip.duplicateMovieClip("Clip2", 2);
Clip.getBounds(_root);
Clip.getBytesLoaded();
Clip.getBytesTotal();

Clip.getURL("http://www.flagstonesoftware.com");
Clip.getURL("http://www.flagstonesoftware.com", _self);
Clip.getURL("http://www.flagstonesoftware.com", _self, "get");
Clip.getURL("http://www.flagstonesoftware.com/cgi-bin/issue", _self, "post");

Clip.globalToLocal({x : _root._xmouse, y : _root._ymouse});

Clip.gotoAndPlay("Scene1", 1);
Clip.gotoAndPlay("Scene1", "Frame 1");

Clip.gotoAndStop("Scene1", 1);
Clip.gotoAndStop("Scene1", "Frame 1");

Clip.hitTest(100, 100, true);
Clip.hitTest(100, 100, false);
Clip.hitTest(Clip2);

Clip.loadMovie("Data.swf");
Clip.loadMovie("Data.swf", "get");
Clip.loadMovie("Data.swf", "POST");

Clip.loadVariables("variables.txt");
Clip.loadVariables("variables.txt", "get");
Clip.loadVariables("variables.txt", "POST");

Clip.localToGlobal({x : this._xmouse, y : this._ymouse});
Clip.nextFrame();
Clip.nextScene();
Clip.play();
Clip.prevFrame();
Clip.prevScene();
Clip.removeMovieClip();
Clip.startDrag(true);
Clip.startDrag(true, 0, 100, 100, 0);
Clip.stop();
Clip.stopDrag();
Clip.swapDepths(2);
Clip.swapDepths(Clip2);
Clip.unloadMovie();

/*
 * Test the methods with the implied this prefix.
 */
 
attachMovie(1, "Clip1", 1);
getBounds(_root);
getBytesLoaded();
getBytesTotal();

getURL("http://www.flagstonesoftware.com");
getURL("http://www.flagstonesoftware.com", _self);
getURL("http://www.flagstonesoftware.com", _self, "get");
getURL("http://www.flagstonesoftware.com/cgi-bin/issue", _self, "post");

globalToLocal({x : _root._xmouse, y : _root._ymouse});
gotoAndPlay("Scene1", 1);
gotoAndPlay("Scene1", "Frame 1");

gotoAndStop("Scene1", 1);
gotoAndStop("Scene1", "Frame 1");

hitTest(100, 100, true);
hitTest(100, 100, false);
hitTest(Clip2);

loadMovie("Data.swf", "get");
loadMovie("Data.swf", "POST");

loadVariables("variables.txt", "get");
loadVariables("variables.txt", "POST");

localToGlobal({x : this._xmouse, y : this._ymouse});
nextFrame();
nextScene();
play();
prevFrame();
prevScene();
stop();
stopDrag();
swapDepths(2);
swapDepths(Clip2);
