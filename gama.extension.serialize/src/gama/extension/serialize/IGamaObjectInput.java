/**
 *
 */
package gama.extension.serialize;

import java.io.IOException;
import java.io.ObjectInput;

import gama.extension.serialize.fst.FSTClazzInfo;

/**
 *
 */
public interface IGamaObjectInput extends ObjectInput {

	/**
	 * Read fully.
	 *
	 * @param b
	 *            the b
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	void readFully(byte[] b) throws IOException;

	/**
	 * Read fully.
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
	void readFully(byte[] b, int off, int len) throws IOException;

	/**
	 * Skip bytes.
	 *
	 * @param n
	 *            the n
	 * @return the int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	int skipBytes(int n) throws IOException;

	/**
	 * Read boolean.
	 *
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	boolean readBoolean() throws IOException;

	/**
	 * Read byte.
	 *
	 * @return the byte
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	byte readByte() throws IOException;

	/**
	 * Read unsigned byte.
	 *
	 * @return the int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	int readUnsignedByte() throws IOException;

	/**
	 * Read short.
	 *
	 * @return the short
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	short readShort() throws IOException;

	/**
	 * Read unsigned short.
	 *
	 * @return the int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	int readUnsignedShort() throws IOException;

	/**
	 * Read char.
	 *
	 * @return the char
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	char readChar() throws IOException;

	/**
	 * Read int.
	 *
	 * @return the int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	int readInt() throws IOException;

	/**
	 * Read long.
	 *
	 * @return the long
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	long readLong() throws IOException;

	/**
	 * Read float.
	 *
	 * @return the float
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	float readFloat() throws IOException;

	/**
	 * Read double.
	 *
	 * @return the double
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	double readDouble() throws IOException;

	/**
	 * Read line.
	 *
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	String readLine() throws IOException;

	/**
	 * Read UTF.
	 *
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	String readUTF() throws IOException;

	/**
	 * Read object.
	 *
	 * @return the object
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	Object readObject() throws ClassNotFoundException, IOException;

	/**
	 * Read.
	 *
	 * @return the int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	int read() throws IOException;

	/**
	 * Read.
	 *
	 * @param b
	 *            the b
	 * @return the int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	int read(byte[] b) throws IOException;

	/**
	 * Read.
	 *
	 * @param b
	 *            the b
	 * @param off
	 *            the off
	 * @param len
	 *            the len
	 * @return the int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	int read(byte[] b, int off, int len) throws IOException;

	/**
	 * Skip.
	 *
	 * @param n
	 *            the n
	 * @return the long
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	long skip(long n) throws IOException;

	/**
	 * Read object.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param possibles
	 *            the possibles
	 * @return the object
	 * @throws Exception
	 *             the exception
	 * @date 29 sept. 2023
	 */
	Object readObject(Class<?>... possibles) throws Exception;

	/**
	 * Read string UTF.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @date 29 sept. 2023
	 */
	String readStringUTF() throws IOException;

	/**
	 * len < 127 !!!!!
	 *
	 * @return
	 * @throws IOException
	 */
	String readStringAsc() throws IOException;

	/**
	 * Read class.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the FST clazz info
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 * @date 29 sept. 2023
	 */
	FSTClazzInfo readClass() throws IOException, ClassNotFoundException;

	/**
	 * Read F int.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @return the int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @date 29 sept. 2023
	 */
	int readFInt() throws IOException;

}