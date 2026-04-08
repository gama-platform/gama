/**
 *
 */
package gama.extension.serialize;

import java.io.IOException;
import java.io.ObjectOutput;

/**
 *
 */
public interface IGamaObjectOutput extends ObjectOutput {

	/** The Constant SPECIAL_COMPATIBILITY_OBJECT_TAG. */
	byte SPECIAL_COMPATIBILITY_OBJECT_TAG = -19; // see issue 52
	/** The Constant ONE_OF. */
	byte ONE_OF = -18;
	/** The Constant BIG_BOOLEAN_FALSE. */
	byte BIG_BOOLEAN_FALSE = -17;
	/** The Constant BIG_BOOLEAN_TRUE. */
	byte BIG_BOOLEAN_TRUE = -16;
	/** The Constant BIG_LONG. */
	byte BIG_LONG = -10;
	/** The Constant BIG_INT. */
	byte BIG_INT = -9;
	/** The Constant DIRECT_ARRAY_OBJECT. */
	byte DIRECT_ARRAY_OBJECT = -8;
	/** The Constant HANDLE. */
	byte HANDLE = -7;
	/** The Constant ENUM. */
	byte ENUM = -6;
	/** The Constant ARRAY. */
	byte ARRAY = -5;
	/** The Constant STRING. */
	byte STRING = -4;
	/** The Constant TYPED. */
	byte TYPED = -3; // var class == object written class
	/** The Constant DIRECT_OBJECT. */
	byte DIRECT_OBJECT = -2;
	/** The Constant NULL. */
	byte NULL = -1;
	/** The Constant OBJECT. */
	byte OBJECT = 0;

	////////////////////////////////////////////////////////////////////////
	void writeObject(Object obj) throws IOException;

	/**
	 * Write.
	 *
	 * @param b
	 *            the b
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void write(int b) throws IOException;

	/**
	 * Write.
	 *
	 * @param b
	 *            the b
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void write(byte[] b) throws IOException;

	/**
	 * Write.
	 *
	 * @param b
	 *            the b
	 * @param off
	 *            the off
	 * @param len
	 *            the len
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void write(byte[] b, int off, int len) throws IOException;

	/**
	 * Write boolean.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeBoolean(boolean v) throws IOException;

	/**
	 * Write byte.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeByte(int v) throws IOException;

	/**
	 * Write short.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeShort(int v) throws IOException;

	/**
	 * Write char.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeChar(int v) throws IOException;

	/**
	 * Write int.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeInt(int v) throws IOException;

	/**
	 * Write long.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeLong(long v) throws IOException;

	/**
	 * Write float.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeFloat(float v) throws IOException;

	/**
	 * Write double.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeDouble(double v) throws IOException;

	/**
	 * Write bytes.
	 *
	 * @param s
	 *            the s
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeBytes(String s) throws IOException;

	/**
	 * Write chars.
	 *
	 * @param s
	 *            the s
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeChars(String s) throws IOException;

	/**
	 * Write UTF.
	 *
	 * @param s
	 *            the s
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	void writeUTF(String s) throws IOException;

	//
	// .. end interface impl
	///////////////////////////////////////////////////

	/**
	 * Write object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param obj
	 *            the obj
	 * @param possibles
	 *            the possibles
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @date 29 sept. 2023
	 */
	void writeObject(Object obj, Class... possibles) throws IOException;

	/**
	 * Write string UTF.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param str
	 *            the str
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @date 29 sept. 2023
	 */
	void writeStringUTF(String str) throws IOException;

	/**
	 * Write class tag.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param aClass
	 *            the a class
	 * @date 29 sept. 2023
	 */
	void writeClassTag(Class aClass);

}