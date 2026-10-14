/*
 * Copyright 2014 Ruediger Moeller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package gama.extension.serialize.fst.coders;

import java.io.IOException;
import java.io.OutputStream;

import gama.extension.serialize.fst.FSTClazzInfo;
import gama.extension.serialize.fst.FSTClazzNameRegistry;
import gama.extension.serialize.fst.FSTConfiguration;
import gama.extension.serialize.fst.FSTEncoder;
import gama.extension.serialize.fst.FSTObjectOutput;
import gama.extension.serialize.fst.util.FSTOutputStream;
import gama.extension.serialize.fst.util.FSTUtil;

/**
 * Default Coder used for serialization. Serializes into a binary stream
 */
public class FSTStreamEncoder implements FSTEncoder {

	/** The conf. */
	private FSTConfiguration conf;

	/** The clnames. */
	private FSTClazzNameRegistry clnames;

	/** The buffout. */
	private FSTOutputStream buffout;

	/** The asc string cache. */
	private byte[] ascStringCache;

	/**
	 * Instantiates a new FST stream encoder.
	 *
	 * @param conf
	 *            the conf
	 */
	public FSTStreamEncoder(final FSTConfiguration conf) {
		this.conf = conf;
		clnames = (FSTClazzNameRegistry) conf.getCachedObject(FSTClazzNameRegistry.class);
		if (clnames == null) {
			clnames = new FSTClazzNameRegistry(conf.getClassRegistry());
		} else {
			clnames.clear();
		}
	}

	@Override
	public void setConf(final FSTConfiguration conf) {
		this.conf = conf;
		clnames = (FSTClazzNameRegistry) conf.getCachedObject(FSTClazzNameRegistry.class);
		if (clnames == null) {
			clnames = new FSTClazzNameRegistry(conf.getClassRegistry());
		} else {
			clnames.clear();
		}
	}

