/*******************************************************************************************************
 *
 * FSTBigNumberSerializers.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.fst.serializers;

import java.io.IOException;

import gama.extension.serialize.fst.FSTBasicObjectSerializer;
import gama.extension.serialize.fst.FSTClazzInfo;
import gama.extension.serialize.fst.FSTObjectInput;
import gama.extension.serialize.fst.FSTObjectOutput;

/**
 * Created with IntelliJ IDEA. User: ruedi Date: 07.12.12 Time: 20:39 To change this template use File | Settings | File
 * Templates.
 */

/**
 * Long and Integer are built in for speed.
 */
public class FSTBigNumberSerializers {

	/**
	 * The Class FSTByteSerializer.
	 */
	public static class FSTByteSerializer extends FSTBasicObjectSerializer {
		@Override
		public void writeObject(final FSTObjectOutput out, final Object toWrite, final FSTClazzInfo clzInfo,
				final FSTClazzInfo.FSTFieldInfo referencedBy, final int streamPosition) throws IOException {
			out.writeByte(((Byte) toWrite).byteValue());
		}

		@Override
		public Object instantiate(final Class objectClass, final FSTObjectInput in, final FSTClazzInfo serializationInfo,
				final FSTClazzInfo.FSTFieldInfo referencee, final int streamPosition) throws Exception {
			Object res = Byte.valueOf(in.readByte());
			return res;
		}

		/**
		 * @return true if FST can skip a search for same instances in the serialized ObjectGraph. This speeds up
		 *         reading and writing and makes sense for short immutable such as Integer, Short, Character, Date, .. .
		 *         For those classes it is more expensive (CPU, size) to do a lookup than to just write the Object twice
		 *         in case.
		 */
		@Override
		public boolean alwaysCopy() {
			return true;
		}

		@Override
		public boolean writeTupleEnd() {
			return false;
		}

	}

	/**
	 * The Class FSTCharSerializer.
	 */
	static public class FSTCharSerializer extends FSTBasicObjectSerializer {
		@Override
		public void writeObject(final FSTObjectOutput out, final Object toWrite, final FSTClazzInfo clzInfo,
				final FSTClazzInfo.FSTFieldInfo referencedBy, final int streamPosition) throws IOException {
			out.writeChar(((Character) toWrite).charValue());
		}

		@Override
		public Object instantiate(final Class objectClass, final FSTObjectInput in, final FSTClazzInfo serializationInfo,
				final FSTClazzInfo.FSTFieldInfo referencee, final int streamPosition) throws Exception {
			Object res = Character.valueOf(in.readChar());
			return res;
		}

		@Override
		public boolean alwaysCopy() {
			return true;
		}

		@Override
		public boolean writeTupleEnd() {
			return false;
		}

	}

	/**
	 * The Class FSTShortSerializer.
	 */
	static public class FSTShortSerializer extends FSTBasicObjectSerializer {

		@Override
		public void writeObject(final FSTObjectOutput out, final Object toWrite, final FSTClazzInfo clzInfo,
				final FSTClazzInfo.FSTFieldInfo referencedBy, final int streamPosition) throws IOException {
			out.writeShort(((Short) toWrite).shortValue());
		}

		@Override
		public Object instantiate(final Class objectClass, final FSTObjectInput in, final FSTClazzInfo serializationInfo,
				final FSTClazzInfo.FSTFieldInfo referencee, final int streamPosition) throws Exception {
			Object res = Short.valueOf(in.readShort());
			return res;
		}

		@Override
		public boolean alwaysCopy() {
			return true;
		}

		@Override
		public boolean writeTupleEnd() {
			return false;
		}

	}

	/**
	 * The Class FSTFloatSerializer.
	 */
	static public class FSTFloatSerializer extends FSTBasicObjectSerializer {

		@Override
		public void writeObject(final FSTObjectOutput out, final Object toWrite, final FSTClazzInfo clzInfo,
				final FSTClazzInfo.FSTFieldInfo referencedBy, final int streamPosition) throws IOException {
			out.writeFloat(((Float) toWrite).floatValue());
		}

		@Override
		public Object instantiate(final Class objectClass, final FSTObjectInput in, final FSTClazzInfo serializationInfo,
				final FSTClazzInfo.FSTFieldInfo referencee, final int streamPosition) throws Exception {
			Object res = Float.valueOf(in.readFloat());
			return res;
		}

		@Override
		public boolean alwaysCopy() {
			return true;
		}

		@Override
		public boolean writeTupleEnd() {
			return false;
		}

	}

	/**
	 * The Class FSTDoubleSerializer.
	 */
	static public class FSTDoubleSerializer extends FSTBasicObjectSerializer {

		@Override
		public void writeObject(final FSTObjectOutput out, final Object toWrite, final FSTClazzInfo clzInfo,
				final FSTClazzInfo.FSTFieldInfo referencedBy, final int streamPosition) throws IOException {
			out.writeDouble(((Double) toWrite).doubleValue());
		}

		@Override
		public Object instantiate(final Class objectClass, final FSTObjectInput in, final FSTClazzInfo serializationInfo,
				final FSTClazzInfo.FSTFieldInfo referencee, final int streamPosition) throws Exception {
			Object res = Double.valueOf(in.readDouble());
			return res;
		}

		@Override
		public boolean alwaysCopy() {
			return true;
		}

		@Override
		public boolean writeTupleEnd() {
			return false;
		}

	}

}
