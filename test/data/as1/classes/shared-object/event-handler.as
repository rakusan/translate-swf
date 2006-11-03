// SharedObject methods

share = SharedObject.getLocal("obj", "/shares");

share.onStatus = function(info) 
{
	if (info.level == "Status")
	{
	    if (info.code == "SharedObject.Flush.Success")
	    {
	        trace("Object saved");
	    }
	}
	if (info.level == "Error")
	{
	    if (info.code == "SharedObject.Flush.Failed")
	    {
	        trace("Cannot write shared object");
	    }
	}
};