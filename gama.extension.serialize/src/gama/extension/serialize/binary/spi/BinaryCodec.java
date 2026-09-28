/*******************************************************************************************************
 *
 * BinaryCodec.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary.spi;

import java.io.IOException;

/**
 * Backend contract for encoding and decoding binary object graphs.
 */
public interface BinaryCodec {

	/**
	 * Registers a GAMA serializer for the specified type.
	 *
	 * @param type
	 *            the serialized type
	 * @param serializer
	 *            the serializer to use
	 * @param includeSubtypes
	 *            whether the serializer also handles subtypes
	 * @param <T>
	 *            the serialized type
	 */
	<T> void registerSerializer(Class<T> type, BinaryObjectSerializer<T> serializer, boolean includeSubtypes);

	/**
	 * Encodes an object graph.
	 *
	 * @param value
	 *            the root value
	 * @param context
	 *            GAMA state required by serializers
	 * @return the encoded object graph
	 * @throws IOException
	 *             if encoding fails
	 */
	byte[] encode(Object value, SerializationContext context) throws IOException;

	/**
	 * Decodes an object graph.
	 *
	 * @param bytes
	 *            the encoded object graph
	 * @param context
	 *            GAMA state required by serializers
	 * @return the decoded root value
	 * @throws IOException
	 *             if decoding fails
	 * @throws ClassNotFoundException
	 *             if the encoded graph refers to an unavailable class
	 */
	Object decode(byte[] bytes, SerializationContext context) throws IOException, ClassNotFoundException;
}
