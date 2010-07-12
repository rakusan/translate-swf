/*
 * Test the methods
 */

a = new Sound();

a.attachSound("bell");

a.getBytesLoaded();
a.getBytesTotal();

a.loadSound("event.mp3", false);
a.loadSound("stream.mp3", true);
