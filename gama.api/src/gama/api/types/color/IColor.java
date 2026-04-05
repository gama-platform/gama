/*******************************************************************************************************
 *
 * IColor.java, in gama.api, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.types.color;

import java.awt.Color;

import gama.annotations.doc;
import gama.annotations.getter;
import gama.annotations.variable;
import gama.annotations.vars;
import gama.annotations.constants.IKeyword;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.runtime.scope.IScope;
import gama.api.types.misc.IValue;

/**
 * The core interface for color representation in the GAMA platform.
 * <p>
 * This interface defines the contract for color objects used throughout GAMA models and simulations. It extends
 * {@link IValue} to integrate with the GAML type system and implements {@link Comparable} to allow color ordering.
 * </p>
 *
 * <h2>Color Components</h2>
 * <p>
 * Colors are represented using the RGBA color model:
 * </p>
 * <ul>
 * <li><strong>Red:</strong> Red component (0-255)</li>
 * <li><strong>Green:</strong> Green component (0-255)</li>
 * <li><strong>Blue:</strong> Blue component (0-255)</li>
 * <li><strong>Alpha:</strong> Transparency/opacity (0 = fully transparent, 255 = fully opaque)</li>
 * </ul>
 *
 * <h2>Color Transformations</h2>
 * <p>
 * The interface provides methods to create color variants:
 * </p>
 * <ul>
 * <li>{@link #brighter()} - Creates a lighter version by increasing luminance</li>
 * <li>{@link #darker()} - Creates a darker version by decreasing luminance</li>
 * <li>{@link #withAlpha(double)} - Creates a variant with modified transparency</li>
 * </ul>
 *
 * <h2>Color Comparison</h2>
 * <p>
 * Multiple comparison strategies are available for ordering colors:
 * </p>
 * <ul>
 * <li>{@link #compareRgbTo(IColor)} - Direct RGB integer value comparison</li>
 * <li>{@link #compareLuminescenceTo(IColor)} - Based on perceived brightness (NTSC formula: 0.299R + 0.587G +
 * 0.114B)</li>
 * <li>{@link #compareBrightnessTo(IColor)} - Based on HSB color model brightness component</li>
 * <li>{@link #compareLumaTo(IColor)} - Based on luma calculation (0.21R + 0.72G + 0.07B)</li>
 * </ul>
 *
 * <h2>GAML Variable Access</h2>
 * <p>
 * When used in GAML models, color objects expose the following attributes through the {@code @vars} annotation:
 * </p>
 * <ul>
 * <li>{@code red} - The red component (int)</li>
 * <li>{@code green} - The green component (int)</li>
 * <li>{@code blue} - The blue component (int)</li>
 * <li>{@code alpha} - The alpha/transparency component (int)</li>
 * <li>{@code brighter} - A brighter color variant (color)</li>
 * <li>{@code darker} - A darker color variant (color)</li>
 * </ul>
 *
 * @author GAMA Development Team
 * @since 1.0
 * @see GamaColorFactory
 * @see GamaColor
 */
@vars ({ @variable (
		name = IKeyword.COLOR_RED,
		type = IType.INT,
		doc = { @doc ("Returns the red component of the color (between 0 and 255)") }),
		@variable (
				name = IKeyword.COLOR_GREEN,
				type = IType.INT,
				doc = { @doc ("Returns the green component of the color (between 0 and 255)") }),
		@variable (
				name = IKeyword.COLOR_BLUE,
				type = IType.INT,
				doc = { @doc ("Returns the blue component of the color (between 0 and 255)") }),
		@variable (
				name = IKeyword.ALPHA,
				type = IType.INT,
				doc = { @doc ("Returns the alpha component (transparency) of the color (between 0 for transparent and 255 for opaque)") }),
		@variable (
				name = IKeyword.BRIGHTER,
				type = IType.COLOR,
				doc = { @doc ("Returns a lighter color (with increased luminance)") }),
		@variable (
				name = IKeyword.DARKER,
				type = IType.COLOR,
				doc = { @doc ("Returns a darker color (with decreased luminance)") }) })
public interface IColor extends IValue, Comparable<IColor> {

	/**
	 * The brightness factor used for darkening and brightening operations.
	 * <p>
	 * A value of 0.7 means that darkening reduces each component to 70% of its original value, while brightening
	 * divides by 0.7 (multiplies by ~1.43).
	 * </p>
	 */
	float BRIGHTNESS_FACTOR = 0.7f;

	/**
	 * Serialize to gaml.
	 *
	 * @param includingBuiltIn
	 *            the including built in
	 * @return the string
	 */
	@Override
	default String serializeToGaml(final boolean includingBuiltIn) {
		return "rgb (" + red() + ", " + green() + ", " + blue() + ", " + alpha() + ")";
	}

	/**
	 * String value.
	 *
	 * @param scope
	 *            the scope
	 * @return the string
	 */
	@Override
	default String stringValue(final IScope scope) {
		return toString();
	}

	/**
	 * Converts an {@link IColor} instance to a Java AWT {@link Color}.
	 * <p>
	 * This static utility method provides a null-safe conversion from GAMA's color representation to Java's standard
	 * AWT color object. It is useful when interfacing with Java graphics APIs or legacy code that expects AWT colors.
	 * </p>
	 *
	 * @param c
	 *            the IColor to convert, may be null
	 * @return the corresponding java.awt.Color object, or null if the input is null
	 */
	static java.awt.Color toAWTColor(final IColor c) {
		return c == null ? null : c.getAWTColor();
	}

