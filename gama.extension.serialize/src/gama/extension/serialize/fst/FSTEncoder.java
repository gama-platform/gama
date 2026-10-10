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
package gama.extension.serialize.fst;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by ruedi on 27.03.14.
 */
public interface FSTEncoder {

	/**
	 * Sets the conf.
	 *
	 * @param conf
	 *            the new conf
	 */
	void setConf(FSTConfiguration conf);

	/**
	 * Write raw bytes.
	 *
	 * @param bufferedName
	 *            the buffered name
	 * @param off
	 *            the off
	 * @param length
	 *            the length
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeRawBytes(byte[] bufferedName, int off, int length) throws IOException;

	/**
	 * does not write class tag and length
	 *
	 * @param array
	 * @throws IOException
	 */
	void writePrimitiveArray(Object array, int start, int length) throws IOException;

	/**
	 * Write string UTF.
	 *
	 * @param str
	 *            the str
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeStringUTF(String str) throws IOException;

	/**
	 * Write F short.
	 *
	 * @param c
	 *            the c
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeFShort(short c) throws IOException;

	/**
	 * Write F char.
	 *
	 * @param c
	 *            the c
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeFChar(char c) throws IOException;

	/**
	 * Write F byte.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeFByte(int v) throws IOException;

	/**
	 * Write F int.
	 *
	 * @param anInt
	 *            the an int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeFInt(int anInt) throws IOException;

	/**
	 * Write F long.
	 *
	 * @param anInt
	 *            the an int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeFLong(long anInt) throws IOException;

	/**
	 * Write F float.
	 *
	 * @param value
	 *            the value
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeFFloat(float value) throws IOException;

	/**
	 * Write F double.
	 *
	 * @param value
	 *            the value
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeFDouble(double value) throws IOException;

	/**
	 * Gets the written.
	 *
	 * @return the written
	 */
	int getWritten();

	/**
	 * Skip.
	 *
	 * @param i
	 *            the i
	 */
	void skip(int i);

	/**
	 * close and flush to underlying stream if present. The stream is also closed
	 *
	 * @throws java.io.IOException
	 */
	void close() throws IOException;

	/**
	 * Reset.
	 *
	 * @param out
	 *            the out
	 */
	void reset(byte[] out); // resets outbuff only

	/**
	 * resets stream (positions are lost)
	 *
	 * @throws java.io.IOException
	 */
	void flush() throws IOException;

	/**
	 * used to write uncompressed int (guaranteed length = 4) at a (eventually recent) position
	 *
	 * @param position
	 * @param v
	 */
	void writeInt32At(int position, int v);

	/**
	 * if output stream is null, just encode into a byte array
	 *
	 * @param outstream
	 */
	void setOutstream(OutputStream outstream);

	/**
	 * Ensure free.
	 *
	 * @param bytes
	 *            the bytes
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void ensureFree(int bytes) throws IOException;

	/**
	 * Gets the buffer.
	 *
	 * @return the buffer
	 */
	byte[] getBuffer();

	/**
	 * Register class.
	 *
	 * @param possible
	 *            the possible
	 */
	void registerClass(Class possible);

	/**
	 * Write class.
	 *
	 * @param cl
	 *            the cl
	 */
	void writeClass(Class cl);

	/**
	 * Write class.
	 *
	 * @param clInf
	 *            the cl inf
	 */
	void writeClass(FSTClazzInfo clInf);

	/**
	 * Write tag.
	 *
	 * @param tag
	 *            the tag
	 * @param info
	 *            the info
	 * @param somValue
	 *            the som value
	 * @param toWrite
	 *            the to write
	 * @param oout
	 *            the oout
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	// write a meta byte item. return true if encoder wrote full object (e.g. literal, primitive)
	boolean writeTag(byte tag, Object info, long somValue, Object toWrite, FSTObjectOutput oout) throws IOException;

	/**
	 * Write attribute name.
	 *
	 * @param subInfo
	 *            the sub info
	 * @param value
	 *            the value
	 * @return true, if successful
	 */
	// return true, if this already wrote everything
	boolean writeAttributeName(FSTClazzInfo.FSTFieldInfo subInfo, Object value);

	/**
	 * External end.
	 *
	 * @param clz
	 *            the clz
	 */
	void externalEnd(FSTClazzInfo clz); // demarkls the end of an externalizable or classes with serializer registered

	/**
	 * Checks if is writing attributes.
	 *
	 * @return true, if is writing attributes
	 */
	boolean isWritingAttributes();

	/**
	 * Checks if is primitive array.
	 *
	 * @param array
	 *            the array
	 * @param componentType
	 *            the component type
	 * @return true, if is primitive array
	 */
	boolean isPrimitiveArray(Object array, Class<?> componentType);

	/**
	 * Checks if is tag multi dim sub arrays.
	 *
	 * @return true, if is tag multi dim sub arrays
	 */
	boolean isTagMultiDimSubArrays();

	/**
	 * Write version tag.
	 *
	 * @param version
	 *            the version
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeVersionTag(int version) throws IOException;

	/**
	 * Checks if is byte array based.
	 *
	 * @return true, if is byte array based
	 */
	boolean isByteArrayBased();

	/**
	 * Write array end.
	 */
	void writeArrayEnd();

	/**
	 * Write fields end.
	 *
	 * @param serializationInfo
	 *            the serialization info
	 */
	void writeFieldsEnd(FSTClazzInfo serializationInfo);

	/**
	 * Gets the conf.
	 *
	 * @return the conf
	 */
	FSTConfiguration getConf();
}
