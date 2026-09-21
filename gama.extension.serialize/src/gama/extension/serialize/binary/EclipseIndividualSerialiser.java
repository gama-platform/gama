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

import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.persistence.types.PersistenceStorer;
import org.eclipse.serializer.persistence.types.PersistenceTypeHandler;

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
public abstract class EclipseIndividualSerialiser<T> extends PersistenceTypeHandler.Abstract<T> {

	/**
	 * Instantiates a new eclipse individual serialiser.
	 *
	 * @param type
	 *            the type
	 */
	protected EclipseIndividualSerialiser(Class<T> type) {
		super(type);
	}

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

	/**
	 * Handles references.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean handlesReferences() {
		return shouldRegister();
	}

	/**
	 * Checks for persisted references.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean hasPersistedReferences() {
		return handlesReferences();
	}

	/**
	 * Store.
	 *
	 * @param persistenceStore
	 *            the persistence store
	 * @param t
	 *            the t
	 */
	@Override
	public void store(PersistenceStorer persistenceStore, T t) {
		try {
			IGamaObjectOutput out = new EclipseObjectOutput(persistenceStore);
			serialise(out, t);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates the.
	 *
	 * @param persistenceLoad
	 *            the persistence load
	 * @param persistenceReferenceLoader
	 *            the persistence reference loader
	 * @return the t
	 */
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

	/**
	 * Update state.
	 *
	 * @param persistenceLoad
	 *            the persistence load
	 * @param persistenceReferenceLoader
	 *            the persistence reference loader
	 * @param t
	 *            the t
	 */
	@Override
	public void updateState(PersistenceLoad persistenceLoad, PersistenceReferenceLoader persistenceReferenceLoader,
			T t) {
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
