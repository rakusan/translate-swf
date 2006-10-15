// create a new NetStream

conn = new NetConnection();
conn.connect(null);

stream = new NetStream(conn);

stream.setBufferTime(5);

screen.attachVideo(stream);

stream.play("video.flv");
stream.pause(false);
stream.seek(10);
stream.pause(true);
stream.close();