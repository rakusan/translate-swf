// LoadMovieClip methods

this.createEmptyMovieClip("clip", this.getNextHighestDepth());

loader = new MovieClipLoader();

loader.loadClip("http://www.flagstonesoftware.com/clip.swf", "clip");

loader.getProgress("clip");

loader.unloadClip("clip");

