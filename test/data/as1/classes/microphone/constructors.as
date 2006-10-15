// getting the installed Microphone

mic = Microphone.get();
mic = Microphone.get(1);

// choose from the installed set of Microphone

mics = Microphone.names;

for (i=0; i<mics.length; i++) 
{
    trace("["+i+"] "+mics[i]);
}