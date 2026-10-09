/*******************************************************************************************************
 *
 * ForyObjectInput.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.fory;

import java.io.IOException;

import org.apache.fory.context.ReadContext;
import org.apache.fory.memory.MemoryBuffer;

import gama.api.kernel.serialization.IGamaObjectInput;

/**
 * An {@link IGamaObjectInput} reading from the {@link ReadContext} of an Apache Fory deserialisation in progress.
 * Mirrors {@link ForyObjectOutput}.
 */
public class ForyObjectInput implements IGamaObjectInput {

	/** The Fory read context. */
	private final ReadContext context;

	/** The underlying buffer. */
	private final MemoryBuffer buffer;

	/**
	 * Instantiates a new input.
	 *
	 * @param context
	 *            the Fory read context from which to read
	 */
	public ForyObjectInput(final ReadContext context) {
		this.context = context;
		this.buffer = context.getBuffer();
	}

	@Override
	public Object readObject() throws ClassNotFoundException, IOException {
		return context.readRef();
	}

	@Override
	public Object readObject(final Class<?>... possibles) throws Exception {
		return context.readRef();
	}

	@Override
	public void readFully(final byte[] b) throws IOException {
		buffer.readBytes(b);
	}

	@Override
	public void readFully(final byte[] b, final int off, final int len) throws IOException {
		buffer.readBytes(b, off, len);
	}

	@Override
	public int skipBytes(final int n) throws IOException {
		final int skipped = Math.min(n, buffer.remaining());
		buffer.increaseReaderIndex(skipped);
		return skipped;
	}

	@Override
	public boolean readBoolean() throws IOException {
		return buffer.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return buffer.readByte();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return buffer.readByte() & 0xFF;
	}

	@Override
	public short readShort() throws IOException {
		return buffer.readInt16();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return buffer.readInt16() & 0xFFFF;
	}

	@Override
	public char readChar() throws IOException {
		return buffer.readChar();
	}

	@Override
	public int readInt() throws IOException {
		return buffer.readVarInt32();
	}

	@Override
	public int readFInt() throws IOException {
		return buffer.readVarInt32();
	}

	@Override
	public long readLong() throws IOException {
		return buffer.readVarInt64();
	}

	@Override
	public float readFloat() throws IOException {
		return buffer.readFloat32();
	}

	@Override
	public double readDouble() throws IOException {
		return buffer.readFloat64();
	}

	@Override
	public String readLine() throws IOException {
		throw new UnsupportedOperationException("readLine() is not supported by binary serialisation");
	}

	@Override
	public String readUTF() throws IOException {
		return context.readString();
	}

	@Override
	public String readStringUTF() throws IOException {
		return context.readString();
	}

	@Override
	public String readStringAsc() throws IOException {
		return context.readString();
	}

	@Override
	public int read() throws IOException {
		return buffer.remaining() == 0 ? -1 : buffer.readByte() & 0xFF;
	}

	@Override
	public int read(final byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		if (len == 0) return 0;
		final int n = Math.min(len, buffer.remaining());
		if (n == 0) return -1;
		buffer.readBytes(b, off, n);
		return n;
	}

	@Override
	public long skip(final long n) throws IOException {
		return skipBytes((int) Math.min(n, Integer.MAX_VALUE));
	}

	@Override
	public int available() throws IOException {
		return buffer.remaining();
	}

	@Override
	public void close() throws IOException {}

}
