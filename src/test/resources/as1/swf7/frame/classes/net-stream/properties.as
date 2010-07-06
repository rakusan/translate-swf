// create a new NetStream

conn = new NetConnection();
conn.connect(null);

stream = new NetStream(conn);

x = stream.bufferTime;
x = stream.bufferLength;
x = stream.bytesLoaded;
x = stream.bytesTotal;
x = stream.currentFps;
x = stream.time;
