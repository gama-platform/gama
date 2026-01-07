/*******************************************************************************************************
 *
 * GamaColor.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util;

import java.awt.Color;

import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.util.file.json.IJSon;
import gama.core.util.file.json.IJsonValue;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class GamaColor.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 20 août 2023
 */

public class GamaColor implements IColor {

	/** The internal color. */
	Color internalColor;

	/**
	 * Instantiates a new gama color.
	 *
	 * @param awtRGB
	 *            the awt RGB
	 */
	GamaColor(final int awtRGB) {
		internalColor = new Color(awtRGB, true);
	}

	/**
	 * Instantiates a new gama color.
	 *
	 * @param r
	 *            the r
	 * @param g
	 *            the g
	 * @param b
	 *            the b
	 * @param t
	 *            the t
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

	/** The brightness factor. */
	static float BRIGHTNESS_FACTOR = 0.7f;

	/**
	 * Gets the brighter.
	 *
	 * @return the brighter
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
		if (r == 0 && g == 0 && b == 0) return GamaColorFactory.get(i, i, i, alpha);
		if (r > 0 && r < i) { r = i; }
		if (g > 0 && g < i) { g = i; }
		if (b > 0 && b < i) { b = i; }

		return GamaColorFactory.get(Math.min((int) (r / BRIGHTNESS_FACTOR), 255),
				Math.min((int) (g / BRIGHTNESS_FACTOR), 255), Math.min((int) (b / BRIGHTNESS_FACTOR), 255), alpha);
	}

	/**
	 * Gets the darker.
	 *
	 * @return the darker
	 */
	@Override
	@getter (IKeyword.DARKER)
	public IColor darker() {
		return GamaColorFactory.get(Math.max((int) (red() * BRIGHTNESS_FACTOR), 0),
				Math.max((int) (green() * BRIGHTNESS_FACTOR), 0), Math.max((int) (blue() * BRIGHTNESS_FACTOR), 0),
				alpha());
	}

	@Override
	public IColor copy(final IScope scope) {
		return GamaColorFactory.get(getRGB());
	}

	/**
	 * Merge.
	 *
	 * @param c1
	 *            the c 1
	 * @param c2
	 *            the c 2
	 * @return the gama color
	 */
	public static IColor merge(final IColor c1, final IColor c2) {
		return GamaColorFactory.get(c1.red() + c2.red(), c1.green() + c2.green(), c1.blue() + c2.blue(),
				c1.alpha() + c2.alpha());
	}

	/**
	 * Compare rgb to.
	 *
	 * @param c2
	 *            the c 2
	 * @return the int
	 */
	@Override
	public int compareRgbTo(final IColor c2) {
		return Integer.signum(getRGB() - c2.getRGB());
	}

	/**
	 * Compare luminescence to.
	 *
	 * @param c2
	 *            the c 2
	 * @return the int
	 */
	@Override
	public int compareLuminescenceTo(final IColor c2) {
		return Double.compare(this.red() * 0.299d + this.green() * 0.587d + this.blue() * 0.114d,
				c2.red() * 0.299d + c2.green() * 0.587d + c2.blue() * 0.114d);
	}

	/**
	 * Compare brightness to.
	 *
	 * @param c2
	 *            the c 2
	 * @return the int
	 */
	@Override
	public int compareBrightnessTo(final IColor c2) {
		final float[] hsb = Color.RGBtoHSB(red(), green(), blue(), null);
		final float[] hsb2 = Color.RGBtoHSB(c2.red(), c2.green(), c2.blue(), null);
		return Float.compare(hsb[2], hsb2[2]);
	}

	/**
	 * Compare luma to.
	 *
	 * @param c2
	 *            the c 2
	 * @return the int
	 */
	@Override
	public int compareLumaTo(final IColor c2) {
		return Double.compare(this.red() * 0.21d + this.green() * 0.72d + this.blue() * 0.07d,
				c2.red() * 0.21d + c2.green() * 0.72d + c2.blue() * 0.07d);
	}

	/**
	 * Compare to.
	 *
	 * @param c2
	 *            the c 2
	 * @return the int
	 */
	@Override
	public int compareTo(final IColor c2) {
		return compareRgbTo(c2);
	}

	/**
	 * Method getType()
	 *
	 * @see gama.gaml.interfaces.ITyped#getGamlType()
	 */
	@Override
	public IType<?> getGamlType() { return Types.COLOR; }

	/**
	 * With alpha.
	 *
	 * @param d
	 *            the d
	 * @return the gama color
	 */
	@Override
	public IColor withAlpha(final double d) {
		return GamaColorFactory.getWithDoubleAlpha(red(), green(), blue(), d);
	}

	/**
	 * Checks if is zero.
	 *
	 * @return true, if is zero
	 */
	@Override
	public boolean isZero() { return red() == 0 && green() == 0 && blue() == 0; }

	@Override
	public int intValue(final IScope scope) {
		return internalColor.getRGB();
	}

	@Override
	public IJsonValue serializeToJson(final IJSon json) {
		return json.typedObject(getGamlType(), "red", red(), "green", green(), "blue", blue(), "alpha", alpha());

	}

	@Override
	public int getRGB() { return internalColor.getRGB(); }

	@Override
	public Color getAWTColor() { return internalColor; }

}
