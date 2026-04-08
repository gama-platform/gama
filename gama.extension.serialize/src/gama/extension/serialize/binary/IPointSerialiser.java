/*******************************************************************************************************
 *
 * IPointSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaPointFactory;
import gama.api.types.geometry.IPoint;
import gama.extension.serialize.IGamaObjectInput;
import gama.extension.serialize.IGamaObjectOutput;

/**
 * FST serialiser for {@link IPoint} instances. Serialises the x, y, and z coordinates, handling {@link Double#NaN} z
 * values via a boolean flag. Objects deserialised by this serialiser are not registered for back-reference tracking.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class IPointSerialiser extends FSTIndividualSerialiser<IPoint> {

	/**
	 * Returns {@code false}: points are not registered for FST back-reference tracking.
	 *
	 * @return {@code false}
	 */
	@Override
	protected boolean shouldRegister() {
		return false;
	}

	/**
	 * Serialises the x, y, and z coordinates of the point. A boolean flag is written before z to indicate whether its
	 * value is {@link Double#NaN}. If z is NaN, {@code 0.0} is written in its place.
	 *
	 * @param out
	 *            the FST output stream
	 * @param toWrite
	 *            the point to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final IGamaObjectOutput out, final IPoint toWrite) throws Exception {
		out.writeDouble(toWrite.getX());
		out.writeDouble(toWrite.getY());
		double z = toWrite.getZ();
		out.writeBoolean(Double.isNaN(z));
		out.writeDouble(Double.isNaN(z) ? 0d : z);
	}

	/**
	 * Deserialises a point by reading x, y, and z coordinates from the stream. If the NaN flag is {@code true}, the z
	 * coordinate is restored as {@link Double#NaN}.
	 *
	 * @param scope
	 *            the current GAMA simulation scope (unused)
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link IPoint}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@Override
	public IPoint deserialise(final IScope scope, final IGamaObjectInput in) throws Exception {
		double x = in.readDouble();
		double y = in.readDouble();
		boolean isNaN = in.readBoolean();
		double z = in.readDouble();
		return GamaPointFactory.create(x, y, isNaN ? Double.NaN : z);
	}

}
