/*******************************************************************************************************
 *
 * InternalGamaFontFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util;

import static gama.api.utils.prefs.GamaPreferences.Displays.DEFAULT_DISPLAY_FONT;

import java.awt.Font;

import gama.api.data.factories.IFontFactory;
import gama.api.data.objects.IFont;

/**
 *
 */
public class InternalGamaFontFactory implements IFontFactory {

	/**
	 * Gets the default font.
	 *
	 * @return the default font
	 */
	@Override
	public IFont getDefaultFont() {

		return DEFAULT_DISPLAY_FONT.getValue();

	}

	/**
	 * @param value
	 * @return
	 */
	@Override
	public IFont createFont(final IFont value) {
		return new GamaFont(value.getName(), value.getStyle(), value.getSize());
	}

	@Override
	public IFont createFont(final String name, final int style, final int size) {
		return new GamaFont(name, style, size);
	}

	@Override
	public IFont createFontFrom(final Font awtFont) {
		return new GamaFont(awtFont.getFontName(), awtFont.getStyle(), awtFont.getSize());
	}

	@Override
	public IFont createFont(final String def) {
		return createFontFrom(Font.decode(def));
	}

	/**
	 * Creates a new InternalGamaFont object.
	 *
	 * @param size
	 *            the size
	 * @return the i font
	 */
	@Override
	public IFont createWithSize(final int size) {
		return new GamaFont(getDefaultFont().getName(), getDefaultFont().getStyle(), size);
	}

	/**
	 * Creates a new InternalGamaFont object.
	 *
	 * @param style
	 *            the style
	 * @return the i font
	 */
	@Override
	public IFont createWithStyle(final int style) {
		return new GamaFont(getDefaultFont().getName(), style, getDefaultFont().getSize());
	}

}
