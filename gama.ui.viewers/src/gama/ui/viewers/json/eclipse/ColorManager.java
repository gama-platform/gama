/*******************************************************************************************************
 *
 * ColorManager.java, in gama.ui.viewers, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2025 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.viewers.json.eclipse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * The Class ColorManager.
 */
public class ColorManager {

	/** The standalone. */
	private static ColorManager standalone;

	/** The color table. */
	private final Map<RGB, Color> fColorTable = new HashMap<>(10);

	/**
	 * Instantiates a new color manager.
	 */
	public ColorManager() {

	}

	/**
	 * @return color manager for standalone SWT programs, never <code>null</code>.
	 * @throws IllegalStateException
	 *             when no standalone color manager set but used
	 */
	public static ColorManager getStandalone() {
		if (standalone == null) throw new IllegalStateException("no standalone color manager set.");
		return standalone;
	}

	/**
	 * Set color manager for standalone SWT programs
	 * 
	 * @param standalone
	 */
	public static void setStandalone(final ColorManager standalone) { // NO_UCD (test only)
		ColorManager.standalone = standalone;
	}

	/**
	 * Dispose.
	 */
	public void dispose() {
		Iterator<Color> e = fColorTable.values().iterator();
		while (e.hasNext()) { e.next().dispose(); }
	}

	/**
	 * Gets the color.
	 *
	 * @param rgb
	 *            the rgb
	 * @return the color
	 */
	public Color getColor(final RGB rgb) {
		Color color = fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}

}
