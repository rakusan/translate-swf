/* TextFormat Constructors

TextFormat([font:String, [size:Number, [color:Number, [bold:Boolean,
[italic:Boolean, [underline:Boolean, [url:String, [target:String,
[align:String, [leftMargin:Number, [rightMargin:Number, [indent:Number,
[leading:Number]]]]]]]]]]]]])

*/

format = new TextFormat();
format = new TextFormat("Arial");
format = new TextFormat("Arial", 12);
format = new TextFormat("Arial", 12, 0x000000);
format = new TextFormat("Arial", 12, 0x000000, true);
format = new TextFormat("Arial", 12, 0x000000, true, true);
format = new TextFormat("Arial", 12, 0x000000, true, true);
format = new TextFormat("Arial", 12, 0x000000, true, true, "http://www.flagstonesoftware.com");
format = new TextFormat("Arial", 12, 0x000000, true, true, "http://www.flagstonesoftware.com", "_self");
format = new TextFormat("Arial", 12, 0x000000, true, true, "http://www.flagstonesoftware.com", "_self", "left");
format = new TextFormat("Arial", 12, 0x000000, true, true, "http://www.flagstonesoftware.com", "_self", "left", 20);
format = new TextFormat("Arial", 12, 0x000000, true, true, "http://www.flagstonesoftware.com", "_self", "left", 20, 20);
format = new TextFormat("Arial", 12, 0x000000, true, true, "http://www.flagstonesoftware.com", "_self", "left", 20, 20, 20);
format = new TextFormat("Arial", 12, 0x000000, true, true, "http://www.flagstonesoftware.com", "_self", "left", 20, 20, 20, 10);
