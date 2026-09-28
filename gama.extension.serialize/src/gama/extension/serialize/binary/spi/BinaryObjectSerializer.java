/*******************************************************************************************************
 *
 * BinaryObjectSerializer.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary.spi;

import java.io.IOException;

/**
 * Serializes and deserializes values of a GAMA type without exposing a binary backend's API.
 *
 * @param <T>
 *            the type handled by this serializer
 */
public interface BinaryObjectSerializer<T> {

	/**
	 * Writes a value using backend-neutral binary operations.
	 *
	 * @param output
	 *            the output for primitive values and nested objects
	 * @param value
	 *            the value to write
	 * @param context
	 *            GAMA state required by the serializer
	 * @throws IOException
	 *             if writing fails
	 */
	void write(BinaryOutput output, T value, SerializationContext context) throws IOException;

	/**
	 * Reads a value using backend-neutral binary operations.
	 *
	 * @param input
	 *            the input for primitive values and nested objects
	 * @param context
	 *            GAMA state required by the serializer
	 * @return the decoded value
	 * @throws IOException
	 *             if reading fails
	 * @throws ClassNotFoundException
	 *             if a nested object refers to an unavailable class
	 */
	T read(BinaryInput input, SerializationContext context) throws IOException, ClassNotFoundException;

	/**
	 * Returns whether decoded values of this type should participate in backend-managed reference tracking.
	 *
	 * @return {@code true} by default
	 */
	default boolean tracksReferences() {
		return true;
	}
}
