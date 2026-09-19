package gama.extension.serialize.binary;

import java.io.IOException;

import org.eclipse.serializer.persistence.types.PersistenceStore;

import gama.extension.serialize.IGamaObjectOutput;

public class EclipseObjectOutput implements IGamaObjectOutput {

	private final PersistenceStore persistenceStore;

	public EclipseObjectOutput(PersistenceStore persistenceStore) {
		this.persistenceStore = persistenceStore;
	}

	@Override
	public void writeObject(Object obj) throws IOException {
		persistenceStore.store(obj);
	}

	@Override
	public void write(int b) throws IOException {
		persistenceStore.dataOut().writeByte((byte) b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		persistenceStore.dataOut().writeByteArray(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		byte[] copy = new byte[len];
		System.arraycopy(b, off, copy, 0, len);
		persistenceStore.dataOut().writeByteArray(copy);
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		persistenceStore.dataOut().writeBoolean(v);
	}

	@Override
	public void writeByte(int v) throws IOException {
		persistenceStore.dataOut().writeByte((byte) v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		persistenceStore.dataOut().writeShort((short) v);
	}

	@Override
	public void writeChar(int v) throws IOException {
		persistenceStore.dataOut().writeChar((char) v);
	}

	@Override
	public void writeInt(int v) throws IOException {
		persistenceStore.dataOut().writeInt(v);
	}

	@Override
	public void writeLong(long v) throws IOException {
		persistenceStore.dataOut().writeLong(v);
	}

	@Override
	public void writeFloat(float v) throws IOException {
		persistenceStore.dataOut().writeFloat(v);
	}

	@Override
	public void writeDouble(double v) throws IOException {
		persistenceStore.dataOut().writeDouble(v);
	}

	@Override
	public void writeBytes(String s) throws IOException {
		persistenceStore.dataOut().writeByteArray(s.getBytes());
	}

	@Override
	public void writeChars(String s) throws IOException {
		for (int i = 0; i < s.length(); i++) {
			persistenceStore.dataOut().writeChar(s.charAt(i));
		}
	}

	@Override
	public void writeUTF(String s) throws IOException {
		writeStringUTF(s);
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public void writeStringUTF(String str) throws IOException {
		if (str == null) {
			persistenceStore.store(null);
		} else {
			persistenceStore.store(str);
		}
	}

	@Override
	public void writeObject(Object toWrite, Class<?> clazz) throws IOException {
		persistenceStore.store(toWrite);
	}
}
