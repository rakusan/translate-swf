/*
 * Test the methods with the implied this prefix.
 */

onClipEvent(mouseDown)
{

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

}
