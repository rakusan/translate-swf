// Handlers for responding to camera events

cam = Camera.get();

videoScreen.attachVideo(cam);

cam.onActivity = function(mode)
{
    trace(mode);
};

cam.onStatus = function(status) 
{
   if (status.code == "Camera.Muted")
   {
       trace("Camera not available");
   }
};
