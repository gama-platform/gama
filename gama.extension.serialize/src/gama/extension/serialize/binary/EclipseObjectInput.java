package gama.extension.serialize.binary;

import java.io.IOException;

import org.eclipse.serializer.persistence.types.PersistenceLoad;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;

import gama.extension.serialize.IGamaObjectInput;

public class EclipseObjectInput implements IGamaObjectInput {

	private final PersistenceLoad persistenceLoad;
	private final PersistenceReferenceLoader persistenceReferenceLoader;

	public EclipseObjectInput(PersistenceLoad persistenceLoad, PersistenceReferenceLoader persistenceReferenceLoader) {
		this.persistenceLoad = persistenceLoad;
		this.persistenceReferenceLoader = persistenceReferenceLoader;
	}

	@Override
	public Object readObject() throws ClassNotFoundException, IOException {
		return persistenceLoad.load();
	}

	@Override
	public int read() throws IOException {
		return persistenceLoad.dataIn().readByte() & 0xFF;
	}

	@Override
	public int read(byte[] b) throws IOException {
		byte[] arr = persistenceLoad.dataIn().readByteArray();
		System.arraycopy(arr, 0, b, 0, Math.min(arr.length, b.length));
		return Math.min(arr.length, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		byte[] arr = persistenceLoad.dataIn().readByteArray();
		System.arraycopy(arr, 0, b, off, Math.min(arr.length, len));
		return Math.min(arr.length, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return 0;
	}

	@Override
	public int available() throws IOException {
		return 0;
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		read(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		read(b, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return 0;
	}

	@Override
	public boolean readBoolean() throws IOException {
		return persistenceLoad.dataIn().readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return persistenceLoad.dataIn().readByte();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return persistenceLoad.dataIn().readByte() & 0xFF;
	}

	@Override
	public short readShort() throws IOException {
		return persistenceLoad.dataIn().readShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return persistenceLoad.dataIn().readShort() & 0xFFFF;
	}

	@Override
	public char readChar() throws IOException {
		return persistenceLoad.dataIn().readChar();
	}

	@Override
	public int readInt() throws IOException {
		return persistenceLoad.dataIn().readInt();
	}

	@Override
	public long readLong() throws IOException {
		return persistenceLoad.dataIn().readLong();
	}

	@Override
	public float readFloat() throws IOException {
		return persistenceLoad.dataIn().readFloat();
	}

	@Override
	public double readDouble() throws IOException {
		return persistenceLoad.dataIn().readDouble();
	}

	@Override
	public String readLine() throws IOException {
		return null;
	}

	@Override
	public String readUTF() throws IOException {
		return readStringUTF();
	}

	@Override
	public String readStringUTF() throws IOException {
		try {
			return (String) persistenceLoad.load();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public Object readObject(Class<?> clazz) throws Exception {
		return persistenceLoad.load();
	}

	@Override
	public String readStringAsc() throws IOException {
		return readStringUTF();
	}

	@Override
	public int readFInt() throws IOException {
		return readInt();
	}
}
