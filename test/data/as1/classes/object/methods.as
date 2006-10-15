// Object methods

function Ship(name)
{
    this.setCrew = function(crew)
    {
        this.crew = crew;
    }

    this.toString()
    {
        return "Name: "+this.name+";
    }

    this.name = name;
}

a = new Ship("Marie Celeste");

a.toString();

a.valueOf();

// monitoring an object

function writeStory(author)
{
    if (author == "Conan Doyle")
    {
         trace("Mystery"):
    }
}

a.watch("crew", writeStory, "Conan Doyle"):

a.setCrew(null);

a.unwatch("crew");

