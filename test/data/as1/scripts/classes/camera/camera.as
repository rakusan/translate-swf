// getting the installed camera

cam = Camera.get();
cam = Camera.get(1);

// choose from the installed set of cameras

cameras = Camera.names;
cameraPanel = 3;    

if (cameras.length == 1)
{
    cam = Camera.get();
}
else 
{
	System.showSettings(cameraPanel);
	cam = Camera.get();
}