	/**
	 * Write F boolean arr.
	 *
	 * @param arr
	 *            the arr
	 * @param off
	 *            the off
	 * @param len
	 *            the len
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeFBooleanArr(final boolean[] arr, final int off, final int len) throws IOException {
		buffout.ensureFree(len);
		for (int i = off; i < off + len; i++) { buffout.buf[buffout.pos++] = (byte) (arr[i] ? 1 : 0); }
	}

	/**
	 * Write F float arr.
	 *
	 * @param arr
	 *            the arr
	 * @param off
	 *            the off
	 * @param len
	 *            the len
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void writeFFloatArr(final float[] arr, final int off, final int len) throws IOException {
		int byteLen = arr.length * 4;
		buffout.ensureFree(byteLen);
		byte buf[] = buffout.buf;
		int count = buffout.pos;
		int max = off + len;
		for (int i = off; i < max; i++) {
			long anInt = Float.floatToIntBits(arr[i]);
			buf[count] = (byte) (anInt >>> 0);
			buf[count + 1] = (byte) (anInt >>> 8);
			buf[count + 2] = (byte) (anInt >>> 16);
			buf[count + 3] = (byte) (anInt >>> 24);
			count += 4;
		}
		buffout.pos += byteLen;
	}

	/**
	 * Write F double arr.
	 *
	 * @param arr
	 *            the arr
	 * @param off
	 *            the off
	 * @param len
	 *            the len
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void writeFDoubleArr(final double[] arr, final int off, final int len) throws IOException {
		final int byteLen = arr.length * 8;
		buffout.ensureFree(byteLen);
		final byte buf[] = buffout.buf;
		int count = buffout.pos;
		final int max = off + len;
		for (int i = off; i < max; i++) {
			long aLong = Double.doubleToLongBits(arr[i]);
			buf[count] = (byte) (aLong >>> 0);
			buf[count + 1] = (byte) (aLong >>> 8);
			buf[count + 2] = (byte) (aLong >>> 16);
			buf[count + 3] = (byte) (aLong >>> 24);
			buf[count + 4] = (byte) (aLong >>> 32);
			buf[count + 5] = (byte) (aLong >>> 40);
			buf[count + 6] = (byte) (aLong >>> 48);
			buf[count + 7] = (byte) (aLong >>> 56);
			count += 8;
		}
		buffout.pos += byteLen;
	}

	// Using ByteBuffers is faster but only with 1.8_u40. seems terrible with prior versions.
	// also requires some hacking in order to reuse a bytebuffer

	// static Field bbHB, bbCap;
	// static {
	// Field[] fields = ByteBuffer.class.getDeclaredFields();
	// for (int i = 0; i < fields.length; i++) {
	// Field fi = fields[i];
	// if ( fi.getName() == "hb" ) {
	// bbHB = fi;
	// bbHB.setAccessible(true);
	// }
	// }
	// fields = Buffer.class.getDeclaredFields();
	// for (int i = 0; i < fields.length; i++) {
	// Field fi = fields[i];
	// if ( fi.getName() == "capacity" ) {
	// bbCap = fi;
	// bbCap.setAccessible(true);
	// }
	// }
	// }
	//
	// ThreadLocal<ByteBuffer> buf = new ThreadLocal<ByteBuffer>() {
	// @Override
	// protected ByteBuffer initialValue() {
	// return ByteBuffer.wrap(new byte[0]);
	// }
	// };
	//
	// public void writeFDoubleArr(double[] arr, int off, int len) throws IOException {
	// int byteLen = arr.length * 8;
	// buffout.ensureFree(byteLen);
	// int max = off + len;
	// ByteBuffer wrap = buf.get();
	// try {
	// bbHB.set(wrap, buffout.buf);
	// bbCap.set(wrap,buffout.buf.length);
	// wrap.limit(buffout.pos+byteLen);
	// } catch (IllegalAccessException e) {
	// e.printStackTrace();
	// wrap = ByteBuffer.wrap(buffout.buf, buffout.pos, byteLen).order(ByteOrder.LITTLE_ENDIAN);
	// }

	// int count = buffout.pos;
	// for (int i = off; i < max; i++) {
	// long aLong = Double.doubleToLongBits(arr[i]);
	// wrap.putLong(count,aLong);
	// count += 8;
	// }
	// buffout.pos+= byteLen;
	// }

	/**
	 * Write F short arr.
	 *
	 * @param arr
	 *            the arr
	 * @param off
	 *            the off
	 * @param len
	 *            the len
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void writeFShortArr(final short[] arr, final int off, final int len) throws IOException {
		buffout.ensureFree(len * 3);
		for (int i = off; i < off + len; i++) {
			short c = arr[i];
			if (c < 255 && c >= 0) {
				buffout.buf[buffout.pos++] = (byte) c;
			} else {
				buffout.buf[buffout.pos] = (byte) 255;
				buffout.buf[buffout.pos + 1] = (byte) (c >>> 0);
				buffout.buf[buffout.pos + 2] = (byte) (c >>> 8);
				buffout.pos += 3;
			}
		}
	}

	/**
	 * Write F char arr.
	 *
	 * @param arr
	 *            the arr
	 * @param off
	 *            the off
	 * @param len
	 *            the len
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void writeFCharArr(final char[] arr, final int off, final int len) throws IOException {
		buffout.ensureFree(len * 3);
		for (int i = off; i < off + len; i++) {
			char c = arr[i];
			if (c < 255 && c >= 0) {
				buffout.buf[buffout.pos++] = (byte) c;
			} else {
				byte[] buf = buffout.buf;
				int count = buffout.pos;
				buf[count] = (byte) 255;
				buf[count + 1] = (byte) (c >>> 0);
				buf[count + 2] = (byte) (c >>> 8);
				buffout.pos += 3;
			}
		}
	}

	/**
	 * Write F int arr.
	 *
	 * @param arr
	 *            the arr
	 * @param off
	 *            the off
	 * @param len
	 *            the len
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	// uncompressed version
	public void writeFIntArr(final int[] arr, final int off, final int len) throws IOException {
		int byteLen = arr.length * 4;
		buffout.ensureFree(byteLen);
		byte buf[] = buffout.buf;
		int count = buffout.pos;
		int max = off + len;
		for (int i = off; i < max; i++) {
			long anInt = arr[i];
			buf[count] = (byte) (anInt >>> 0);
			buf[count + 1] = (byte) (anInt >>> 8);
			buf[count + 2] = (byte) (anInt >>> 16);
			buf[count + 3] = (byte) (anInt >>> 24);
			count += 4;
		}
		buffout.pos += byteLen;
	}

	/**
	 * Write F int arr.
	 *
	 * @param v
	 *            the v
	 * @param off
	 *            the off
	 * @param len
	 *            the len
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	// compressed version
	public void _writeFIntArr(final int v[], final int off, final int len) throws IOException {
		final int free = 5 * len;
		buffout.ensureFree(free);
		final byte[] buf = buffout.buf;
		int count = buffout.pos;
		for (int i = off; i < off + len; i++) {
			final int anInt = v[i];
			if (anInt > -127 && anInt <= 127) {
				buffout.buf[count++] = (byte) anInt;
			} else if (anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE) {
				buf[count++] = -128;
				buf[count++] = (byte) (anInt >>> 0 & 0xFF);
				buf[count++] = (byte) (anInt >>> 8 & 0xFF);
			} else {
				buf[count++] = -127;
				buf[count++] = (byte) (anInt >>> 0 & 0xFF);
				buf[count++] = (byte) (anInt >>> 8 & 0xFF);
				buf[count++] = (byte) (anInt >>> 16 & 0xFF);
				buf[count++] = (byte) (anInt >>> 24 & 0xFF);
			}
		}
		buffout.pos = count;
	}

	/**
	 * Write F long arr.
	 *
	 * @param arr
	 *            the arr
	 * @param off
	 *            the off
	 * @param len
	 *            the len
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeFLongArr(final long[] arr, final int off, final int len) throws IOException {
		int byteLen = arr.length * 8;
		buffout.ensureFree(byteLen);
		byte buf[] = buffout.buf;
		int count = buffout.pos;
		for (int i = off; i < off + len; i++) {
			long anInt = arr[i];
			buf[count] = (byte) (anInt >>> 0);
			buf[count + 1] = (byte) (anInt >>> 8);
			buf[count + 2] = (byte) (anInt >>> 16);
			buf[count + 3] = (byte) (anInt >>> 24);
			buf[count + 4] = (byte) (anInt >>> 32);
			buf[count + 5] = (byte) (anInt >>> 40);
			buf[count + 6] = (byte) (anInt >>> 48);
			buf[count + 7] = (byte) (anInt >>> 56);
			count += 8;
		}
		buffout.pos += byteLen;
	}

	/**
	 * write prim array no len no tag
	 *
	 *
	 * @param array
	 * @throws IOException
	 */
	@Override
	public void writePrimitiveArray(final Object array, final int off, final int len) throws IOException {
		Class<?> componentType = array.getClass().getComponentType();
		if (componentType == byte.class) {
			writeRawBytes((byte[]) array, off, len);
		} else if (componentType == char.class) {
			writeFCharArr((char[]) array, off, len);
		} else if (componentType == short.class) {
			writeFShortArr((short[]) array, off, len);
		} else if (componentType == int.class) {
			writeFIntArr((int[]) array, off, len);
		} else if (componentType == double.class) {
			writeFDoubleArr((double[]) array, off, len);
		} else if (componentType == float.class) {
			writeFFloatArr((float[]) array, off, len);
		} else if (componentType == long.class) {
			writeFLongArr((long[]) array, off, len);
		} else if (componentType == boolean.class) {
			writeFBooleanArr((boolean[]) array, off, len);
		} else
			throw new RuntimeException("expected primitive array");
	}

