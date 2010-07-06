// Stage properties

Stage.align;      // T, B, L, R, TL, TR, BL, BR
Stage.height;     // height in pixels
Stage.width;      // width in pixels
Stage.scaleMode;  // "exactFit", "showAll", "noBorder", "noScale"
Stage.showMenu;   // true, false

// Stage listener

listener = new Object();

listener.onResize = function() {
    trace("new width: "+Stage.width+", height:"+Stage.height);
};

Stage.addListener(listener);
Stage.removeListener(listener);