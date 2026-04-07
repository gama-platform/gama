/*******************************************************************************************************
 *
 * IShapeSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import org.locationtech.jts.geom.Geometry;

import gama.api.runtime.scope.IScope;
import gama.api.types.geometry.GamaShapeFactory;
import gama.api.types.geometry.IShape;
import gama.api.types.geometry.IShape.Type;
import gama.extension.serialize.fst.FSTObjectInput;
import gama.extension.serialize.fst.FSTObjectOutput;

/**
 * FST serialiser for {@link IShape} instances.
 * Serialises the depth value, the geometrical type ordinal, and the underlying JTS {@link Geometry}.
 * Objects deserialised by this serialiser are not registered for back-reference tracking.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class IShapeSerialiser extends FSTIndividualSerialiser<IShape> {

	/**
	 * Constructs a new {@code IShapeSerialiser} bound to the given {@link BinarySerialiser}.
	 *
	 * @param serialiser
	 *            the owning binary serialiser
	 */
	IShapeSerialiser(final BinarySerialiser serialiser) {
		super(serialiser);
	}

	/**
	 * Returns {@code false}: shapes are not registered for FST back-reference tracking.
	 *
	 * @return {@code false}
	 */
	@Override
	protected boolean shouldRegister() {
		return false;
	}

	/**
	 * Serialises the shape's depth (as {@code 0.0} if null), its geometrical type ordinal,
	 * and its inner JTS {@link Geometry}.
	 *
	 * @param out
	 *            the FST output stream
	 * @param toWrite
	 *            the shape to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final FSTObjectOutput out, final IShape toWrite) throws Exception {
		Double d = toWrite.getDepth();
		IShape.Type t = toWrite.getGeometricalType();
		out.writeDouble(d == null ? 0d : d);
		out.writeInt(t.ordinal());
		out.writeObject(toWrite.getInnerGeometry());
	}

	/**
	 * Deserialises a shape by reading its depth, geometrical type, and inner JTS geometry.
	 * Depth and type are only applied if they carry meaningful (non-default) values.
	 *
	 * @param scope
	 *            the current GAMA simulation scope (unused)
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link IShape}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@Override
	public IShape deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
		double d = in.readDouble();
		IShape.Type t = IShape.Type.values()[in.readInt()];
		IShape result = GamaShapeFactory.createFrom((Geometry) in.readObject());
		if (d > 0d) { result.setDepth(d); }
		if (t != Type.NULL) { result.setGeometricalType(t); }
		return result;
	}

}
