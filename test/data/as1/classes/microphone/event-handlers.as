// Handlers for responding to mic events

mic = Microphone.get();

soundClip.attachAudio(mic);

mic.onActivity = function(mode)
{
    trace(mode);
}

mic.onStatus = function(status) 
{
   if (status.code == "Microphone.muted")
   {
       trace("Microphone not available");
   }
}
