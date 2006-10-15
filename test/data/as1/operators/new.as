/*
 * create a built-in object
 */ 
x = new Array(8);

/*
 * create a user defined object
 */ 
x = new Object();

/*
 * Create a function
 */
 
function Ship(name, purpose)
{
    this.name = name;
    this.purpose = purpose;
}

x = new Ship("Marie Celeste", "GhostShip");

