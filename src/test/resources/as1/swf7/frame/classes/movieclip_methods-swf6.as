/*
 * Test the methods with the specified movie clip.
 */

clip.attachAudio(Microphone.get());
clip.createEmptyMovieClip("subclip", this.getNextHighestDepth());
clip.createTextField("title", this.getNextHighestDepth(), 100, 100, 250, 25);
clip.getDepth();
clip.setMask(mc);

