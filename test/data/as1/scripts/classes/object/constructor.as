// Simple Objects

a = new Object();

a = new Object("Marie Celeste");


// More complex objects

function Ship(name)
{
    this.getName = function()
    {
        return this.name;
    };
    
    this.getCaptain = function()
    {
        return this.captain;
    };
    
    this.setCaptain = function(name)
    {
        this.captain = name;
    };
    
    this.getCrew = function()
    {
        return this.crew;
    };
    
    this.setCrew = function(crew)
    {
        this.crew = crew;
    };
    
    this.toString = function()
    {
        return "Name: "+this.name+"; Captain: "+captain;
    };
    
    this.name = name;
    this.addProperty("name", this.getName, null);
    this.addProperty("name", this.getCaptain, this.setCaptain);
}

// Registering Classes

Object.registerClass("SailingShip", Ship);


