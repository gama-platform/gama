/*******************************************************************************************************
 *
 * GamaFontFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.font;

import static gama.api.utils.prefs.GamaPreferences.Displays.DEFAULT_DISPLAY_FONT;

import java.awt.Font;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.api.constants.IKeyword;
import gama.api.runtime.scope.IScope;

/**
 * A static factory for creating {@link IFont} instances (representing typefaces used in displays). This class abstracts
 * the font creation process .
 */
public class GamaFontFactory {

	/**
	 * Gets the default font.
	 *
	 * @return the default font
	 */
	public static IFont getDefaultFont() { return DEFAULT_DISPLAY_FONT.getValue(); }

	/**
	 * Creates a copy of an existing {@link IFont}.
	 *
	 * @param f
	 *            the source font.
	 * @return a new {@link IFont} instance identical to the source.
	 */
	public static IFont cloneFont(final IFont value) {
		return new GamaFont(value.getName(), value.getStyle(), value.getSize());
	}

	/**
	 * Creates a new font from a string definition.
	 *
	 * @param def
	 *            the font definition string (e.g., "Helvetica", "Arial-BOLD-12").
	 * @return the corresponding {@link IFont} instance.
	 */
	public static IFont createFontFrom(final String def) {
		return createFontFrom(Font.decode(def));
	}

	/**
	 * Creates a new GamaFont object.
	 *
	 * @param awtFont
	 *            the awt font
	 * @return the i font
	 */
	public static IFont createFontFrom(final Font awtFont) {
		return new GamaFont(awtFont.getFontName(), awtFont.getStyle(), awtFont.getSize());
	}

	/**
	 * @param scope
	 * @param obj
	 * @param copy
	 * @return
	 */
	public static IFont castToFont(final IScope scope, final Object obj, final boolean copy) {
		return switch (obj) {
			case Number size -> cloneWithSize(getDefaultFont(), size.intValue());
			case IFont f -> copy ? cloneFont(f) : f;
			case String s -> createFontFrom(s);
			case null, default -> getDefaultFont();
		};
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
	public static IFont cloneWithSize(final IFont font, final Integer size) {
		return new GamaFont(font.getName(), font.getStyle(), size);
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
	public static IFont cloneWithStyle(final IFont font, final Integer style) {
		return new GamaFont(font.getName(), style, font.getSize());
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
	public static IFont createFont(final String name, final Integer size) {
		return new GamaFont(name, Font.PLAIN, size);
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
	public static IFont createFont(final String name, final Integer size, final Integer style) {
		return new GamaFont(name, style, size);
	}

}