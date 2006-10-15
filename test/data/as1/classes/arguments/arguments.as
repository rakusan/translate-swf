// simple expressions

echo = function(x) 
{
	for (i=0; i<arguments.length; i++)
	{
		trace(arguments[i]);
	}
};

echo(1,2,3,4,5);


factorial = function(x) 
{
    if (x <= 1) 
    {
        return 1;
    } 
    else 
    {
        return x * arguments.callee(x-1);
    }
};

x = factorial(6);


child = function(x) 
{
	if (arguments.caller == null)
	{
	    trace("no parent function");
	}
};

child(1);