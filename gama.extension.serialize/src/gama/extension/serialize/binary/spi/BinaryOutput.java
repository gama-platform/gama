/*******************************************************************************************************
 *
 * BinaryOutput.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary.spi;

import java.io.DataOutput;
import java.io.IOException;

/**
 * Backend-neutral output for primitive values, strings, and nested objects.
 */
public interface BinaryOutput extends DataOutput {

	/**
	 * Writes a nested object.
	 *
	 * @param value
	 *            the value to write
	 * @throws IOException
	 *             if writing fails
	 */
	void writeObject(Object value) throws IOException;

	/**
	 * Writes a backend-neutral string value.
	 *
	 * @param value
	 *            the string to write
	 * @throws IOException
	 *             if writing fails
	 */
	void writeString(String value) throws IOException;
}
