/*******************************************************************************************************
 *
 * InternalGamaColorFactory.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform
 * (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util.color;

import java.awt.Color;

import gama.api.data.factories.GamaColorFactory;
import gama.api.data.factories.IColorFactory;
import gama.api.data.objects.IColor;
import gama.api.runtime.scope.IScope;

/**
 *
 */
public class InternalGamaColorFactory implements IColorFactory {

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
	 * Gets the color specified by its RGBA value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param rgba
	 *            the rgba
	 * @return the gama color
	 * @date 20 août 2023
	 */

	@Override
	public IColor get(final int rgba) {
		IColor result = INT_REGISTRY.get(rgba);
		if (result == null) {
			result = new GamaColor(rgba);
			INT_REGISTRY.put(rgba, result);
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
	@Override
	public IColor create(final int rgb, final int alpha) {
		IColor c = get(rgb);
		return create(c.red(), c.green(), c.blue(), alpha);
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
	@Override
	public IColor get(final int r, final int g, final int b) {
		return create(r, g, b, 255);
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
	@Override
	public IColor create(final int r, final int g, final int b, final int a) {
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
	@Override
	public IColor getWithDoubleAlpha(final int r, final int g, final int b, final double t) {
		return create(r, g, b, normalize(t));
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
	@Override
	public IColor getWithDoubles(final double r, final double g, final double b, final double t) {
		return create(normalize(r), normalize(g), normalize(b), normalize(t));
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
	@Override
	public IColor createWithAlpha(final Color c, final double t) {
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
	@Override
	public IColor createWithAlpha(final Color c, final int t) {
		return create(c.getRed(), c.getGreen(), c.getBlue(), t);
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
	@Override
	public IColor createWithAlpha(final IColor c, final int t) {
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
	@Override
	public IColor createWithAlpha(final IColor c, final double t) {
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
	@Override
	public IColor get(final Color c) {
		return get(c.getRGB());
	}

	/**
	 * Creates a new GamaColor object, with the specified name and RGBA values.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param name
	 *            the name of the color
	 * @param r
	 *            the red component (0-255)
	 * @param g
	 *            the green component (0-255)
	 * @param b
	 *            the blue component (0-255)
	 * @param a
	 *            the alpha component (0-255)
	 * @return the gama color
	 * @date 20 août 2023
	 */
	@Override
	public IColor createWithNameAndRGBA(final String name, final int r, final int g, final int b, final int a) {
		IColor c = NAME_REGISTRY.get(name);
		if (c == null) {
			c = new NamedGamaColor(name, normalize(r), normalize(g), normalize(b), normalize(a));
			NAME_REGISTRY.put(name, c);
			INT_REGISTRY.put(c.getRGB(), c);
		}
		return c;
	}

	/**
	 * Normalize.
	 *
	 * @param number
	 *            the transp
	 * @return the int
	 */
	// returns a value between 0 and 255 from a double between 0 and 1
	private int normalize(final double number) {
		return (int) (number < 0 ? 0 : number > 1 ? 255 : 255 * number);
	}

	/**
	 * Normalize.
	 *
	 * @param number
	 *            the rgb comp
	 * @return the int
	 */
	private int normalize(final int number) {
		return number < 0 ? 0 : number > 255 ? 255 : number;
	}

	@Override
	public IColor createByMerging(final IColor c1, final IColor c2) {
		int r = (c1.red() + c2.red()) / 2;
		int g = (c1.green() + c2.green()) / 2;
		int b = (c1.blue() + c2.blue()) / 2;
		int a = (c1.alpha() + c2.alpha()) / 2;
		return create(r, g, b, a);
	}

	@Override
	public IColor createWithAlpha(final String c, final int alpha) {
		IColor color = GamaColorFactory.get(c);
		if (color == null) { color = GamaColorFactory.BLACK; }
		return color.withAlpha(alpha / 255d);
	}

}