	/**
	 * does not write length, just plain bytes
	 *
	 * @param array
	 * @param length
	 * @throws java.io.IOException
	 */
	@Override
	public void writeRawBytes(final byte[] array, final int start, final int length) throws IOException {
		buffout.ensureFree(length);
		System.arraycopy(array, start, buffout.buf, buffout.pos, length);
		buffout.pos += length;
	}

	@Override
	public void writeStringUTF(final String str) throws IOException {
		final int strlen = str.length();

		writeFInt(strlen);
		buffout.ensureFree(strlen * 3);
		final byte[] bytearr = buffout.buf;
		int count = buffout.pos;
		for (int i = 0; i < strlen; i++) {
			final char c = str.charAt(i);
			bytearr[count++] = (byte) c;
			if (c >= 255) {
				bytearr[count - 1] = (byte) 255;
				bytearr[count++] = (byte) (c >>> 0 & 0xFF);
				bytearr[count++] = (byte) (c >>> 8 & 0xFF);
			}
		}
		buffout.pos = count;
	}

	/**
	 * length < 127 !!!!!
	 *
	 * @param name
	 * @throws java.io.IOException
	 */
	void writeStringAsc(final String name) throws IOException {
		int len = name.length();
		if (len >= 127) throw new RuntimeException("Ascii String too long");
		writeFByte((byte) len);
		buffout.ensureFree(len);
		if (ascStringCache == null || ascStringCache.length < len) { ascStringCache = new byte[len]; }
		name.getBytes(0, len, ascStringCache, 0);
		writeRawBytes(ascStringCache, 0, len);
	}

	@Override
	public void writeFShort(final short c) throws IOException {
		if (c < 255 && c >= 0) {
			writeFByte(c);
		} else {
			writeFByte(255);
			writePlainShort(c);
		}
	}

	@Override
	public boolean writeAttributeName(final FSTClazzInfo.FSTFieldInfo subInfo, final Object value) {
		return false;
	}

	@Override
	public boolean writeTag(final byte tag, final Object info, final long somValue, final Object toWrite, final FSTObjectOutput oout)
			throws IOException {
		writeFByte(tag);
		return false;
	}

