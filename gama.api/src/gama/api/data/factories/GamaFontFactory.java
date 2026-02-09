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
}