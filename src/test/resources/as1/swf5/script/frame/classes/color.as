/*
 * Test the constructors.
 */
 
a = new Color(clip);

/*
 * Test the setRGB method.
 */
 
a = new Color(clip);

a.setRGB(0x00FF00);

/*
 * Test the getRGB method.
 */
 
a = new Color(clip);

trace(a.getRGB());

/*
 * Test the setTransform method.
 */
 
transform = new Object();

transform.ra = 100;
transform.rb = 255;
transform.ga = 30;
transform.gb = 7;
transform.ba = -50;
transform.bb = -200;
transform.aa = 100;
transform.ab = 25;

a.setTransform(transform);

/*
 * Test the getTransform method.
 */
 
anObject = a.getTransform();

anObject.ra;
anObject.rb;
anObject.ga;
anObject.gb;
anObject.ba;
anObject.bb;
anObject.aa;
anObject.ab;
