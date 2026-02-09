/*******************************************************************************************************
 *
 * IFontFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.factories;

import java.awt.Font;

import gama.api.data.objects.IFont;

/**
 *
 */
public interface IFontFactory extends IFactory<IFont> {

	/**
	 * Creates a new IFont object from name, style, and size.
	 *
	 * @param name
	 *            the font face name (e.g. "Arial")
	 * @param style
	 *            the font style (e.g. java.awt.Font.BOLD)
	 * @param size
	 *            the font size
	 * @return the created IFont
	 */
	IFont createFont(String name, int style, int size);

	/**
	 * Creates a new IFont object wrapping a java.awt.Font.
	 *
	 * @param awtFont
	 *            the source AWT font
	 * @return the created IFont
	 */
	IFont createFontFrom(Font awtFont);

	/**
	 * Creates a new IFont object from a string definition.
	 *
	 * @param def
	 *            the string definition of the font
	 * @return the created IFont
	 */
	IFont createFont(final String def);

	/**
	 * Creates a new default font with values derived from the default font, but with a specific style.
	 *
	 * @param style
	 *            the new style
	 * @return the created IFont
	 */
	IFont createWithStyle(final int style);

	/**
	 * Creates a new default font with values derived from the default font, but with a specific size.
	 *
	 * @param size
	 *            the new size
	 * @return the created IFont
	 */
	IFont createWithSize(final int size);

	/**
	 * Clone or wrap an existing IFont.
	 * 
	 * @param value
	 *            the source IFont
	 * @return a new IFont
	 */
	IFont createFont(final IFont value);

	/**
	 * Gets the system default font.
	 *
	 * @return the default IFont
	 */
	IFont getDefaultFont();

}
