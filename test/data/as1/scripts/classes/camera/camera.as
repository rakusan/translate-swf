// getting the installed camera

camera = Camera.get();
camera = Camera.get(1);

// choose from the installed set of cameras

cameras = Camera.names;
cameraPanel = 3;    

if (cameras.length == 1)
{
    camera = Camera.get();
}
else 
{
	System.showSettings(cameraPanel);
	camera = Camera.get();
}