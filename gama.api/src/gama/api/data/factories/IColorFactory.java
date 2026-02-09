/*******************************************************************************************************
 *
 * IColorFactory.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.data.factories;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import gama.api.data.objects.IColor;

/**
 *
 */
public interface IColorFactory extends IFactory<IColor> {

	/**
	 * A map storing named colors accessible by their lowercase names.
	 */
	Map<String, IColor> NAME_REGISTRY = new HashMap<>();

	/**
	 * A thread-safe map storing colors indexed by their integer RGB value.
	 */
	Map<Integer, IColor> INT_REGISTRY = Collections.synchronizedMap(new HashMap<>());

	/**
	 * Returns an IColor corresponding to the given integer RGB value.
	 *
	 * @param rgb
	 *            the integer representation of the color (e.g. -16777216 for black)
	 * @return the IColor corresponding to the value
	 */
	IColor get(final int rgb);

	/**
	 * Creates a color from a packed RGB integer and an explicit alpha value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param rgb
	 *            the integer representation of the color
	 * @param alpha
	 *            the alpha (transparency) component (0-255)
	 * @return the created IColor
	 * @date 20 août 2023
	 */
	IColor create(int rgb, int alpha);

	/**
	 * Returns an IColor constructed from red, green, and blue components. Alpha is assumed to be 255 (opaque).
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param r
	 *            the red component (0-255)
	 * @param g
	 *            the green component (0-255)
	 * @param b
	 *            the blue component (0-255)
	 * @return the corresponding IColor
	 * @date 20 août 2023
	 */
	IColor get(int r, int g, int b);

	/**
	 * Returns an IColor constructed from red, green, blue, and alpha components.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param r
	 *            the red component (0-255)
	 * @param g
	 *            the green component (0-255)
	 * @param b
	 *            the blue component (0-255)
	 * @param a
	 *            the alpha component (0-255)
	 * @return the corresponding IColor
	 * @date 20 août 2023
	 */
	IColor create(int r, int g, int b, int a);

	/**
	 * Returns an IColor from integer RGB values and a double alpha value.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param r
	 *            the red component (0-255)
	 * @param g
	 *            the green component (0-255)
	 * @param b
	 *            the blue component (0-255)
	 * @param t
	 *            the alpha/transparency (0.0 for transparent, 1.0 for opaque)
	 * @return the corresponding IColor
	 * @date 20 août 2023
	 */
	IColor getWithDoubleAlpha(int r, int g, int b, double t);

	/**
	 * Returns an IColor from double values for all components.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param r
	 *            the red component
	 * @param g
	 *            the green component
	 * @param b
	 *            the blue component
	 * @param t
	 *            the alpha/transparency component
	 * @return the corresponding IColor
	 * @date 20 août 2023
	 */
	IColor getWithDoubles(double r, double g, double b, double t);

	/**
	 * Creates a new IColor based on a java.awt.Color and a new alpha value (double).
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param c
	 *            the source AWT color
	 * @param t
	 *            the new alpha value (0.0 - 1.0)
	 * @return the new IColor
	 * @date 20 août 2023
	 */
	IColor createWithAlpha(Color c, double t);

	/**
	 * Creates a new IColor based on a java.awt.Color and a new alpha value (int).
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param c
	 *            the source AWT color
	 * @param t
	 *            the new alpha value (0-255)
	 * @return the new IColor
	 * @date 20 août 2023
	 */
	IColor createWithAlpha(Color c, int t);

	/**
	 * Creates a new IColor based on an existing IColor and a new alpha value (int).
	 *
	 * @param c
	 *            the source IColor
	 * @param t
	 *            the new alpha value (0-255)
	 * @return the new IColor
	 */
	IColor createWithAlpha(IColor c, int t);

	/**
	 * Creates a new IColor based on an existing IColor and a new alpha value (double).
	 *
	 * @param c
	 *            the source IColor
	 * @param t
	 *            the new alpha value (0.0 - 1.0)
	 * @return the new IColor
	 */
	IColor createWithAlpha(IColor c, double t);

	/**
	 * Returns an IColor wrapping or corresponding to a java.awt.Color.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param c
	 *            the source AWT color
	 * @date 20 août 2023
	 */
	IColor get(Color c);

	/**
	 * Returns an IColor from a name and optional transparency.
	 *
	 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
	 * @param name
	 *            the color name or definition
	 * @param t
	 *            optional alpha values (0-255). If multiple are provided, only the first is usually used.
	 * @return the corresponding IColor
	 * @date 20 août 2023
	 */
	IColor createWithNameAndRGBA(String name, int r, int g, int b, int a);

	/**
	 * Creates a new IColor object by merging two existing IColor objects.
	 *
	 * @param c1
	 *            the first color
	 * @param c2
	 *            the second color
	 * @return the merged IColor
	 */
	IColor createByMerging(IColor c1, IColor c2);

	/**
	 * Creates a new IColor based on a CSS color name and a specific alpha (transparency). If the named color does not
	 * exist, returns black with the specified alpha.
	 *
	 * @param c
	 *            the CSS color name.
	 * @param alpha
	 *            the alpha component (0-255).
	 * @return the new {@link IColor} instance.
	 */
	IColor createWithAlpha(String c, int alpha);

}