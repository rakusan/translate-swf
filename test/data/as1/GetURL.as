//frame
//
//  GetURL.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
// 
//  GetURL is used to display a web page in a browser.
//
//  Syntax: getURL(url [, window [, get|post]]);

a = "http://www.flagstonesoftware.com";

getURL("http://www.flagstonesoftware.com");
getURL(a);

getURL("http://www.flagstonesoftware.com", _self);
getURL("http://www.flagstonesoftware.com", "_self");

getURL("http://www.flagstonesoftware.com", _blank);
getURL("http://www.flagstonesoftware.com", "_blank");

getURL("http://www.flagstonesoftware.com", _parent);
getURL("http://www.flagstonesoftware.com", "_parent");

getURL("http://www.flagstonesoftware.com", _top);
getURL("http://www.flagstonesoftware.com", "_top");


package = "transform";
getURL("http://www.flagstonesoftware.com/cgi-bin/issue", _self, "GET");
getURL("http://www.flagstonesoftware.com/cgi-bin/issue", "_self", "get");

package = "transform";
state = "start";

getURL("http://www.flagstonesoftware.com/cgi-bin/download", _self, "POST");
getURL("http://www.flagstonesoftware.com/cgi-bin/download", "_self", "post");
