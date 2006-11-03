// Camera properties

camera = Camera.get();

cameras = Camera.names;            // list of installed cameras
index = Camera.index;              // index in Camera.names
name = Camera.name;                // name of camera

isActive = Camera.muted;           // is camera enabled

rate = Camera.currentFps;          // current frame rate
maxRate = Camera.fps;              // maximum frame rate possible

frameHeight = Camera.height;       // height of camera image
frameWidth = Camera.width;         // width of camera image

activity = camera.activityLevel;   // current level of actvity, range: 0..100

motion = camera.motionLevel;       // threshhold for registering activity, range: 0..100
timeout = camera.motionLevel;      // delay (milliseconds) before registering no motion

bandwidth = camera.bandwidth;      // available bandwidth in bytes/sec

quality = camera.quality;          // compression, range: 0..100