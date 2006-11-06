// Stage listener

listener = new Object();

listener.onResize = function() 
{
    trace("new width: "+Stage.width+", height:"+Stage.height);
};

Stage.addListener(listener);
Stage.removeListener(listener);