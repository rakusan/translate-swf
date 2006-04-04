//frame
//
//  LoadVariables.as
//  Translate
//
//  Created by smackay on Wed Oct 9 2002.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
// 
//  LoadVariables is used to load variables from a file.
//
//  Syntax: loadVariables(url [, target/level [, get|post]]);

// Levels

loadVariables("Data.swf", 1);
loadVariables("Data.swf", 1, "GET");
loadVariables("Data.swf", 1, "get");
loadVariables("Data.swf", 1, "POST");
loadVariables("Data.swf", 1, "post");

loadVariables("Data.swf", a);

// Target Reserved Names

loadVariables("Data.swf", "_blank");
loadVariables("Data.swf", "_self");
loadVariables("Data.swf", "_parent");
loadVariables("Data.swf", "_top");

// Targets

loadVariables("Data.swf", _root);
loadVariables("Data.swf", _level1);

loadVariables("Data.swf", "Clip");
loadVariables("Data.swf", "Clip", "get");
loadVariables("Data.swf", "Clip", "post");

loadVariables("Data.swf", a + b, "post");
loadVariables("Data.swf", a + b, "post");
loadVariables("Data.swf", a + b, "post");