	/**
	 * Red.
	 *
	 * @return the integer
	 */
	@getter (IKeyword.COLOR_RED)
	default Integer red() {
		return getAWTColor().getRed();
	}

	/**
	 * Blue.
	 *
	 * @return the integer
	 */
	@getter (IKeyword.COLOR_BLUE)
	default Integer blue() {
		return getAWTColor().getBlue();
	}

	/**
	 * Green.
	 *
	 * @return the integer
	 */
	@getter (IKeyword.COLOR_GREEN)
	default Integer green() {
		return getAWTColor().getGreen();
	}

	/**
	 * Alpha.
	 *
	 * @return the integer
	 */
	@getter (IKeyword.ALPHA)
	default Integer alpha() {
		return getAWTColor().getAlpha();
	}

	/**
	 * Creates and returns a brighter version of this color.
	 * <p>
	 * The brightening algorithm increases the luminance while preserving the hue. Special cases:
	 * </p>
	 * <ul>
	 * <li>Pure black (0,0,0) returns a dark gray</li>
	 * <li>Very dark colors are boosted to ensure visible change</li>
	 * <li>The alpha component is preserved</li>
	 * </ul>
	 *
	 * @return a new IColor instance that is brighter than this color
	 */
	@getter (IKeyword.BRIGHTER)
	default IColor brighter() {
		int r = red();
		int g = green();
		int b = blue();
		int alpha = alpha();

		int i = (int) (1.0 / (1.0 - BRIGHTNESS_FACTOR));
		if (r == 0 && g == 0 && b == 0) return GamaColorFactory.createWithRGBA(i, i, i, alpha);
		if (r > 0 && r < i) { r = i; }
		if (g > 0 && g < i) { g = i; }
		if (b > 0 && b < i) { b = i; }

		return GamaColorFactory.createWithRGBA(Math.min((int) (r / BRIGHTNESS_FACTOR), 255),
				Math.min((int) (g / BRIGHTNESS_FACTOR), 255), Math.min((int) (b / BRIGHTNESS_FACTOR), 255), alpha);
	}

	/**
	 * Creates and returns a darker version of this color.
	 * <p>
	 * The darkening algorithm decreases the luminance by applying a factor to each RGB component while preserving the
	 * hue. The alpha component is preserved.
	 * </p>
	 *
	 * @return a new IColor instance that is darker than this color
	 */
	@getter (IKeyword.DARKER)
	default IColor darker() {
		return GamaColorFactory.createWithRGBA(Math.max((int) (red() * BRIGHTNESS_FACTOR), 0),
				Math.max((int) (green() * BRIGHTNESS_FACTOR), 0), Math.max((int) (blue() * BRIGHTNESS_FACTOR), 0),
				alpha());
	}

	/**
	 * Copy.
	 *
	 * @param scope
	 *            the scope
	 * @return the i color
	 */
	@Override
	default IColor copy(final IScope scope) {
		return GamaColorFactory.get(getRGB());
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
	default int compareTo(final IColor c2) {
		return compareRgbTo(c2);
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
	default int compareRgbTo(final IColor c2) {
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
	default int compareLuminescenceTo(final IColor c2) {
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
	default int compareBrightnessTo(final IColor c2) {
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
	default int compareLumaTo(final IColor c2) {
		return Double.compare(this.red() * 0.21d + this.green() * 0.72d + this.blue() * 0.07d,
				c2.red() * 0.21d + c2.green() * 0.72d + c2.blue() * 0.07d);
	}

	/**
	 * Creates a new color with the same RGB components but different alpha.
	 *
	 * @param d
	 *            the new alpha value as a double (0.0 = transparent, 1.0 = opaque)
	 * @return a new IColor with modified transparency
	 */
	default IColor withAlpha(final double d) {
		return GamaColorFactory.createWithDoubleAlpha(red(), green(), blue(), d);
	}

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
	default int intValue(final IScope scope) {
		return getRGB();
	}

	/**
	 * Checks if this is a pure black color (all RGB components are zero).
	 * <p>
	 * Note: Alpha is ignored. A fully transparent black still returns true.
	 * </p>
	 *
	 * @return true if RGB = (0,0,0), false otherwise
	 */
	default boolean isZero() { return red() == 0 && green() == 0 && blue() == 0; }

	/**
	 * Returns the RGBA color value as a single integer.
	 * <p>
	 * The integer is packed as: (alpha << 24) | (red << 16) | (green << 8) | blue. This is compatible with AWT's
	 * Color.getRGB() format.
	 * </p>
	 *
	 * @return the RGBA color encoded as a 32-bit integer
	 */

	default int getRGB() { return getAWTColor().getRGB(); }

	/**
	 * Returns this color as a Java AWT {@link Color} object.
	 * <p>
	 * This method provides access to the underlying AWT color representation for use with Java graphics APIs.
	 * </p>
	 *
	 * @return the java.awt.Color representation of this color
	 */
	Color getAWTColor();

	/**
	 * Returns the GAML type for colors.
	 *
	 * @return the IType representing the color type in GAML's type system
	 */
	@Override
	default IType<IColor> getGamlType() { return Types.COLOR; }

}