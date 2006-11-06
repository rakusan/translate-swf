// class methods

Accessibility.isAction();
Accessibility.updateProperties();

// defining accessibility properties for the entire movie

_accProps.silent = false;
_accProps.forceSimple = false;
_accProps.name = "Accessibility";
_accProps.description = "Test script for Accessbility class.";

Accessbility.updateProperties();

delete _accProps;

// defining accessibility properties for a button

button._accProps.silent = false;
button._accProps.name = "Load";
button._accProps.description = "Load a new movie clip.";
button._accProps.shortcut = "Ctrl+L";

Accessbility.updateProperties();

delete button._accProps;

// defining accessibility properties for a movie clip

clip._accProps.silent = false;
clip._accProps.forceSimple = false;
clip._accProps.name = "Animation";
clip._accProps.description = "Simple animation.";
clip._accProps.shortcut = "Ctrl+A";

Accessbility.updateProperties();

delete clip._accProps;

