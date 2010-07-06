// create a new local connection

// create the receiver

receiver = new LocalConnection();

receiver.methodToExecute = function(a, b) 
{
	trace("a = "+a);
	trace("b = "+b);
};

receiver.connect("conection");

// create the sender

sender = new LocalConnection();

sender.send("connection", "methodToExecute", 5, 7);

receiver.close();
sender.close();


