/**
 *
 */
package gama.api.kernel.serialization;

import java.io.IOException;
import java.io.ObjectOutput;

/**
 *
 */
public interface IGamaObjectOutput extends ObjectOutput {

	///////////////////////////////////////////////////////////////////////
	@Override
	void writeObject(Object obj) throws IOException;

	/**
	 * Write.
	 *
	 * @param b
	 *            the b
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	void write(int b) throws IOException;

	/**
	 * Write.
	 *
	 * @param b
	 *            the b
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
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
	@Override
	void write(byte[] b, int off, int len) throws IOException;

	/**
	 * Write boolean.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	void writeBoolean(boolean v) throws IOException;

	/**
	 * Write byte.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	void writeByte(int v) throws IOException;

	/**
	 * Write short.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	void writeShort(int v) throws IOException;

	/**
	 * Write char.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	void writeChar(int v) throws IOException;

	/**
	 * Write int.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	void writeInt(int v) throws IOException;

	/**
	 * Write long.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	void writeLong(long v) throws IOException;

	/**
	 * Write float.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	void writeFloat(float v) throws IOException;

	/**
	 * Write double.
	 *
	 * @param v
	 *            the v
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	void writeDouble(double v) throws IOException;

	/**
	 * Write bytes.
	 *
	 * @param s
	 *            the s
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	void writeBytes(String s) throws IOException;

	/**
	 * Write chars.
	 *
	 * @param s
	 *            the s
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	void writeChars(String s) throws IOException;

	/**
	 * Write UTF.
	 *
	 * @param s
	 *            the s
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	void writeUTF(String s) throws IOException;

	//
	// .. end interface impl
	//////////////////////////////////////////////////

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