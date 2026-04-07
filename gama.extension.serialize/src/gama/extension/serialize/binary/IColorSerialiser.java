/*******************************************************************************************************
 *
 * IObjectSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import gama.api.runtime.scope.IScope;
import gama.api.types.color.GamaColorFactory;
import gama.api.types.color.IColor;
import gama.extension.serialize.fst.FSTObjectInput;
import gama.extension.serialize.fst.FSTObjectOutput;

/**
 * FST serialiser for {@link IObject} instances. Serialises the species name and the full attribute map of the object.
 * On deserialisation, the species class is looked up in the model and a new instance is created. Objects deserialised
 * by this serialiser are not registered for back-reference tracking.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class IColorSerialiser extends FSTIndividualSerialiser<IColor> {

	/**
	 * Constructs a new {@code IObjectSerialiser} bound to the given {@link BinarySerialiser}.
	 *
	 * @param serialiser
	 *            the owning binary serialiser
	 */
	IColorSerialiser(final BinarySerialiser serialiser) {
		super(serialiser);
	}

	/**
	 * Returns {@code false}: objects are not registered for FST back-reference tracking.
	 *
	 * @return {@code false}
	 */
	@Override
	protected boolean shouldRegister() {
		return false;
	}

	/**
	 * Serialises the species name and the attribute map of the object.
	 *
	 * @param out
	 *            the FST output stream
	 * @param o
	 *            the object to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final FSTObjectOutput out, final IColor o) throws Exception {
		out.write(o.red());
		out.write(o.green());
		out.write(o.blue());
		out.write(o.alpha());
	}

	/**
	 * Deserialises an object by reading its species name and attributes, then creating a new instance via the model's
	 * class registry. Returns {@code null} if the class cannot be found.
	 *
	 * @param scope
	 *            the current GAMA simulation scope
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link IObject}, or {@code null} if the class is unknown
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public IColor deserialise(final IScope scope, final FSTObjectInput in) throws Exception {
		// Use in.read() (readIntByte) instead of in.readInt() (readFInt): writeFByte writes a raw single byte,
		// so readIntByte correctly returns the unsigned value 0-255, whereas readFInt interprets byte 0xFF (-1)
		// as integer -1, which normalize() then clamps to 0, corrupting the alpha channel.
		return GamaColorFactory.createWithRGBA(in.read(), in.read(), in.read(), in.read());
	}

}
