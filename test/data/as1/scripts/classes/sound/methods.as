/*
 * Test the methods
 */

a = new Sound();

a.attachSound("bell");

a.getPan();
a.getTransform();
a.getVolume();
a.setPan(0);
a.setTransform({ll:100, lr:30, rr:40, rl:-50});
a.setVolume(50);

a.start();
a.start(1);
a.start(5, 1);

a.stop();
a.stop("bell");

a.getBytesLoaded();
a.getBytesTotal();

a.loadSound("event.mp3", false);
a.loadSound("stream.mp3", true);
