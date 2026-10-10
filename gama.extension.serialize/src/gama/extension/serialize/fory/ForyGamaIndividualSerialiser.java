/*******************************************************************************************************
 *
 * ForyGamaIndividualSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.fory;

import org.apache.fory.config.Config;
import org.apache.fory.context.ReadContext;
import org.apache.fory.context.WriteContext;
import org.apache.fory.serializer.Serializer;

import gama.api.exceptions.GamaRuntimeException;
import gama.api.kernel.serialization.AbstractBinarySerializer.TransientSerializationContext;
import gama.api.kernel.serialization.IGamaObjectSerializer;

/**
 * Adapts an {@link IGamaObjectSerializer} to an Apache Fory {@link Serializer}. This is the Fory counterpart of
 * {@code FSTGamaIndividualSerialiser}.
 *
 * @param <T>
 *            the GAMA type being serialised and deserialised
 */
public class ForyGamaIndividualSerialiser<T> extends Serializer<T> {

	/** The serialisation context shared with the owning binary serialiser. */
	private final TransientSerializationContext serializationContext;

	/** The GAMA serialiser to which the actual work is delegated. */
	private final IGamaObjectSerializer<T> gamaSerializer;

	/**
	 * Instantiates a new adapter. Objects for which {@link IGamaObjectSerializer#shouldRegister()} returns
	 * {@code false} do not participate in reference tracking.
	 *
	 * @param config
	 *            the Fory configuration
	 * @param type
	 *            the type handled by the serialiser
	 * @param gamaSerializer
	 *            the GAMA serialiser to adapt
	 * @param context
	 *            the serialisation context giving access to the current scope
	 */
	public ForyGamaIndividualSerialiser(final Config config, final Class<T> type,
			final IGamaObjectSerializer<T> gamaSerializer, final TransientSerializationContext context) {
		super(config, type, config.trackingRef() && gamaSerializer.shouldRegister(), false);
		this.gamaSerializer = gamaSerializer;
		this.serializationContext = context;
	}

	@Override
	public void write(final WriteContext context, final T value) {
		try {
			gamaSerializer.serialise(new ForyObjectOutput(context), value, serializationContext);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw GamaRuntimeException.create(e, serializationContext.getScope());
		}
	}

	@Override
	public T read(final ReadContext context) {
		try {
			return gamaSerializer.deserialise(serializationContext.getScope(), new ForyObjectInput(context));
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw GamaRuntimeException.create(e, serializationContext.getScope());
		}
	}

}
