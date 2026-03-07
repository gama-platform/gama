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
	 * Returns the red component of this color.
	 *
	 * @return the red component value in the range 0-255
	 */
	@getter (IKeyword.COLOR_RED)
	Integer red();

	/**
	 * Returns the blue component of this color.
	 *
	 * @return the blue component value in the range 0-255
	 */
	@getter (IKeyword.COLOR_BLUE)
	Integer blue();

	/**
	 * Returns the green component of this color.
	 *
	 * @return the green component value in the range 0-255
	 */
	@getter (IKeyword.COLOR_GREEN)
	Integer green();

	/**
	 * Returns the alpha (transparency) component of this color.
	 *
	 * @return the alpha component value in the range 0-255, where 0 is fully transparent and 255 is fully opaque
	 */
	@getter (IKeyword.ALPHA)
	Integer alpha();

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
	IColor brighter();

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
	IColor darker();

	/**
	 * Compares this color to another based on their RGB integer values.
	 * <p>
	 * This comparison treats the entire RGBA value as a single 32-bit integer and compares them numerically. This is
	 * the fastest comparison method but may not correspond to visual perception.
	 * </p>
	 *
	 * @param c2
	 *            the color to compare with
	 * @return a negative integer, zero, or a positive integer if this color is less than, equal to, or greater than
	 *         the specified color
	 */
	int compareRgbTo(IColor c2);

	/**
	 * Compares this color to another based on luminescence (perceived brightness).
	 * <p>
	 * Uses the NTSC formula: 0.299*R + 0.587*G + 0.114*B, which approximates how the human eye perceives brightness.
	 * Green contributes most to perceived brightness, followed by red, then blue.
	 * </p>
	 *
	 * @param c2
	 *            the color to compare with
	 * @return a negative integer, zero, or a positive integer if this color is less luminescent than, equal to, or
	 *         more luminescent than the specified color
	 */
	int compareLuminescenceTo(IColor c2);

	/**
	 * Compares this color to another based on HSB brightness.
	 * <p>
	 * Converts both colors to HSB (Hue, Saturation, Brightness) color space and compares the brightness component.
	 * This represents the maximum of the RGB values normalized to 0-1 range.
	 * </p>
	 *
	 * @param c2
	 *            the color to compare with
	 * @return a negative integer, zero, or a positive integer if this color is less bright than, equal to, or brighter
	 *         than the specified color
	 */
	int compareBrightnessTo(IColor c2);

	/**
	 * Compares this color to another based on luma.
	 * <p>
	 * Uses the formula: 0.21*R + 0.72*G + 0.07*B, which is another perceptual brightness measure commonly used in
	 * video and image processing.
	 * </p>
	 *
	 * @param c2
	 *            the color to compare with
	 * @return a negative integer, zero, or a positive integer if this color has less luma than, equal to, or more luma
	 *         than the specified color
	 */
	int compareLumaTo(IColor c2);

	/**
	 * Creates a new color with the same RGB components but with a different alpha (transparency) value.
	 *
	 * @param d
	 *            the new alpha value as a double in the range 0.0 (fully transparent) to 1.0 (fully opaque)
	 * @return a new IColor instance with the modified alpha channel
	 */
	IColor withAlpha(double d);

	/**
	 * Checks if this color is pure black (all RGB components are zero).
	 * <p>
	 * Note: This method ignores the alpha channel. A transparent black will still return true.
	 * </p>
	 *
	 * @return true if red, green, and blue are all zero; false otherwise
	 */
	boolean isZero();

	/**
	 * Returns the RGBA color value as a single integer.
	 * <p>
	 * The integer is packed as: (alpha << 24) | (red << 16) | (green << 8) | blue. This is compatible with AWT's
	 * Color.getRGB() format.
	 * </p>
	 *
	 * @return the RGBA color encoded as a 32-bit integer
	 */
	int getRGB();

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