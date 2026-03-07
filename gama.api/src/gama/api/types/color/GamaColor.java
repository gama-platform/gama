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

import gama.annotations.getter;
import gama.api.constants.IKeyword;
import gama.api.runtime.scope.IScope;
import gama.api.types.color.GamaColorFactory.NamedGamaColor;
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
class GamaColor implements IColor {

	/** The internal AWT color representation. */
	Color internalColor;

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
		internalColor = new Color(awtRGB, true);
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
		internalColor = new Color(r, g, b, t);
	}

	@Override
	public String toString() {
		return serializeToGaml(true);
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return "rgb (" + red() + ", " + green() + ", " + blue() + ", " + alpha() + ")";
	}

	@Override
	public String stringValue(final IScope scope) {
		return toString();
	}

	/**
	 * Red.
	 *
	 * @return the integer
	 */
	@Override
	@getter (IKeyword.COLOR_RED)
	public Integer red() {
		return internalColor.getRed();
	}

	/**
	 * Blue.
	 *
	 * @return the integer
	 */
	@Override
	@getter (IKeyword.COLOR_BLUE)
	public Integer blue() {
		return internalColor.getBlue();
	}

	/**
	 * Green.
	 *
	 * @return the integer
	 */
	@Override
	@getter (IKeyword.COLOR_GREEN)
	public Integer green() {
		return internalColor.getGreen();
	}

	/**
	 * Alpha.
	 *
	 * @return the integer
	 */
	@Override
	@getter (IKeyword.ALPHA)
	public Integer alpha() {
		return internalColor.getAlpha();
	}

	/**
	 * The brightness factor used for darkening and brightening operations.
	 * <p>
	 * A value of 0.7 means that darkening reduces each component to 70% of its original value, while brightening
	 * divides by 0.7 (multiplies by ~1.43).
	 * </p>
	 */
	static float BRIGHTNESS_FACTOR = 0.7f;

	/**
	 * Creates a brighter version of this color.
	 * <p>
	 * This implementation follows specific rules to ensure predictable and visually pleasing results:
	 * </p>
	 * <ol>
	 * <li><strong>Pure black:</strong> Returns a dark grey instead of staying black</li>
	 * <li><strong>Pure colors:</strong> Colors like pure blue (0,0,255) will brighten to lighter blue, not white</li>
	 * <li><strong>Very dark colors:</strong> Components with very low non-zero values are boosted to ensure a visible
	 * change</li>
	 * <li><strong>Alpha preservation:</strong> The alpha channel remains unchanged</li>
	 * </ol>
	 * <p>
	 * The algorithm divides each non-zero RGB component by {@link #BRIGHTNESS_FACTOR} (0.7), effectively multiplying by
	 * ~1.43, with a ceiling of 255.
	 * </p>
	 *
	 * @return a new IColor instance that is brighter than this color
	 */
	@Override
	@getter (IKeyword.BRIGHTER)
	public IColor brighter() {
		int r = red();
		int g = green();
		int b = blue();
		int alpha = alpha();

		/*
		 * From 2D group: 1. black.brighter() should return grey 2. applying brighter to blue will always return blue,
		 * brighter 3. non pure color (non zero rgb) will eventually return white
		 */
		int i = (int) (1.0 / (1.0 - BRIGHTNESS_FACTOR));
		if (r == 0 && g == 0 && b == 0) return GamaColorFactory.createWithRGBA(i, i, i, alpha);
		if (r > 0 && r < i) { r = i; }
		if (g > 0 && g < i) { g = i; }
		if (b > 0 && b < i) { b = i; }

		return GamaColorFactory.createWithRGBA(Math.min((int) (r / BRIGHTNESS_FACTOR), 255),
				Math.min((int) (g / BRIGHTNESS_FACTOR), 255), Math.min((int) (b / BRIGHTNESS_FACTOR), 255), alpha);
	}

	/**
	 * Creates a darker version of this color.
	 * <p>
	 * Multiplies each RGB component by {@link #BRIGHTNESS_FACTOR} (0.7), reducing brightness while maintaining hue. The
	 * alpha channel is preserved. Components are floored at 0.
	 * </p>
	 *
	 * @return a new IColor instance that is darker than this color
	 */
	@Override
	@getter (IKeyword.DARKER)
	public IColor darker() {
		return GamaColorFactory.createWithRGBA(Math.max((int) (red() * BRIGHTNESS_FACTOR), 0),
				Math.max((int) (green() * BRIGHTNESS_FACTOR), 0), Math.max((int) (blue() * BRIGHTNESS_FACTOR), 0),
				alpha());
	}

	@Override
	public IColor copy(final IScope scope) {
		return GamaColorFactory.get(getRGB());
	}

	/**
	 * Compares this color to another based on RGB integer values.
	 * <p>
	 * Returns -1, 0, or 1 based on the comparison of the packed RGBA integers.
	 * </p>
	 *
	 * @param c2
	 *            the color to compare with
	 * @return the sign of the difference between the RGB values
	 */
	@Override
	public int compareRgbTo(final IColor c2) {
		return Integer.signum(getRGB() - c2.getRGB());
	}

	/**
	 * Compares colors based on luminescence using the NTSC formula.
	 * <p>
	 * Formula: 0.299*R + 0.587*G + 0.114*B
	 * </p>
	 * <p>
	 * This formula weights colors according to human eye sensitivity, where green contributes most to perceived
	 * brightness.
	 * </p>
	 *
	 * @param c2
	 *            the color to compare with
	 * @return a negative, zero, or positive value based on relative luminescence
	 */
	@Override
	public int compareLuminescenceTo(final IColor c2) {
		return Double.compare(this.red() * 0.299d + this.green() * 0.587d + this.blue() * 0.114d,
				c2.red() * 0.299d + c2.green() * 0.587d + c2.blue() * 0.114d);
	}

	/**
	 * Compares colors based on HSB brightness component.
	 * <p>
	 * Converts both colors to HSB (Hue, Saturation, Brightness) and compares the brightness values.
	 * </p>
	 *
	 * @param c2
	 *            the color to compare with
	 * @return a negative, zero, or positive value based on relative brightness
	 */
	@Override
	public int compareBrightnessTo(final IColor c2) {
		final float[] hsb = Color.RGBtoHSB(red(), green(), blue(), null);
		final float[] hsb2 = Color.RGBtoHSB(c2.red(), c2.green(), c2.blue(), null);
		return Float.compare(hsb[2], hsb2[2]);
	}

	/**
	 * Compares colors based on luma calculation.
	 * <p>
	 * Formula: 0.21*R + 0.72*G + 0.07*B
	 * </p>
	 * <p>
	 * This is an alternative perceptual brightness measure commonly used in video processing.
	 * </p>
	 *
	 * @param c2
	 *            the color to compare with
	 * @return a negative, zero, or positive value based on relative luma
	 */
	@Override
	public int compareLumaTo(final IColor c2) {
		return Double.compare(this.red() * 0.21d + this.green() * 0.72d + this.blue() * 0.07d,
				c2.red() * 0.21d + c2.green() * 0.72d + c2.blue() * 0.07d);
	}

	/**
	 * Default comparison method using RGB integer values.
	 * <p>
	 * Delegates to {@link #compareRgbTo(IColor)}.
	 * </p>
	 *
	 * @param c2
	 *            the color to compare with
	 * @return the comparison result
	 */
	@Override
	public int compareTo(final IColor c2) {
		return compareRgbTo(c2);
	}

	/**
	 * Creates a new color with the same RGB components but different alpha.
	 *
	 * @param d
	 *            the new alpha value as a double (0.0 = transparent, 1.0 = opaque)
	 * @return a new IColor with modified transparency
	 */
	@Override
	public IColor withAlpha(final double d) {
		return GamaColorFactory.createWithDoubleAlpha(red(), green(), blue(), d);
	}

	/**
	 * Checks if this is a pure black color (all RGB components are zero).
	 * <p>
	 * Note: Alpha is ignored. A fully transparent black still returns true.
	 * </p>
	 *
	 * @return true if RGB = (0,0,0), false otherwise
	 */
	@Override
	public boolean isZero() { return red() == 0 && green() == 0 && blue() == 0; }

	/**
	 * Returns the color as a packed integer value in RGBA format.
	 * <p>
	 * This is equivalent to {@link #getRGB()} and returns the AWT-compatible 32-bit representation.
	 * </p>
	 *
	 * @param scope
	 *            the execution scope (unused)
	 * @return the packed RGBA integer value
	 */
	@Override
	public int intValue(final IScope scope) {
		return internalColor.getRGB();
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
	 * Returns the packed RGBA integer value for this color.
	 * <p>
	 * Format: (alpha << 24) | (red << 16) | (green << 8) | blue
	 * </p>
	 *
	 * @return the 32-bit RGBA value
	 */
	@Override
	public int getRGB() { return internalColor.getRGB(); }

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
