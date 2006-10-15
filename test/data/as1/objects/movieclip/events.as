//
//  MovieClipFunctions.as
//  Translate
//
//  Created by smackay on Mon Nov 24 2003.
//  Copyright (c) 2002 Flagstone Software Ltd. All rights reserved.
//
//

onClipEvent(mouseDown) 
{
    startDrag(this);
}

onClipEvent(mouseUp) 
{
    stopDrag();
}
