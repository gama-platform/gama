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

	/** The Constant array. */
	public final static Object[] array = ColorCSS.array;

	/** The Constant colors. */
	public final static Map<String, IColor> COLORS = GamaMapFactory.createUnordered();

	/** The Constant int_colors. */
	public final static Map<Integer, IColor> INT_COLORS = Collections.synchronizedMap(new HashMap<>());

	static {
		for (int i = 0; i < array.length; i += 2) {
			final IColor color = get((String) array[i], (int[]) array[i + 1]);
			COLORS.put((String) array[i], color);
			INT_COLORS.put(color.getRGB(), color);
		}
		// A.G add the GAMA Color corresponding to the GAMA 1.9 Logo
		final IColor orange = get("gamaorange", 244, 165, 40, 255);
		COLORS.put("gamaorange", orange);
		INT_COLORS.put(orange.getRGB(), orange);

		final IColor red = get("gamared", 217, 72, 33, 255);
		COLORS.put("gamared", red);
		INT_COLORS.put(red.getRGB(), red);

		final IColor blue = get("gamablue", 22, 94, 147, 255);
		COLORS.put("gamablue", blue);
		INT_COLORS.put(blue.getRGB(), blue);

		final IColor green = get("gamagreen", 81, 135, 56, 255);
		COLORS.put("gamagreen", green);
		INT_COLORS.put(green.getRGB(), green);
	}

	/** The Constant TRANSPARENT. */
	public static final IColor TRANSPARENT = get("transparent", 0, 0, 0, 0);

	/** The Constant BLACK. */
	public static final IColor BLACK = get("black", 0, 0, 0);

	/** The Constant WHITE. */
	public static final IColor WHITE = get("white", 255, 255, 255);

	/** The Constant RED. */
	public static final IColor RED = get("red", 255, 0, 0);

	/** The Constant GREEN. */
	public static final IColor GREEN = get("green", 0, 255, 0);

	/** The Constant BLUE. */
	public static final IColor BLUE = get("blue", 0, 0, 255);

	/** The Constant YELLOW. */
	public static final IColor YELLOW = get("yellow", 255, 255, 0);

	/** The Constant LIGHT_GRAY. */
	public static final IColor LIGHT_GRAY = get("lightgray", 211, 211, 211);

	/** The Constant GRAY. */
	public static final IColor GRAY = get("gray", 128, 128, 128);
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

	/**
	 * Gets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param rgb
	 *            the rgb
	 * @return the gama color
	 * @date 20 août 2023
	 */

	public static IColor get(final int rgb) {
		// rgba value expected
		IColor result = INT_COLORS.get(rgb);
		if (result == null) {
			result = new GamaColor(rgb);
			INT_COLORS.put(rgb, result);
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
	public static IColor create(final int rgb, final int alpha) {
		IColor c = get(rgb);
		return get(c.red(), c.green(), c.blue(), alpha);
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
	public static IColor get(final int r, final int g, final int b) {
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
	public static IColor get(final int r, final int g, final int b, final int a) {
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
	public static IColor getWithDoubleAlpha(final int r, final int g, final int b, final double t) {
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
	public static IColor getWithDoubles(final double r, final double g, final double b, final double t) {
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
	public static IColor createWithAlpha(final Color c, final double t) {
		return createWithAlpha(c, normalize(t));
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
	public static IColor createWithAlpha(final Color c, final int t) {
		return get(c.getRed(), c.getGreen(), c.getBlue(), t);
	}

	/**
	 * Creates a new GamaColor object.
	 *
	 * @param c
	 *            the c
	 * @param t
	 *            the t
	 * @return the gama color
	 */
	public static IColor createWithAlpha(final IColor c, final int t) {
		return c.withAlpha(t / 255d);
	}

	/**
	 * Creates a new GamaColor object.
	 *
	 * @param c
	 *            the c
	 * @param t
	 *            the t
	 * @return the i color
	 */
	public static IColor createWithAlpha(final IColor c, final double t) {
		return c.withAlpha(t);
	}

	/**
	 * Gets the.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param c
	 *            the c
	 * @date 20 août 2023
	 */
	public static IColor get(final Color c) {
		return get(c.getRGB());
	}

	/**
	 * Gets the named.
	 *
	 * @param rgb
	 *            the rgb
	 * @return the named
	 */
	public static IColor get(final String rgb) {
		return COLORS.get(rgb);
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
	public static IColor get(final String name, final int... t) {
		IColor c = COLORS.get(name);
		if (c == null) {
			COLORS.put(name,
					new NamedGamaColor(name, normalize(t[0]), normalize(t[1]), normalize(t[2]), normalize(t[3])));
		}
		return COLORS.get(name);
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
