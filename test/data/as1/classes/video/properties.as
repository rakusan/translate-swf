// Video properties

camera = Camera.get();

screen.attachVideo(camera);

screen.deblocking = 0; // default, apply deblocking as required
screen.deblocking = 1; // do not use deblocking
screen.deblocking = 2; // always use deblocking

screen.height;
screen.width;

screen.smoothing = true;