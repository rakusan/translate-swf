/*
 * Methods for drawing with movie clips
 */
 
this.createEmptyMovieClip("shape", this.getNextHighestDepth());

shape.lineStyle(1, 0x000000, 100);

shape.beginFill(0xCC9966, 50);

shape.moveTo(0, 0);
shape.lineTo(100, 0);
shape.curveTo(600, 500, 600, 400);
shape.lineTo(0, 100);
shape.lineTo(0, 0);

shape.endFill();
shape.clear();

/*
 * creating a linear or radial fill.
 */

colors = [0xFF0000, 0x0000FF];
alphas = [100, 100];
ratios = [0, 0xFF];
matrix = {a:200, b:0, c:0, d:0, e:200, f:0, g:200, h:200, i:1};

shape.beginGradientFill("linear", colors, alphas, ratio, matrix);
shape.beginGradientFill("radial", colors, alphas, ratio, matrix);