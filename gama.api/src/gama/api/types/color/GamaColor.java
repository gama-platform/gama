/*******************************************************************************************************
 *
 * GamaColor.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.color;

import java.awt.Color;

import gama.api.utils.json.IJson;
import gama.api.utils.json.IJsonValue;

/**
 * The default implementation of {@link IColor} for the GAMA platform.
 * <p>
 * This class wraps a Java AWT {@link Color} object and provides all the functionality defined by the {@link IColor}
 * interface. It is the primary concrete color type used throughout GAMA.
 * </p>
 *
 * <h2>Implementation Details</h2>
 * <ul>
 * <li><strong>Internal Storage:</strong> Colors are stored as AWT Color objects which use RGBA format with 8 bits per
 * component (0-255 range)</li>
 * <li><strong>Immutability:</strong> While the internal color reference is not final, color objects should be treated
 * as immutable. Transformation methods (brighter, darker, withAlpha) return new instances</li>
 * <li><strong>Brightness Operations:</strong> Uses a brightness factor of 0.7 for darkening/brightening operations,
 * consistent with AWT's approach</li>
 * <li><strong>Comparison:</strong> The default {@link #compareTo(IColor)} uses RGB value comparison</li>
 * </ul>
 *
 * <h2>GAML Serialization</h2>
 * <p>
 * Colors serialize to GAML syntax as: {@code rgb(r, g, b, a)}. Named colors (created through {@link NamedGamaColor})
 * use their name instead.
 * </p>
 *
 * <h2>JSON Serialization</h2>
 * <p>
 * Colors serialize to JSON as typed objects containing "red", "green", "blue", and "alpha" properties.
 * </p>
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @since 1.0
 * @see IColor
 * @see GamaColorFactory
 */
record GamaColor(Color internalColor) implements IColor {

	/**
	 * Creates a new color from a packed RGBA integer value.
	 * <p>
	 * The integer should be in AWT format: (alpha << 24) | (red << 16) | (green << 8) | blue.
	 * </p>
	 *
	 * @param awtRGB
	 *            the packed RGBA value including alpha channel
	 */
	GamaColor(final int awtRGB) {
		this(new Color(awtRGB, true));
	}

	/**
	 * Creates a new color from individual RGB and alpha components.
	 *
	 * @param r
	 *            the red component (0-255)
	 * @param g
	 *            the green component (0-255)
	 * @param b
	 *            the blue component (0-255)
	 * @param t
	 *            the alpha/transparency component (0-255, where 0 is fully transparent and 255 is fully opaque)
	 */
	GamaColor(final int r, final int g, final int b, final int t) {
		// t between 0 and 255
		this(new Color(r, g, b, t));
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return serializeToGaml(true);
	}

	/**
	 * Serializes this color to JSON format.
	 * <p>
	 * Creates a typed JSON object with properties: "red", "green", "blue", "alpha".
	 * </p>
	 *
	 * @param json
	 *            the JSON builder
	 * @return the JSON representation of this color
	 */
	@Override
	public IJsonValue serializeToJson(final IJson json) {
		return json.typedObject(getGamlType(), "red", red(), "green", green(), "blue", blue(), "alpha", alpha());
	}

	/**
	 * Returns the underlying AWT Color object.
	 * <p>
	 * Provides direct access to the internal AWT representation.
	 * </p>
	 *
	 * @return the java.awt.Color instance
	 */
	@Override
	public Color getAWTColor() { return internalColor; }

}
