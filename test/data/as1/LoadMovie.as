//frame
//
//  LoadMovie.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
// 
//  LoadMovie is used to load a Flash movie.
//
//  Syntax: loadMovie(url, target/level [, get|post]);

// Levels

loadMovie("Data.swf", 1);
loadMovie("Data.swf", 1, "GET");
loadMovie("Data.swf", 1, "get");
loadMovie("Data.swf", 1, "POST");
loadMovie("Data.swf", 1, "post");

loadMovie("Data.swf", a);

// Target Reserved Names

loadMovie("Data.swf", "_blank");
loadMovie("Data.swf", "_self");
loadMovie("Data.swf", "_parent");
loadMovie("Data.swf", "_top");

// Targets

loadMovie("Data.swf", _root);
loadMovie("Data.swf", _level1);

loadMovie("Data.swf", "Clip");
loadMovie("Data.swf", "Clip", "get");
loadMovie("Data.swf", "Clip", "post");

loadMovie("Data.swf", a + b, "post");
loadMovie("Data.swf", a + b, "post");
loadMovie("Data.swf", a + b, "post");
