/*******************************************************************************************************
 *
 * GamaGeometryFactorySerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and
 * simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import gama.api.runtime.scope.IScope;
import gama.api.utils.geometry.GamaGeometryFactory;
import gama.api.utils.geometry.GeometryUtils;
import gama.extension.serialize.IGamaObjectInput;
import gama.extension.serialize.IGamaObjectOutput;

/**
 * FST serialiser for {@link GamaGeometryFactory} instances. The factory is a singleton; serialisation writes a fixed
 * marker string ({@value #MARKER}) and deserialisation always returns the global singleton via
 * {@link GeometryUtils#getGeometryFactory()}.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class GamaGeometryFactorySerialiser extends FSTIndividualSerialiser<GamaGeometryFactory> {

	/**
	 * Marker string written to the stream to identify the geometry factory placeholder.
	 */
	private static final String MARKER = "*GGF*";

	/**
	 * Serialises the geometry factory by writing the marker string {@value #MARKER}.
	 *
	 * @param out
	 *            the FST output stream
	 * @param o
	 *            the geometry factory (not directly used)
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final IGamaObjectOutput out, final GamaGeometryFactory o) throws Exception {
		out.writeStringUTF(MARKER);
	}

	/**
	 * Deserialises the geometry factory by consuming the marker string and returning the global singleton.
	 *
	 * @param scope
	 *            the current GAMA simulation scope (unused)
	 * @param in
	 *            the FST input stream
	 * @return the global {@link GamaGeometryFactory} singleton
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@Override
	public GamaGeometryFactory deserialise(final IScope scope, final IGamaObjectInput in) throws Exception {
		in.readStringUTF();
		return GeometryUtils.getGeometryFactory();
	}

}
