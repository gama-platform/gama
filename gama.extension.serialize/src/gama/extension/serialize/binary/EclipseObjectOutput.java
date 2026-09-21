/*******************************************************************************************************
 *
 * EclipseObjectOutput.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import java.io.IOException;

import org.eclipse.serializer.persistence.types.PersistenceStorer;

import gama.extension.serialize.IGamaObjectOutput;

/**
 * The Class EclipseObjectOutput.
 */
public class EclipseObjectOutput implements IGamaObjectOutput {

	/** The persistence store. */
	private final PersistenceStorer persistenceStore;

	/**
	 * Instantiates a new eclipse object output.
	 *
	 * @param persistenceStore
	 *            the persistence store
	 */
	public EclipseObjectOutput(final PersistenceStorer persistenceStore) {
		this.persistenceStore = persistenceStore;
	}

	@Override
	public void writeObject(final Object obj) throws IOException {
		persistenceStore.store(obj);
	}

	@Override
	public void write(final int b) throws IOException {
		persistenceStore.dataOut().writeByte((byte) b);
	}

	@Override
	public void write(final byte[] b) throws IOException {
		persistenceStore.dataOut().writeByteArray(b);
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		byte[] copy = new byte[len];
		System.arraycopy(b, off, copy, 0, len);
		persistenceStore.dataOut().writeByteArray(copy);
	}

	@Override
	public void writeBoolean(final boolean v) throws IOException {
		persistenceStore.dataOut().writeBoolean(v);
	}

	@Override
	public void writeByte(final int v) throws IOException {
		persistenceStore.dataOut().writeByte((byte) v);
	}

	@Override
	public void writeShort(final int v) throws IOException {
		persistenceStore.dataOut().writeShort((short) v);
	}

	@Override
	public void writeChar(final int v) throws IOException {
		persistenceStore.dataOut().writeChar((char) v);
	}

	@Override
	public void writeInt(final int v) throws IOException {
		persistenceStore.dataOut().writeInt(v);
	}

	@Override
	public void writeLong(final long v) throws IOException {
		persistenceStore.dataOut().writeLong(v);
	}

	@Override
	public void writeFloat(final float v) throws IOException {
		persistenceStore.dataOut().writeFloat(v);
	}

	@Override
	public void writeDouble(final double v) throws IOException {
		persistenceStore.dataOut().writeDouble(v);
	}

	@Override
	public void writeBytes(final String s) throws IOException {
		persistenceStore.dataOut().writeByteArray(s.getBytes());
	}

	@Override
	public void writeChars(final String s) throws IOException {
		for (int i = 0; i < s.length(); i++) { persistenceStore.dataOut().writeChar(s.charAt(i)); }
	}

	@Override
	public void writeUTF(final String s) throws IOException {
		writeStringUTF(s);
	}

	@Override
	public void flush() throws IOException {}

	@Override
	public void close() throws IOException {}

	@Override
	public void writeStringUTF(final String str) throws IOException {
		if (str == null) {
			persistenceStore.store(null);
		} else {
			persistenceStore.store(str);
		}
	}

	/**
	 * Write object.
	 *
	 * @param toWrite
	 *            the to write
	 * @param clazz
	 *            the clazz
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	public void writeObject(final Object toWrite, final Class<?> clazz) throws IOException {
		persistenceStore.store(toWrite);
	}
}
