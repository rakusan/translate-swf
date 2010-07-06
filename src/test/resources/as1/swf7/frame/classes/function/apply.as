// apply arguments to a function

function show() 
{
    trace(arguments);
}

var args = new Array(1,2,3);

show.apply(null,args);

// passing an object 

function show() 
{
    trace(val);
    trace(arguments);
}

obj = new Object();
obj.val = 1;

show.apply(obj,args);
