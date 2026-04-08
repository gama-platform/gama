/*******************************************************************************************************
 *
 * UniqueCoordinateSequenceSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling
 * and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.UniqueCoordinateSequence;
import gama.extension.serialize.IGamaObjectInput;
import gama.extension.serialize.IGamaObjectOutput;

/**
 * FST serialiser for {@link UniqueCoordinateSequence} instances. A {@code UniqueCoordinateSequence} holds exactly one
 * coordinate; serialisation persists its x, y, and z components at index 0.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class UniqueCoordinateSequenceSerialiser extends FSTIndividualSerialiser<UniqueCoordinateSequence> {

	/**
	 * Serialises the x, y, and z values of the single coordinate at index 0.
	 *
	 * @param out
	 *            the FST output stream
	 * @param o
	 *            the coordinate sequence to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final IGamaObjectOutput out, final UniqueCoordinateSequence o) throws Exception {
		out.writeDouble(o.getX(0));
		out.writeDouble(o.getY(0));
		out.writeDouble(o.getZ(0));
	}

	/**
	 * Deserialises a unique coordinate sequence by reading x, y, and z values from the stream.
	 *
	 * @param scope
	 *            the current GAMA simulation scope (unused)
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link UniqueCoordinateSequence}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@Override
	public UniqueCoordinateSequence deserialise(final IScope scope, final IGamaObjectInput in) throws Exception {
		return new UniqueCoordinateSequence(in.readDouble(), in.readDouble(), in.readDouble());
	}

}
