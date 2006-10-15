// Handlers for responding to camera events

camera = Camera.get();

videoScreen.attachVideo(camera);

camera.onActivity = function(mode)
{
    trace(mode);
}

camera.onStatus = function(status) 
{
   if (status.code == "Camera.Muted")
   {
       trace("Camera not available");
   }
}
