/*******************************************************************************************************
 *
 * FSTIndividualSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import java.io.IOException;

import gama.api.runtime.scope.IScope;
import gama.extension.serialize.fst.FSTBasicObjectSerializer;
import gama.extension.serialize.fst.FSTClazzInfo;
import gama.extension.serialize.fst.FSTClazzInfo.FSTFieldInfo;
import gama.extension.serialize.fst.FSTObjectInput;
import gama.extension.serialize.fst.FSTObjectOutput;

/**
 * Abstract base class for FST-based individual serialisers used within {@link BinarySerialiser}.
 * Each subclass is responsible for serialising and deserialising a single specific GAMA type.
 * Instances hold a reference to their owning {@link BinarySerialiser} to access the current
 * simulation scope and shared serialisation state (such as the {@code inAgent} flag).
 *
 * <p>Subclasses must implement {@link #deserialise(IScope, FSTObjectInput)} and may optionally
 * override {@link #serialise(FSTObjectOutput, Object)} and {@link #shouldRegister()}.</p>
 *
 * @param <T>
 *            the GAMA type being serialised and deserialised
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
abstract class FSTIndividualSerialiser<T> extends FSTBasicObjectSerializer {

	/**
	 * The owning {@link BinarySerialiser}, providing access to the current simulation scope
	 * and shared serialisation state.
	 */
	protected final BinarySerialiser serialiser;

	/**
	 * Constructs a new {@code FSTIndividualSerialiser} bound to the given {@link BinarySerialiser}.
	 *
	 * @param serialiser
	 *            the owning binary serialiser; must not be {@code null}
	 */
	protected FSTIndividualSerialiser(final BinarySerialiser serialiser) {
		this.serialiser = serialiser;
	}

	/**
	 * Returns whether the deserialised object should be registered with the FST input stream
	 * for back-reference tracking. Returns {@code true} by default.
	 * Subclasses may override this to return {@code false} when reference tracking is not needed.
	 *
	 * @return {@code true} if the object should be registered after deserialisation
	 */
	protected boolean shouldRegister() {
		return true;
	}

	/**
	 * Instantiates an object by reading it from the FST input stream.
	 * Delegates to {@link #deserialise(IScope, FSTObjectInput)} using the scope
	 * from the owning {@link BinarySerialiser}, then optionally registers the result.
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
	public final T instantiate(final Class objectClass, final FSTObjectInput in,
			final FSTClazzInfo serializationInfo, final FSTFieldInfo referencee, final int streamPosition)
			throws Exception {
		T result = deserialise(serialiser.scope, in);
		if (shouldRegister()) { in.registerObject(result, streamPosition, serializationInfo, referencee); }
		return result;
	}

	/**
	 * Writes the object to the FST output stream by delegating to {@link #serialise(FSTObjectOutput, Object)}.
	 * Any exception thrown during serialisation is printed to stderr.
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
			serialise(out, (T) toWrite);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Serialises the given object to the FST output stream.
	 * The default implementation does nothing; subclasses should override this method.
	 *
	 * @param out
	 *            the FST output stream
	 * @param toWrite
	 *            the object to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	public void serialise(final FSTObjectOutput out, final T toWrite) throws Exception {}

	/**
	 * Deserialises an object from the FST input stream using the given simulation scope.
	 *
	 * @param scope
	 *            the current GAMA simulation scope
	 * @param in
	 *            the FST input stream
	 * @return the deserialised object of type {@code T}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	public abstract T deserialise(IScope scope, FSTObjectInput in) throws Exception;

}
