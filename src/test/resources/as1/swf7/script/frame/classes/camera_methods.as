// Methods for controlling camera

cam = Camera.get();

// motion level thresholds

cam.setMotionLevel(50);
cam.setMotionLevel(50, 2000);

// set bandwidth and frame quality

cam.setQuality(8192, 0);    // Limit quality to maintain 8 KB/s bandwidth
cam.setQuality(0, 50);      // Adjust bandwidth to maintain quality
cam.setQuality(16484, 50);  // Limit bandwidth and quality

// Set camera operating parameters

cam.setMode(640, 480, 24);        // 640x480 frame, 24 frames/sec
cam.setMode(640, 480, 24, true);  // prefer 640x480 at frame rate's expense