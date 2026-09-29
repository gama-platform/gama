/**
 *
 */
package gama.extension.serialize;

import gama.api.runtime.scope.IScope;

/**
 *
 */
public interface GamaObjectSerializer<T> {

	/**
	 * Serialises the given object to the FST output stream. The default implementation does nothing; subclasses should
	 * override this method.
	 *
	 * @param out
	 *            the FST output stream
	 * @param toWrite
	 *            the object to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	void serialise(final IGamaObjectOutput out, final T toWrite) throws Exception;

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
	T deserialise(IScope scope, IGamaObjectInput in) throws Exception;

	/**
	 * @return
	 */
	default boolean shouldRegister() {
		return true;
	}

}
