// Microphone properties

mic = Microphone.get();

mics = Microphone.names;               // list of installed mics
index = Microphone.index;              // index in Microphone.names
name = Microphone.name;                // name of mic

isActive = Microphone.muted:           // is mic enabled

rate = Microphone.rate;                // current sampling rate in KHz

activity = mic.activityLevel;          // sound level, range: 0..100
gain = mic.gain;                       // gain, range: 0..100

silenceLevel = mic.silenceLevel;       // threshold to activate microphone, range: 0..100
silenceTimeout = mic.silenceTimeout;   // delay (milliseconds) before registering no sound
echoCancel = mic.useEchoSuppression;  // use noise cancelling, true || false  
