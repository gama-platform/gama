/*******************************************************************************************************
 *
 * CoordinateSequenceFactorySerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA
 * modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import org.locationtech.jts.geom.CoordinateSequenceFactory;

import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.GeometryUtils;
import gama.extension.serialize.IGamaObjectInput;
import gama.extension.serialize.IGamaObjectOutput;

/**
 * FST serialiser for {@link CoordinateSequenceFactory} instances. The factory is a singleton obtained from the global
 * geometry factory. Serialisation writes a fixed marker string ({@value #MARKER}); deserialisation always returns the
 * singleton via {@link GeometryUtils#getGeometryFactory()}.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class CoordinateSequenceFactorySerialiser extends FSTIndividualSerialiser<CoordinateSequenceFactory> {

	/**
	 * Marker string written to the stream to identify the coordinate sequence factory placeholder.
	 */
	private static final String MARKER = "*GCSF*";

	/**
	 * Serialises the coordinate sequence factory by writing the marker string {@value #MARKER}.
	 *
	 * @param out
	 *            the FST output stream
	 * @param o
	 *            the factory (not directly used)
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final IGamaObjectOutput out, final CoordinateSequenceFactory o) throws Exception {
		out.writeStringUTF(MARKER);
	}

	/**
	 * Deserialises the coordinate sequence factory by consuming the marker and returning the singleton from the global
	 * geometry factory.
	 *
	 * @param scope
	 *            the current GAMA simulation scope (unused)
	 * @param in
	 *            the FST input stream
	 * @return the global {@link CoordinateSequenceFactory}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@Override
	public CoordinateSequenceFactory deserialise(final IScope scope, final IGamaObjectInput in) throws Exception {
		in.readStringUTF();
		return GeometryUtils.getGeometryFactory().getCoordinateSequenceFactory();
	}

}
