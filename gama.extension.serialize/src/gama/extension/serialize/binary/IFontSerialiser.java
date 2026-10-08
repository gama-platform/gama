/*******************************************************************************************************
 *
 * IFontSerialiser.java, in gama.extension.serialize, is part of the source code of the GAMA modeling and simulation
 * platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extension.serialize.binary;

import gama.api.runtime.scope.IScope;
import gama.api.types.font.GamaFontFactory;
import gama.api.types.font.IFont;
import gama.extension.serialize.IGamaObjectInput;
import gama.extension.serialize.IGamaObjectOutput;

/**
 * FST serialiser for {@link IFont} instances. Persists the font name, AWT style integer, and point size. On
 * deserialisation, the font is recreated via {@link GamaFontFactory#createFont(String, int, int)}.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 5 août 2023
 */
class IFontSerialiser extends FSTIndividualSerialiser<IFont> {

	/**
	 * Serialises the font name, style, and size.
	 *
	 * @param out
	 *            the FST output stream
	 * @param o
	 *            the font to serialise
	 * @throws Exception
	 *             if serialisation fails
	 */
	@Override
	public void serialise(final IGamaObjectOutput out, final IFont o) throws Exception {
		out.writeStringUTF(o.getName());
		out.writeInt(o.getStyle());
		out.writeInt(o.getSize());
	}

	/**
	 * Deserialises a font by reading its name, style, and size, then constructing it via
	 * {@link GamaFontFactory#createFont(String, int, int)}.
	 *
	 * @param scope
	 *            the current GAMA simulation scope (unused)
	 * @param in
	 *            the FST input stream
	 * @return the deserialised {@link IFont}
	 * @throws Exception
	 *             if deserialisation fails
	 */
	@Override
	public IFont deserialise(final IScope scope, final IGamaObjectInput in) throws Exception {
		return GamaFontFactory.createFont(in.readStringUTF(), in.readInt(), in.readInt());
	}

}
