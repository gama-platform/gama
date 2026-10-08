/*******************************************************************************************************
 *
 * ColorUtil.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * The Class ColorUtil.
 */
public class ColorUtil {

	/**
	 * Returns a web color in format "#RRGGBB"
	 * 
	 * @param color
	 * @return web color as string
	 */
	public static String convertToHexColor(final Color color) {
		if (color == null) return null;
		return convertToHexColor(color.getRGB());
	}

	/**
	 * Convert to hex color.
	 *
	 * @param rgb
	 *            the rgb
	 * @return the string
	 */
	public static String convertToHexColor(final RGB rgb) {
		if (rgb == null) return null;
		String hex = String.format("#%02x%02x%02x", rgb.red, rgb.green, rgb.blue);
		return hex;
	}
}
