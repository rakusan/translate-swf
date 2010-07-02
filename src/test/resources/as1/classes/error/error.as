// create an error

err = new Error("An error occurred");

// Access the properties

trace(err.name);
trace(err.message);

// Display the error

err.toString();