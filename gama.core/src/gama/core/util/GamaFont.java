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

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.api.constants.IKeyword;
import gama.api.data.json.IJson;
import gama.api.data.json.IJsonValue;
import gama.api.data.objects.IFont;
import gama.api.data.objects.IValue;
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
	public IValue copy(final IScope scope) throws GamaRuntimeException {
		return new GamaFont(name, style, size);
	}

	/**
	 * Font.
	 *
	 * @param name
	 *            the name
	 * @param size
	 *            the size
	 * @param style
	 *            the style
	 * @return the gama font
	 */
	@operator (
			value = IKeyword.FONT,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.TEXT, IConcept.DISPLAY },
			can_be_const = true)
	@doc (
			value = "Creates a new font, by specifying its name (either a font face name like 'Lucida Grande Bold' or 'Helvetica', or a logical name like 'Dialog', 'SansSerif', 'Serif', etc.), a size in points and a style, either #bold, #italic or #plain or a combination (addition) of them.",
			masterDoc = true,
			examples = @example (
					value = "font ('Helvetica Neue',12, #bold + #italic)",
					equals = "a bold and italic face of the Helvetica Neue family",
					test = false))
	@no_test
	public static IFont font(final String name, final Integer size, final Integer style) {
		return new GamaFont(name, style, size);
	}

	/**
	 * With size.
	 *
	 * @param font
	 *            the font
	 * @param size
	 *            the size
	 * @return the gama font
	 */
	@operator (
			value = "with_size",
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.TEXT, IConcept.DISPLAY },
			can_be_const = true)
	@doc (
			value = "Creates a new font from an existing font, with a new size in points",
			masterDoc = true,
			examples = @example (
					value = "font ('Helvetica Neue',12, #bold + #italic) with_size 24",
					equals = "a bold and italic face of the Helvetica Neue family with a size of 24 points",
					test = false))
	@no_test
	public static IFont withSize(final GamaFont font, final Integer size) {
		return new GamaFont(font.name, font.style, size);
	}

	/**
	 * With size.
	 *
	 * @param font
	 *            the font
	 * @param size
	 *            the size
	 * @return the gama font
	 */
	@operator (
			value = "with_style",
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.TEXT, IConcept.DISPLAY },
			can_be_const = true)
	@doc (
			value = "Creates a new font from an existing font, with a new style: either #bold, #italic or #plain or a combination (addition) of them.",
			masterDoc = true,
			examples = @example (
					value = "font ('Helvetica Neue',12, #bold + #italic) with_style #plain",
					equals = "a plain face of the Helvetica Neue family with a size of 12 points",
					test = false))
	@no_test
	public static IFont withStyle(final GamaFont font, final Integer style) {
		return new GamaFont(font.name, style, font.size);
	}

	/**
	 * Font.
	 *
	 * @param name
	 *            the name
	 * @param size
	 *            the size
	 * @return the gama font
	 */
	@operator (
			value = IKeyword.FONT,
			category = { IOperatorCategory.CASTING },
			concept = { IConcept.TEXT, IConcept.DISPLAY },
			can_be_const = true)
	@doc (
			value = "Creates a new font, by specifying its name (either a font face name like 'Lucida Grande Bold' or 'Helvetica', or a logical name like 'Dialog', 'SansSerif', 'Serif', etc.) and a size in points. No style is attached to this font")
	@no_test
	public static IFont font(final String name, final Integer size) {
		return new GamaFont(name, Font.PLAIN, size);
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
