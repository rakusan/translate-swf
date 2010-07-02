#define DIRECTIVE 1

#ifdef DIRECTIVE
a = 1;
b = 2;
#else
a = 3;
b = 4;
#endif

#ifndef DIRECTIVE
c = 1;
d = 2;
#else
c = 3;
d = 4;
#endif
