// Simple use of setInterval(), clearInterval()

function handler() 
{
    trace("interval called");
}

id = setInterval(handler, 1000);

clearInterval(id);