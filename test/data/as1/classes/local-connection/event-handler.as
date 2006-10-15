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

sender.onStatus = function(status) 
{
    switch (status.level) 
    {
        case 'status' :
            trace("Connected.");
            break;
        case 'error' :
            trace("Error.");
            break;
    }
};

sender.send("connection", "methodToExecute", 5, 7);

receiver.close();
sender.close();


