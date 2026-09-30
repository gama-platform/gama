/*******************************************************************************************************
 *
 * FSTGamaIndividualSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.fst;

import java.io.IOException;

import gama.api.kernel.serialization.AbstractBinarySerializer.TransientSerializationContext;
import gama.api.kernel.serialization.IGamaObjectSerializer;
import gama.extension.serialize.fst.FSTClazzInfo.FSTFieldInfo;

/**
 * Base class for FST-based individual serialisers used within {@link FSTBinarySerialiser}. Wraps an
 * IGamaObjectSerializer responsible for serialising and deserialising a single specific GAMA type. Instances hold a
 * reference to a {@link TransientSerializationContext} to access the current simulation scope and shared serialisation
 * state (i.e. {@code inAgent} flag).
 *
 * <p>
 * Subclasses must implement {@link #deserialise(IScope, FSTObjectInput)} and may optionally override
 * {@link #serialise(FSTObjectOutput, Object)} and {@link #shouldRegister()}.
 * </p>
 *
 * @param <T>
 *            the GAMA type being serialised and deserialised
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
public class FSTGamaIndividualSerialiser<T> extends FSTBasicObjectSerializer {

	/**
	 * The owning {@link FSTBinarySerialiser}, providing access to the current simulation scope and shared serialisation
	 * state.
	 */
	protected TransientSerializationContext serializationContext;

	/** The gama serializer. */
	protected final IGamaObjectSerializer<T> gamaSerializer;

	/**
	 * Instantiates a new FST individual binarySerialiser.
	 *
	 * @param gamaSerializer
	 *            the gama serializer
	 */
	public FSTGamaIndividualSerialiser(final IGamaObjectSerializer<T> gamaSerializer) {
		this.gamaSerializer = gamaSerializer;
	}

	/**
	 * @param context
	 */
	public void setSerializationContext(final TransientSerializationContext context) {
		serializationContext = context;
	}

	/**
	 * Returns whether the deserialised object should be registered with the FST input stream for back-reference
	 * tracking. Returns {@code true} by default. Subclasses may override this to return {@code false} when reference
	 * tracking is not needed.
	 *
	 * @return {@code true} if the object should be registered after deserialisation
	 */
	protected boolean shouldRegister() {
		return true;
	}

	/**
	 * Instantiates an object by reading it from the FST input stream. Delegates to
	 * {@link #deserialise(IScope, FSTObjectInput)} using the scope from the owning {@link FSTBinarySerialiser}, then
	 * optionally registers the result.
	 *
	 * @param objectClass
	 *            the class of the object to instantiate
	 * @param in
	 *            the FST input stream
	 * @param serializationInfo
	 *            class metadata for the object
	 * @param referencee
	 *            field metadata for the referencing field
	 * @param streamPosition
	 *            the current byte position in the stream
	 * @return the deserialised object of type {@code T}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@SuppressWarnings ("rawtypes")
	@Override
	public final T instantiate(final Class objectClass, final FSTObjectInput in, final FSTClazzInfo serializationInfo,
			final FSTFieldInfo referencee, final int streamPosition) throws Exception {
		T result = gamaSerializer.deserialise(serializationContext.getScope(), in);
		if (shouldRegister()) { in.registerObject(result, streamPosition, serializationInfo, referencee); }
		return result;
	}

	/**
	 * Writes the object to the FST output stream by delegating to {@link #serialise(FSTObjectOutput, Object)}. Any
	 * exception thrown during serialisation is printed to stderr.
	 *
	 * @param out
	 *            the FST output stream
	 * @param toWrite
	 *            the object to write
	 * @param clzInfo
	 *            class metadata
	 * @param referencedBy
	 *            field metadata for the referencing field
	 * @param streamPosition
	 *            the current byte position in the stream
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public void writeObject(final FSTObjectOutput out, final Object toWrite, final FSTClazzInfo clzInfo,
			final FSTFieldInfo referencedBy, final int streamPosition) throws IOException {
		try {
			gamaSerializer.serialise(out, (T) toWrite, serializationContext);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
