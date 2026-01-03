/*******************************************************************************************************
 *
 * GamaColorFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import gama.annotations.precompiler.constants.ColorCSS;
import gama.core.runtime.IScope;
import gama.core.util.map.GamaMapFactory;

/**
 *
 */
public class GamaColorFactory {

	/** The Constant TRANSPARENT. */
	public static final GamaColor TRANSPARENT = get("transparent", 0, 0, 0, 0);

	/** The Constant BLACK. */
	public static final GamaColor BLACK = get("black", 0, 0, 0);

	/** The Constant WHITE. */
	public static final GamaColor WHITE = get("white", 255, 255, 255);

	/** The Constant RED. */
	public static final GamaColor RED = get("red", 255, 0, 0);

	/** The Constant GREEN. */
	public static final GamaColor GREEN = get("green", 0, 255, 0);

	/** The Constant BLUE. */
	public static final GamaColor BLUE = get("blue", 0, 0, 255);

	/** The Constant YELLOW. */
	public static final GamaColor YELLOW = get("yellow", 255, 255, 0);

	/** The Constant LIGHT_GRAY. */
	public static final GamaColor LIGHT_GRAY = get("lightgray", 211, 211, 211);

	/** The Constant GRAY. */
	public static final GamaColor GRAY = get("gray", 128, 128, 128);
	// Add more colors as needed

	/**
	 * The Class NamedGamaColor.
	 */
	public static class NamedGamaColor extends GamaColor {

		/**
		 * Instantiates a new named gama color.
		 *
		 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
		 * @param name
		 *            the name.
		 * @param rgba
		 *            the rgba
		 * @date 20 août 2023
		 */
		NamedGamaColor(final String name, final int... rgba) {
			super(rgba[0], rgba[1], rgba[2], rgba[3]);
			this.name = name;
		}

		/** The name. */
		final String name;

		@Override
		public String toString() {
			return "color[" + name + "]";
		}

		@Override
		public String serializeToGaml(final boolean includingBuiltIn) {
			return "#" + name;
		}

		@Override
		public String stringValue(final IScope scope) {
			return name;
		}

	}

	/** The Constant array. */
	public final static Object[] array = ColorCSS.array;

	/** The Constant colors. */
	public final static Map<String, GamaColor> colors = GamaMapFactory.createUnordered();

	/** The Constant int_colors. */
	public final static Map<Integer, GamaColor> int_colors = Collections.synchronizedMap(new HashMap<>());

	static {
		for (int i = 0; i < array.length; i += 2) {
			final GamaColor color = GamaColorFactory.get((String) array[i], (int[]) array[i + 1]);
			colors.put((String) array[i], color);
			int_colors.put(color.getRGB(), color);
		}
		// A.G add the GAMA Color corresponding to the GAMA 1.9 Logo
		final GamaColor orange = GamaColorFactory.get("gamaorange", 244, 165, 40, 255);
		colors.put("gamaorange", orange);
		int_colors.put(orange.getRGB(), orange);

		final GamaColor red = GamaColorFactory.get("gamared", 217, 72, 33, 255);
		colors.put("gamared", red);
		int_colors.put(red.getRGB(), red);

		final GamaColor blue = GamaColorFactory.get("gamablue", 22, 94, 147, 255);
		colors.put("gamablue", blue);
		int_colors.put(blue.getRGB(), blue);

		final GamaColor green = GamaColorFactory.get("gamagreen", 81, 135, 56, 255);
		colors.put("gamagreen", green);
		int_colors.put(green.getRGB(), green);
	}

	/**
	 * Gets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param rgb
	 *            the rgb
	 * @return the gama color
	 * @date 20 août 2023
	 */

	public static GamaColor get(final int rgb) {
		// rgba value expected
		GamaColor result = int_colors.get(rgb);
		if (result == null) {
			result = new GamaColor(rgb);
			int_colors.put(rgb, result);
		}
		return result;
	}

	/**
	 * Gets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param rgb
	 *            the rgb
	 * @param alpha
	 *            the alpha
	 * @return the gama color
	 * @date 20 août 2023
	 */
	public static GamaColor create(final int rgb, final int alpha) {
		GamaColor c = get(rgb);
		return get(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}

	/**
	 * Gets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param r
	 *            the r
	 * @param g
	 *            the g
	 * @param b
	 *            the b
	 * @return the gama color
	 * @date 20 août 2023
	 */
	public static GamaColor get(final int r, final int g, final int b) {
		return get(r, g, b, 255);
	}

	/**
	 * Gets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param r
	 *            the r
	 * @param g
	 *            the g
	 * @param b
	 *            the b
	 * @param a
	 *            the a
	 * @return the gama color
	 * @date 20 août 2023
	 */
	public static GamaColor get(final int r, final int g, final int b, final int a) {
		// rgb in 3 components + alpha
		return get((normalize(a) & 0xFF) << 24 | (normalize(r) & 0xFF) << 16 | (normalize(g) & 0xFF) << 8
				| (normalize(b) & 0xFF) << 0);

	}

	/**
	 * Gets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param r
	 *            the r
	 * @param g
	 *            the g
	 * @param b
	 *            the b
	 * @param t
	 *            the t
	 * @return the gama color
	 * @date 20 août 2023
	 */
	public static GamaColor getWithDoubleAlpha(final int r, final int g, final int b, final double t) {
		return get(r, g, b, normalize(t));
	}

	/**
	 * Gets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param r
	 *            the r
	 * @param g
	 *            the g
	 * @param b
	 *            the b
	 * @param t
	 *            the t
	 * @return the gama color
	 * @date 20 août 2023
	 */
	public static GamaColor getWithDoubles(final double r, final double g, final double b, final double t) {
		return get(normalize(r), normalize(g), normalize(b), normalize(t));
	}

	/**
	 * Gets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param c
	 *            the c
	 * @param t
	 *            the t
	 * @return the gama color
	 * @date 20 août 2023
	 */
	public static GamaColor get(final Color c, final double t) {
		return create(c, normalize(t));
	}

	/**
	 * Gets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param c
	 *            the c
	 * @param t
	 *            the t
	 * @return the gama color
	 * @date 20 août 2023
	 */
	public static GamaColor create(final Color c, final int t) {
		return get(c.getRed(), c.getGreen(), c.getBlue(), t);
	}

	/**
	 * Gets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param c
	 *            the c
	 * @date 20 août 2023
	 */
	public static GamaColor get(final Color c) {
		return get(c.getRGB());
	}

	/**
	 * Gets the named.
	 *
	 * @param rgb
	 *            the rgb
	 * @return the named
	 */
	public static GamaColor get(final String rgb) {
		return colors.get(rgb);
	}

	/**
	 * Gets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param name
	 *            the rgb
	 * @return the gama color
	 * @date 20 août 2023
	 */
	public static GamaColor get(final String name, final int... t) {
		GamaColor c = colors.get(name);
		if (c == null) {
			colors.put(name,
					new NamedGamaColor(name, normalize(t[0]), normalize(t[1]), normalize(t[2]), normalize(t[3])));
		}
		return colors.get(name);
	}

	/**
	 * Normalize.
	 *
	 * @param number
	 *            the transp
	 * @return the int
	 */
	// returns a value between 0 and 255 from a double between 0 and 1
	private static int normalize(final double number) {
		return (int) (number < 0 ? 0 : number > 1 ? 255 : 255 * number);
	}

	/**
	 * Normalize.
	 *
	 * @param number
	 *            the rgb comp
	 * @return the int
	 */
	private static int normalize(final int number) {
		return number < 0 ? 0 : number > 255 ? 255 : number;
	}

}
