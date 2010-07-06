// SharedObject methods

share = SharedObject.getLocal("obj", "/shares");

size = share.getSize();

share.flush(1024);

share.clear();