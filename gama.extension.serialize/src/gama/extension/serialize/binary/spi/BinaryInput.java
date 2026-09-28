/*******************************************************************************************************
 *
 * BinaryInput.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary.spi;

import java.io.DataInput;
import java.io.IOException;

/**
 * Backend-neutral input for primitive values, strings, and nested objects.
 */
public interface BinaryInput extends DataInput {

	/**
	 * Reads a nested object.
	 *
	 * @return the decoded object, or {@code null}
	 * @throws IOException
	 *             if reading fails
	 * @throws ClassNotFoundException
	 *             if the object refers to an unavailable class
	 */
	Object readObject() throws IOException, ClassNotFoundException;

	/**
	 * Reads a nested object and verifies that it is assignable to the requested type.
	 *
	 * @param type
	 *            the required type
	 * @param <T>
	 *            the required type
	 * @return the decoded object, or {@code null}
	 * @throws IOException
	 *             if reading fails or the decoded object has the wrong type
	 * @throws ClassNotFoundException
	 *             if the object refers to an unavailable class
	 */
	<T> T readObject(Class<T> type) throws IOException, ClassNotFoundException;

	/**
	 * Reads a backend-neutral string value.
	 *
	 * @return the decoded string
	 * @throws IOException
	 *             if reading fails
	 */
	String readString() throws IOException;
}
