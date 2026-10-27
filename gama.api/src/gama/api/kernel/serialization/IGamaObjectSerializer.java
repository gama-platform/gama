/**
 *
 */
package gama.api.kernel.serialization;

import gama.api.kernel.serialization.AbstractBinarySerializer.TransientSerializationContext;
import gama.api.runtime.scope.IScope;

/**
 *
 */
public interface IGamaObjectSerializer<T> {

	/**
	 * Serialises the given object to the binary output stream.
	 *
	 * @param out
	 *            the binary output stream
	 * @param toWrite
	 *            the object to serialise
	 * @param context
	 *            the current serialisation context
	 * @throws Exception
	 *             if serialisation fails
	 */
	void serialise(final IGamaObjectOutput out, final T toWrite, TransientSerializationContext context)
			throws Exception;

	/**
	 * Deserialises an object from the binary input stream using the given simulation scope.
	 *
	 * @param scope
	 *            the current GAMA simulation scope
	 * @param in
	 *            the binary input stream
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
