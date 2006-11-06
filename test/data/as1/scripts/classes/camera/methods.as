// Methods for controlling camera

camera = Camera.get();

// motion level thresholds

camera.setMotionLevel(50);
camera.setMotionLevel(50, 2000);

// set bandwidth and frame quality

camera.setQuality(8192, 0);    // Limit quality to maintain 8 KB/s bandwidth
camera.setQuality(0, 50);      // Adjust bandwidth to maintain quality
camera.setQuality(16484, 50);  // Limit bandwidth and quality

// Set camera operating parameters

camera.setMode(640, 480, 24);        // 640x480 frame, 24 frames/sec
camera.setMode(640, 480, 24, true);  // prefer 640x480 at frame rate's expense