// apply arguments to a function

function show() 
{
    for (i=0; i<arguments.length; i++)
    {
        trace(arguments[i]);
    }
}

show.call(null, 1, 2, 3);

// passing an object 

function show() 
{
    trace(val);

    for (i=0; i<arguments.length; i++)
    {
        trace(arguments[i]);
    }
}

obj = new Object();
obj.val = 1;

show.call(obj, "a", "b", "c");

