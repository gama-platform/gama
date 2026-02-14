/*******************************************************************************************************
 *
 * GamaFont.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util;

import java.awt.Font;

import gama.api.data.factories.GamaFontFactory;
import gama.api.data.json.IJson;
import gama.api.data.json.IJsonValue;
import gama.api.data.objects.IFont;
import gama.api.exceptions.GamaRuntimeException;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;

/**
 * Class GamaFont. A simple wrapper on an AWT Font
 *
 * @author drogoul
 * @since 22 mars 2015
 *
 */
public class GamaFont extends Font implements IFont {

	/**
	 * @param name
	 * @param style
	 * @param size
	 */
	public GamaFont(final String name, final int style, final int size) {
		super(name, style, size);
	}

	/**
	 * Instantiates a new gama font.
	 *
	 * @param font
	 *            the font
	 */
	public GamaFont(final Font font) {
		super(font);
	}

	@Override
	public String getName() { return name; }

	@Override

	public int getSize() { return size; }

	@Override

	public int getStyle() { return style; }

	/**
	 * Method serialize()
	 *
	 * @see gama.api.utils.IGamlable#serializeToGaml(boolean)
	 */
	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		String strStyle;

		if (isBold()) {
			strStyle = isItalic() ? "#bold + #italic" : "#bold";
		} else {
			strStyle = isItalic() ? "#italic" : "#plain";
		}

		return "font('" + name + "'," + pointSize + "," + strStyle + ")";
	}

	/**
	 * Method getType()
	 *
	 * @see gama.api.gaml.types.ITyped#getGamlType()
	 */
	@Override
	public IType<?> getGamlType() { return Types.FONT; }

	/**
	 * Method stringValue(). Outputs to a format that is usable by Font.decode(String);
	 *
	 * @see gama.api.data.objects.IValue#stringValue(gama.api.runtime.scope.IScope)
	 */
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return toString();
	}

	@Override
	public String toString() {
		String strStyle;
		if (isBold()) {
			strStyle = isItalic() ? "bolditalic" : "bold";
		} else {
			strStyle = isItalic() ? "italic" : "plain";
		}
		return name + "-" + strStyle + "-" + size;
	}

	/**
	 * Method copy()
	 *
	 * @see gama.api.data.objects.IValue#copy(gama.api.runtime.scope.IScope)
	 */
	@Override
	public IFont copy(final IScope scope) throws GamaRuntimeException {
		return GamaFontFactory.createFont(name, style, size);
	}

	@Override
	public int intValue(final IScope scope) {
		return getSize();
	}

	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType(), "name", this.name, "style", this.style, "size", this.size);
	}

	@Override
	public Font getAwtFont() { return this; }

}
