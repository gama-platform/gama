/*******************************************************************************************************
 *
 * EclipseIndividualSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import org.eclipse.serializer.persistence.types.TypeHandler;
import org.eclipse.serializer.persistence.types.PersistenceStore;
import org.eclipse.serializer.persistence.types.PersistenceLoad;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandlerPlugin;

import gama.api.runtime.scope.IScope;
import gama.extension.serialize.IGamaObjectInput;
import gama.extension.serialize.IGamaObjectOutput;

/**
 * Abstract base class for Eclipse Serializer-based individual serialisers used within {@link BinarySerialiser}. Each
 * subclass is responsible for serialising and deserialising a single specific GAMA type. Instances hold a reference to
 * their owning {@link BinarySerialiser} to access the current simulation scope and shared serialisation state.
 *
 * @param <T>
 *            the GAMA type being serialised and deserialised
 */
public abstract class EclipseIndividualSerialiser<T> implements TypeHandler<T> {

	/**
	 * The owning {@link BinarySerialiser}, providing access to the current simulation scope and shared serialisation
	 * state.
	 */
	protected BinarySerialiser serialiser;

	/**
	 * Constructs a new {@code EclipseIndividualSerialiser} bound to the given {@link BinarySerialiser}.
	 *
	 * @param serialiser
	 *            the owning binary serialiser; must not be {@code null}
	 */
	public void setBinarySerialiser(final BinarySerialiser serialiser) { this.serialiser = serialiser; }

	/**
	 * Returns whether the deserialised object should be registered for reference tracking. Returns {@code true} by
	 * default. Subclasses may override this to return {@code false} when reference tracking is not needed.
	 *
	 * @return {@code true} if the object should be tracked
	 */
	protected boolean shouldRegister() {
		return true;
	}

	@Override
	public boolean handlesReferences() {
		return shouldRegister();
	}

	@Override
	public boolean hasPersistedReferences() {
		return handlesReferences();
	}

	@Override
	public void store(PersistenceStore persistenceStore, T t) {
		try {
			IGamaObjectOutput out = new EclipseObjectOutput(persistenceStore);
			serialise(out, t);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public T create(PersistenceLoad persistenceLoad, PersistenceReferenceLoader persistenceReferenceLoader) {
		try {
			IGamaObjectInput in = new EclipseObjectInput(persistenceLoad, persistenceReferenceLoader);
			return deserialise(serialiser.scope, in);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void updateState(PersistenceLoad persistenceLoad, PersistenceReferenceLoader persistenceReferenceLoader, T t) {
		// Used for resolving self-references or lazy load state update if needed.
	}


	/**
	 * Serialises the given object to the output stream.
	 *
	 * @param out
	 *            the GAMA output stream wrapper
	 * @param toWrite
	 *            the object to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	public void serialise(final IGamaObjectOutput out, final T toWrite) throws Exception {}

	/**
	 * Deserialises an object from the input stream using the given simulation scope.
	 *
	 * @param scope
	 *            the current GAMA simulation scope
	 * @param in
	 *            the GAMA input stream wrapper
	 * @return the deserialised object of type {@code T}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	public abstract T deserialise(IScope scope, IGamaObjectInput in) throws Exception;

}
