package de.blablubbabc.paintball.melodies;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class LittleEndianStream {
	protected final DataInputStream dis;
	protected final InputStream is;
	protected final byte[] work;
	
	public LittleEndianStream(InputStream in) {
		this.is = in;
		this.dis = new DataInputStream(in);
		work = new byte[8];
	}

	public final void close() throws IOException {
		dis.close();
	}

	public final int read(byte ba[], int off, int len) throws IOException {
		return is.read(ba, off, len);
	}
	
	public final byte readByte() throws IOException {
		return dis.readByte();
	}

	public final void readFully(byte ba[]) throws IOException {
		dis.readFully(ba, 0, ba.length);
	}

	public final void readFully(byte ba[], int off, int len) throws IOException {
		dis.readFully(ba, off, len);
	}

	public final int readInt() throws IOException {
		dis.readFully(work, 0, 4);
		return (work[3]) << 24 | (work[2] & 0xff) << 16 | (work[1] & 0xff) << 8
				| (work[0] & 0xff);
	}

	public final short readShort() throws IOException {
		dis.readFully(work, 0, 2);
		return (short) ((work[1] & 0xff) << 8 | (work[0] & 0xff));
	}

	public final String readString()
			throws IOException {
		int length = readInt();
		int have = 0;
		byte[] buffer = new byte[length];
		while (have < length)
			have += read(buffer, have, length - have);
		return new String(buffer);
	}
}
