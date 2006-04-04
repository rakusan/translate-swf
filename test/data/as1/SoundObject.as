//frame
//
//  Sound.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//  Sound Object.

/*
 * Test the constructors
 */

a = new Sound();
a = new Sound(Clip);

/*
 * Test the methods
 */

a.attachSound("Sound1");
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
a.stop("Sound1");