	@Override
	public void writeFChar(final char c) throws IOException {
		// -128 = short byte, -127 == 4 byte
		if (c < 255 && c >= 0) {
			buffout.ensureFree(1);
			buffout.buf[buffout.pos++] = (byte) c;
		} else {
			buffout.ensureFree(3);
			byte[] buf = buffout.buf;
			int count = buffout.pos;
			buf[count++] = (byte) 255;
			buf[count++] = (byte) (c >>> 0);
			buf[count++] = (byte) (c >>> 8);
			buffout.pos += 3;
		}
	}

	@Override
	public final void writeFByte(final int v) throws IOException {
		buffout.ensureFree(1);
		buffout.buf[buffout.pos++] = (byte) v;
	}

	@Override
	public void writeFInt(final int anInt) throws IOException {
		// -128 = short byte, -127 == 4 byte
		if (anInt > -127 && anInt <= 127) {
			if (buffout.buf.length <= buffout.pos + 1) { buffout.ensureFree(1); }
			buffout.buf[buffout.pos++] = (byte) anInt;
		} else if (anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE) {
			ensureFree(3);
			byte[] buf = buffout.buf;
			int count = buffout.pos;
			buf[count++] = (byte) -128;
			buf[count++] = (byte) (anInt >>> 0);
			buf[count++] = (byte) (anInt >>> 8);
			buffout.pos = count;
		} else {
			buffout.ensureFree(5);
			byte[] buf = buffout.buf;
			int count = buffout.pos;
			buf[count++] = (byte) -127;
			buf[count++] = (byte) (anInt >>> 0 & 0xFF);
			buf[count++] = (byte) (anInt >>> 8 & 0xFF);
			buf[count++] = (byte) (anInt >>> 16 & 0xFF);
			buf[count++] = (byte) (anInt >>> 24 & 0xFF);
			buffout.pos = count;
		}
	}

	@Override
	public void writeFLong(final long anInt) throws IOException {
		// -128 = short byte, -127 == 4 byte
		if (anInt > -126 && anInt <= 127) {
			writeFByte((int) anInt);
		} else if (anInt >= Short.MIN_VALUE && anInt <= Short.MAX_VALUE) {
			ensureFree(3);
			byte[] buf = buffout.buf;
			int count = buffout.pos;
			buf[count++] = (byte) -128;
			buf[count++] = (byte) (anInt >>> 0);
			buf[count++] = (byte) (anInt >>> 8);
			buffout.pos = count;
		} else if (anInt >= Integer.MIN_VALUE && anInt <= Integer.MAX_VALUE) {
			buffout.ensureFree(5);
			byte[] buf = buffout.buf;
			int count = buffout.pos;
			buf[count++] = (byte) -127;
			buf[count++] = (byte) (anInt >>> 0 & 0xFF);
			buf[count++] = (byte) (anInt >>> 8 & 0xFF);
			buf[count++] = (byte) (anInt >>> 16 & 0xFF);
			buf[count++] = (byte) (anInt >>> 24 & 0xFF);
			buffout.pos = count;
		} else {
			buffout.ensureFree(9);
			byte[] buf = buffout.buf;
			int count = buffout.pos;
			buf[count++] = (byte) -126;
			buf[count++] = (byte) (anInt >>> 0);
			buf[count++] = (byte) (anInt >>> 8);
			buf[count++] = (byte) (anInt >>> 16);
			buf[count++] = (byte) (anInt >>> 24);
			buf[count++] = (byte) (anInt >>> 32);
			buf[count++] = (byte) (anInt >>> 40);
			buf[count++] = (byte) (anInt >>> 48);
			buf[count++] = (byte) (anInt >>> 56);
			buffout.pos = count;
		}
	}

	/**
	 * Writes a 4 byte float.
	 */
	@Override
	public void writeFFloat(final float value) throws IOException {
		writePlainInt(Float.floatToIntBits(value));
	}

	@Override
	public void writeFDouble(final double value) throws IOException {
		writePlainLong(Double.doubleToLongBits(value));
	}

	@Override
	public int getWritten() { return buffout.pos - buffout.getOff(); }

	/**
	 * close and flush to underlying stream if present. The stream is also closed
	 *
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException {
		buffout.close();
		conf.returnObject(clnames);
	}

	@Override
	public void reset(final byte[] out) {
		if (out == null) {
			buffout.reset();
		} else {
			buffout.reset(out);
		}
		clnames.clear();
	}

	@Override
	public void skip(final int i) {
		buffout.pos += i;
	}

	/**
	 * used to write uncompressed int (guaranteed length = 4) at a (eventually recent) position
	 *
	 * @param position
	 * @param v
	 */
	@Override
	public void writeInt32At(final int position, final int v) {
		buffout.buf[position] = (byte) (v >>> 0);
		buffout.buf[position + 1] = (byte) (v >>> 8);
		buffout.buf[position + 2] = (byte) (v >>> 16);
		buffout.buf[position + 3] = (byte) (v >>> 24);
	}

