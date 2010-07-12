// Object methods

function Ship(name)
{
    this.setCrew = function(crew)
    {
        this.crew = crew;
    };

    this.toString = function()
    {
        return "Name: "+this.name;
    };

    this.name = name;
}

a = new Ship("Marie Celeste");

a.toString();

a.valueOf();
