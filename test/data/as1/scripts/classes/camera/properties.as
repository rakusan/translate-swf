// Camera properties

cam = Camera.get();

cameras = Camera.names;            // list of installed cameras
index = Camera.index;              // index in Camera.names
name = Camera.name;                // name of camera

isActive = Camera.muted;           // is camera enabled

rate = Camera.currentFps;          // current frame rate
maxRate = Camera.fps;              // maximum frame rate possible

frameHeight = Camera.height;       // height of camera image
frameWidth = Camera.width;         // width of camera image

activity = cam.activityLevel;   // current level of actvity, range: 0..100

motion = cam.motionLevel;       // threshhold for registering activity, range: 0..100
timeout = cam.motionLevel;      // delay (milliseconds) before registering no motion

bandwidth = cam.bandwidth;      // available bandwidth in bytes/sec

quality = cam.quality;          // compression, range: 0..100