// Methods for controlling microphone

mic = Microphone.get();

// sound level thresholds

mic.setSilenceLevel(50, 2000);
mic.setUseEchoSuppression(true);

mic.setGain(50);
mic.setRate(11);