	/**
	 * if output stream is null, just encode into a byte array
	 *
	 * @param outstream
	 */
	@Override
	public void setOutstream(final OutputStream outstream) {
		if (buffout == null) {
			// try reuse
			buffout = (FSTOutputStream) conf.getCachedObject(FSTOutputStream.class);
			if (buffout == null) { // if fail, alloc
				buffout = new FSTOutputStream(1000, outstream);
			} else {
				buffout.reset(); // reset resued fstoutput
			}
		}
		if (outstream == null) {
			buffout.setOutstream(buffout);
		} else {
			buffout.setOutstream(outstream);
		}
	}

	/**
	 * Gets the outstream.
	 *
	 * @return the outstream
	 */
	public OutputStream getOutstream() { return buffout; }

	/**
	 * writes current buffer to underlying output and resets buffer.
	 *
	 * @throws IOException
	 */
	@Override
	public void flush() throws IOException {
		buffout.flush();
	}

	@Override
	public void ensureFree(final int bytes) throws IOException {
		buffout.ensureFree(bytes);
	}

	@Override
	public byte[] getBuffer() { return buffout.buf; }

	@Override
	public void registerClass(final Class possible) {
		clnames.registerClass(possible, conf);
	}

	@Override
	public final void writeClass(final Class cl) {
		try {
			clnames.encodeClass(this, cl);
		} catch (IOException e) {
			FSTUtil.<RuntimeException> rethrow(e);
		}
	}

	@Override
	public final void writeClass(final FSTClazzInfo clInf) {
		try {
			clnames.encodeClass(this, clInf);
		} catch (IOException e) {
			FSTUtil.<RuntimeException> rethrow(e);
		}
	}

	/**
	 * Write plain long.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writePlainLong(final long v) throws IOException {
		buffout.ensureFree(8);
		byte[] buf = buffout.buf;
		int count = buffout.pos;
		buf[count++] = (byte) (v >>> 0);
		buf[count++] = (byte) (v >>> 8);
		buf[count++] = (byte) (v >>> 16);
		buf[count++] = (byte) (v >>> 24);
		buf[count++] = (byte) (v >>> 32);
		buf[count++] = (byte) (v >>> 40);
		buf[count++] = (byte) (v >>> 48);
		buf[count++] = (byte) (v >>> 56);
		buffout.pos += 8;
	}

	/**
	 * Write plain short.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writePlainShort(final int v) throws IOException {
		buffout.ensureFree(2);
		byte[] buf = buffout.buf;
		int count = buffout.pos;
		buf[count++] = (byte) (v >>> 0);
		buf[count++] = (byte) (v >>> 8);
		buffout.pos += 2;
	}

	/**
	 * Write plain char.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writePlainChar(final int v) throws IOException {
		buffout.ensureFree(2);
		byte[] buf = buffout.buf;
		int count = buffout.pos;
		buf[count++] = (byte) (v >>> 0);
		buf[count++] = (byte) (v >>> 8);
		buffout.pos += 2;
	}

	/**
	 * Write plain int.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writePlainInt(final int v) throws IOException {
		buffout.ensureFree(4);
		byte[] buf = buffout.buf;
		int count = buffout.pos;
		buf[count++] = (byte) (v >>> 0 & 0xFF);
		buf[count++] = (byte) (v >>> 8 & 0xFF);
		buf[count++] = (byte) (v >>> 16 & 0xFF);
		buf[count++] = (byte) (v >>> 24 & 0xFF);
		buffout.pos = count;
	}

	@Override
	public void externalEnd(final FSTClazzInfo clz) {}

	@Override
	public boolean isWritingAttributes() { return false; }

	@Override
	public boolean isPrimitiveArray(final Object array, final Class<?> componentType) {
		return componentType.isPrimitive();
	}

	@Override
	public boolean isTagMultiDimSubArrays() { return false; }

	@Override
	public void writeVersionTag(final int version) throws IOException {
		writeFByte(version);
	}

	@Override
	public boolean isByteArrayBased() { return true; }

	@Override
	public void writeArrayEnd() {

	}

	@Override
	public void writeFieldsEnd(final FSTClazzInfo serializationInfo) {}

	@Override
	public FSTConfiguration getConf() { return conf; }

}
