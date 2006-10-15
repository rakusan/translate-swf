// simple expressions

a = "http://www.flagstonesoftware.com";

getURL("http://www.flagstonesoftware.com");
getURL(a);

// Load web page into different browser windows

getURL("http://www.flagstonesoftware.com", _self);
getURL("http://www.flagstonesoftware.com", "_self");

getURL("http://www.flagstonesoftware.com", _blank);
getURL("http://www.flagstonesoftware.com", "_blank");

getURL("http://www.flagstonesoftware.com", _parent);
getURL("http://www.flagstonesoftware.com", "_parent");

getURL("http://www.flagstonesoftware.com", _top);
getURL("http://www.flagstonesoftware.com", "_top");

// submit variables using HTTP GET

package = "transform";
getURL("http://www.flagstonesoftware.com/cgi-bin/issue", _self, "GET");
getURL("http://www.flagstonesoftware.com/cgi-bin/issue", "_self", "get");

// submit variables using HTTP POST

package = "transform";
state = "start";

getURL("http://www.flagstonesoftware.com/cgi-bin/download", _self, "POST");
getURL("http://www.flagstonesoftware.com/cgi-bin/download", "_self", "post");

// call javascript function

getURL("javascript:alert('A message from Flash.')");
