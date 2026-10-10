/*******************************************************************************************************
 *
 * IDateSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

import gama.api.kernel.serialization.AbstractBinarySerializer.TransientSerializationContext;
import gama.api.kernel.serialization.IGamaObjectInput;
import gama.api.kernel.serialization.IGamaObjectOutput;
import gama.api.kernel.serialization.IGamaObjectSerializer;
import gama.api.runtime.scope.IScope;
import gama.api.types.date.GamaDateFactory;
import gama.api.types.date.IDate;

/**
 * Binary serialiser for {@link IDate} instances. Dates are immutable (their implementation is a record), so their
 * fields cannot be set by reflection when reading them back: the temporal value they hold is written instead, as
 * its kind followed by its ISO representation, and a new date is created from it on deserialisation.
 */
public class IDateSerialiser implements IGamaObjectSerializer<IDate> {

	/** The kinds of temporal values a date can hold. */
	private static final int ZONED = 0, OFFSET = 1, LOCAL = 2;

	/**
	 * Returns {@code false}: dates are not registered for FST back-reference tracking.
	 *
	 * @return {@code false}
	 */
	@Override
	public boolean shouldRegister() {
		return false;
	}

	/**
	 * Serialises the kind of the temporal value held by the date and its ISO representation.
	 *
	 * @param out
	 *            the FST output stream
	 * @param o
	 *            the date to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final IGamaObjectOutput out, final IDate o,
			final TransientSerializationContext context) throws Exception {
		final Temporal temporal = o.getTemporal();
		switch (temporal) {
			case ZonedDateTime zoned -> {
				out.write(ZONED);
				out.writeStringUTF(zoned.toString());
			}
			case OffsetDateTime offset -> {
				out.write(OFFSET);
				out.writeStringUTF(offset.toString());
			}
			default -> {
				out.write(LOCAL);
				out.writeStringUTF(o.getLocalDateTime().toString());
			}
		}
	}

	/**
	 * Deserialises a date by reading the kind of its temporal value and its ISO representation.
	 *
	 * @param scope
	 *            the current GAMA simulation scope
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link IDate}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@Override
	public IDate deserialise(final IScope scope, final IGamaObjectInput in) throws Exception {
		final int kind = in.read();
		final String iso = in.readStringUTF();
		final Temporal temporal = switch (kind) {
			case ZONED -> ZonedDateTime.parse(iso);
			case OFFSET -> OffsetDateTime.parse(iso);
			default -> LocalDateTime.parse(iso);
		};
		return GamaDateFactory.createFromTemporal(scope, temporal);
	}

}
