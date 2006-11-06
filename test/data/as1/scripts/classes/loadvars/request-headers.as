// create a new variable loaders

loader = new LoadVars();

// set headers individually

loader.addRequestHeader("Content-Type", "text/plain");
loader.addRequestHeader("X-Client-Version", "2.1.4");

// set headers using an array

headers = ["Content-Type", "text/plain", "X-Client-Version", "2.1.4"];

loader.addRequestHeader(headers);

