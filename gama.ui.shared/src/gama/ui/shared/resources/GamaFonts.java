/*******************************************************************************************************
 *
 * GamaFonts.java, in gama.ui.shared, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.shared.resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import gama.core.util.GamaFont;
import gama.ui.shared.utils.WorkbenchHelper;

/**
 *
 */
public class GamaFonts {

    /** The max size. */
    static int MAX_SIZE = 512;

    /** The min size. */
    static int MIN_SIZE = 6;

    /** The cache. */
    static LoadingCache<FontData, Font> CACHE = CacheBuilder.newBuilder().build(new CacheLoader<FontData, Font>() {

	@Override
	public Font load(final FontData key) throws Exception {
	    return new Font(WorkbenchHelper.getDisplay(), key);
	}
    });

    /**
     * Gets the font.
     *
     * @param data
     *            the data
     * @return the font
     */
    public static Font getFont(final FontData data) {
	if (data == null) {
	    return null;
	}
	return CACHE.getUnchecked(data);
    }

    /**
     * Gets the font.
     *
     * @param font
     *            the font
     * @return the font
     */
    public static Font getFont(final GamaFont font) {
	if (font == null) {
	    return null;
	}
	return getFont(new FontData(font.getName(), font.getSize(), font.getStyle()));
    }

    /**
     * Gets the font.
     *
     * @param font
     *            the font
     * @return the font
     */
    public static GamaFont getFont(final Font font) {
	FontData data = font.getFontData()[0];
	return new GamaFont(data.getName(), (int) data.height, data.getStyle());
    }

    /**
     * With size.
     *
     * @param font
     *            the font
     * @param newSize
     *            the new size
     * @return the font
     */
    public static Font withSize(final Font font, final int wantedSize) {
	return getFont(withSize(font.getFontData()[0], wantedSize));
    }

    /**
     * With size.
     *
     * @return the font data
     */
    private static FontData withSize(final FontData data, final int wantedSize) {
	int newSize = cap(wantedSize);
	if (newSize != data.getHeight()) {
	    data.setHeight(newSize);
	}
	return data;
    }

    /**
     * With magnification.
     *
     * @param font
     *            the font
     * @param magnification
     *            the magnification
     * @return the font
     */
    public static Font withMagnification(final Font font, final int magnification) {
	FontData data = font.getFontData()[0];
	return getFont(withSize(data, data.getHeight() + magnification));
    }

    /**
     * @param i
     * @return
     */
    private static int cap(final int i) {
	return Math.min(MAX_SIZE, Math.max(MIN_SIZE, i));
    }

    /**
     * In bold.
     *
     * @param font
     *            the font
     * @param wantedSize
     *            the wanted size
     * @return the font
     */
    public static Font inBold(final Font font) {
	FontData bold = font.getFontData()[0];
	bold.setStyle(bold.getStyle() | SWT.BOLD);
	return getFont(bold);
    }

    /**
     * In italic.
     *
     * @param font
     *            the font
     * @return the font
     */
    public static Font inItalic(final Font font) {
	FontData italic = font.getFontData()[0];
	italic.setStyle(italic.getStyle() | SWT.ITALIC);
	return getFont(italic);
    }

}
