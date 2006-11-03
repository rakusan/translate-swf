// object constructor

a = new PrintJob();

a.start();

a.addPage(0, {xMin:0,xMax:640,yMin:0,yMax:480});

a.send();