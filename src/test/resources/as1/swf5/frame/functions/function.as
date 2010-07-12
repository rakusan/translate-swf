/*
 * Define a simple function
 */ 

a = 0;

function a() 
{
    a += 1;
}

a();

/*
 * Define an empty function
 */ 

a = 0;

function a() 
{
}

a();

/*
 * Define a simple function that accepts arguments
 */
 
a = 1;
b = 2;

function addition(a, b)
{
    c = a + b;
}

addition(a, b);

/*
 * Define a simple function that accepts arguments and uses an empty return
 */
 
a = 1;
b = 2;

function addition(a, b)
{
    c = a + b;
    
    return;
}

addition(a, b);

/*
 * Define a simple function that accepts arguments and returns a value
 */
 
a = 1;
b = 2;

function addition(a, b)
{
    return a + b;
}

addition(a, b);

/*
 * Define nested functions
 */
 
a = 1;
b = 2;

function mul(a, b)
{
//TODO    function addition(a, b)
//TODO    {
//TODO        return a + b;
//TODO    }
    
    product = 0;
    
    for (i=0; i<b; i++)
        product = addition(product, a);
        
    return product;
}

mul(a, b);

/*
 * Define a method on an object
 */
 
a = 1;
b = 2;

a = new Object();

a.addition = function(a, b) { return a + b; };

d.addition(a, b);

/*
 * Create a new object from a function.
 */
 
function Ship(name, purpose)
{
    this.name = name;
    this.purpose = purpose;
}

aShip = new Ship("Marie Celeste", "GhostShip");

/*
 * Evaluate a function name
 */ 
 
function echo(aString)
{
    trace(aString);
}

//TODO this["echo"]("A Message");
//TODO this[ value + "func"]();

//TODO eval("echo")("A Message");
//TODO eval( value + "func")();

//TODO eval(a)("A Message");