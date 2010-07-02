// create a new NetStream

conn = new NetConnection();
conn.connect(null);

stream = new NetStream(conn);

stream.onStatus = function(info) 
{
	if (info.level == "status")
	{
	    if (info.code == "NetStream.Buffer.Empty")
	    {
	        trace("Buffer Empty");
	    }
	    else if (info.code == "NetStream.Buffer.Full")
	    {
	        trace("Buffer Full");
	    }
	    else if (info.code == "NetStream.Play.Start")
	    {
	        trace("Playing");
	    }
	    else if (info.code == "NetStream.Play.Stop")
	    {
	        trace("Stopped");
	    }
	}
	if (info.level == "error")
	{
	    if (info.code == "NetStream.Play.StreamNotFound")
	    {
	        trace("Cannot find file to play");
	    }
	}
};