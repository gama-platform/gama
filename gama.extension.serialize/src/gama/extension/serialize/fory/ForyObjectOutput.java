/*******************************************************************************************************
 *
 * ForyObjectOutput.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.fory;

import java.io.IOException;

import org.apache.fory.context.WriteContext;
import org.apache.fory.memory.MemoryBuffer;

import gama.api.kernel.serialization.IGamaObjectOutput;

/**
 * An {@link IGamaObjectOutput} writing to the {@link WriteContext} of an Apache Fory serialisation in progress.
 * Instances are short-lived: one is created each time Fory delegates the writing of an object to a GAMA serialiser.
 */
public class ForyObjectOutput implements IGamaObjectOutput {

	/** The Fory write context. */
	private final WriteContext context;

	/** The underlying buffer. */
	private final MemoryBuffer buffer;

	/**
	 * Instantiates a new output.
	 *
	 * @param context
	 *            the Fory write context in which to write
	 */
	public ForyObjectOutput(final WriteContext context) {
		this.context = context;
		this.buffer = context.getBuffer();
	}

	@Override
	public void writeObject(final Object obj) throws IOException {
		context.writeRef(obj);
	}

	@Override
	public void writeObject(final Object obj, final Class... possibles) throws IOException {
		context.writeRef(obj);
	}

	@Override
	public void write(final int b) throws IOException {
		buffer.writeByte(b);
	}

	@Override
	public void write(final byte[] b) throws IOException {
		buffer.writeBytes(b);
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		buffer.writeBytes(b, off, len);
	}

	@Override
	public void writeBoolean(final boolean v) throws IOException {
		buffer.writeBoolean(v);
	}

	@Override
	public void writeByte(final int v) throws IOException {
		buffer.writeByte(v);
	}

	@Override
	public void writeShort(final int v) throws IOException {
		buffer.writeInt16((short) v);
	}

	@Override
	public void writeChar(final int v) throws IOException {
		buffer.writeChar((char) v);
	}

	@Override
	public void writeInt(final int v) throws IOException {
		buffer.writeVarInt32(v);
	}

	@Override
	public void writeLong(final long v) throws IOException {
		buffer.writeVarInt64(v);
	}

	@Override
	public void writeFloat(final float v) throws IOException {
		buffer.writeFloat32(v);
	}

	@Override
	public void writeDouble(final double v) throws IOException {
		buffer.writeFloat64(v);
	}

	@Override
	public void writeBytes(final String s) throws IOException {
		final int len = s.length();
		for (int i = 0; i < len; i++) { buffer.writeByte((byte) s.charAt(i)); }
	}

	@Override
	public void writeChars(final String s) throws IOException {
		final int len = s.length();
		for (int i = 0; i < len; i++) { buffer.writeChar(s.charAt(i)); }
	}

	@Override
	public void writeUTF(final String s) throws IOException {
		context.writeString(s);
	}

	@Override
	public void writeStringUTF(final String str) throws IOException {
		context.writeString(str);
	}

	@Override
	public void writeClassTag(final Class aClass) {
		// Fory writes the type information by itself when objects are written
	}

	@Override
	public void flush() throws IOException {}

	@Override
	public void close() throws IOException {}

}
