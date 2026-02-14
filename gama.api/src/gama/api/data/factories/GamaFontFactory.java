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
package gama.api.data.factories;

import java.awt.Font;

import gama.annotations.doc;
import gama.annotations.example;
import gama.annotations.no_test;
import gama.annotations.operator;
import gama.annotations.support.IConcept;
import gama.annotations.support.IOperatorCategory;
import gama.api.constants.IKeyword;
import gama.api.data.objects.IFont;
import gama.api.runtime.scope.IScope;

/**
 * A static factory for creating {@link IFont} instances (representing typefaces used in displays). This class abstracts
 * the font creation process and delegates it to an {@link IFontFactory} implementation.
 */
public class GamaFontFactory implements IFactory<IFont> {

	/**
	 * The internal factory used for creating font instances.
	 */
	private static IFontFactory InternalFactory;

	/**
	 * Configures the internal factory implementation.
	 *
	 * @param builder
	 *            the {@link IFontFactory} to be used as the internal builder.
	 */
	public static void setBuilder(final IFontFactory builder) { InternalFactory = builder; }

	/**
	 * Creates a new font based on the default font but with a specific size.
	 *
	 * @param intValue
	 *            the size of the font (in points).
	 * @return the new {@link IFont} instance.
	 */
	public static IFont createWithSize(final int intValue) {
		return InternalFactory.createWithSize(intValue);
	}

	/**
	 * Creates a copy of an existing {@link IFont}.
	 *
	 * @param f
	 *            the source font.
	 * @return a new {@link IFont} instance identical to the source.
	 */
	public static IFont createFont(final IFont f) {
		return InternalFactory.createFont(f);
	}

	/**
	 * Creates a new font from a string definition.
	 *
	 * @param def
	 *            the font definition string (e.g., "Helvetica", "Arial-BOLD-12").
	 * @return the corresponding {@link IFont} instance.
	 */
	public static IFont createFont(final String def) {
		return InternalFactory.createFont(def);
	}

	/**
	 * Retrieves the default font for the platform.
	 *
	 * @return the default {@link IFont} instance.
	 */
	public static IFont getDefaultFont() { return InternalFactory.getDefaultFont(); }

	/**
	 * Creates a new font with specific attributes.
	 *
	 * @param name
	 *            the name of the font family (e.g., "Arial").
	 * @param style
	 *            the font style (e.g., bold, italic). See {@link java.awt.Font} constants or GAMA equivalents.
	 * @param size
	 *            the font size in points.
	 * @return the created {@link IFont} instance.
	 */
	public static IFont createFont(final String name, final int style, final int size) {
		return InternalFactory.createFont(name, style, size);
	}

	/**
	 * @param scope
	 * @param obj
	 * @param copy
	 * @return
	 */
	public static IFont createFontFrom(final IScope scope, final Object obj, final boolean copy) {
		if (obj == null) return getDefaultFont();
		return switch (obj) {
			case Number size -> {
				yield createWithSize(size.intValue());
			}
			case IFont f -> {
				if (copy) { yield createFont(f); }
				yield f;
			}
			case String s -> {
				yield createFont(s);
			}
			default -> {
				yield getDefaultFont();
			}
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
	public static IFont withSize(final IFont font, final Integer size) {
		return createFont(font.getName(), font.getStyle(), size);
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
	public static IFont withStyle(final IFont font, final Integer style) {
		return createFont(font.getName(), style, font.getSize());
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
		return createFont(name, Font.PLAIN, size);
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
		return createFont(name, style, size);
	}

}