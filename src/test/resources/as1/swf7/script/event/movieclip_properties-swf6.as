//  MovieClip Properties.

onClipEvent(mouseDown)
{

clip.enabled = true;
clip.focusEnabled = true;
clip._focusrect = true;
clip.hitArea = movie;

clip._highquality = 0;
clip._soundbuftime = 100;

clip.tabChildren = true;
clip.tabEnabled = true;
clip.tabIndex = 1;

clip.trackAsMenu = true;
clip.useHandCursor = true;
}
