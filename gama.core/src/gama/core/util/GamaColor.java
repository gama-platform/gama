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

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.interfaces.IKeyword;
import gama.core.runtime.IScope;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.gaml.interfaces.IValue;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class GamaColor.
 *
 * @author Alexis Drogoul (alexis.drogoul@ird.fr)
 * @date 20 août 2023
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
public class GamaColor extends Color implements IValue, Comparable<Color> {

	/**
	 * Instantiates a new gama color.
	 *
	 * @param awtRGB
	 *            the awt RGB
	 */
	GamaColor(final int awtRGB) {
		super(awtRGB, true);
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
		super(r, g, b, t);
	}

	@Override
	public String toString() {
		return serializeToGaml(true);
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return "rgb (" + red() + ", " + green() + ", " + blue() + ", " + getAlpha() + ")";
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
	@getter (IKeyword.COLOR_RED)
	public Integer red() {
		return super.getRed();
	}

	/**
	 * Blue.
	 *
	 * @return the integer
	 */
	@getter (IKeyword.COLOR_BLUE)
	public Integer blue() {
		return super.getBlue();
	}

	/**
	 * Green.
	 *
	 * @return the integer
	 */
	@getter (IKeyword.COLOR_GREEN)
	public Integer green() {
		return super.getGreen();
	}

	/**
	 * Alpha.
	 *
	 * @return the integer
	 */
	@getter (IKeyword.ALPHA)
	public Integer alpha() {
		return super.getAlpha();
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
	public GamaColor brighter() {
		int r = getRed();
		int g = getGreen();
		int b = getBlue();
		int alpha = getAlpha();

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
	public GamaColor darker() {
		return GamaColorFactory.get(Math.max((int) (getRed() * BRIGHTNESS_FACTOR), 0),
				Math.max((int) (getGreen() * BRIGHTNESS_FACTOR), 0), Math.max((int) (getBlue() * BRIGHTNESS_FACTOR), 0),
				getAlpha());
	}

	@Override
	public GamaColor copy(final IScope scope) {
		return GamaColorFactory.get(this);
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
	public static GamaColor merge(final GamaColor c1, final GamaColor c2) {
		return GamaColorFactory.get(c1.getRed() + c2.getRed(), c1.getGreen() + c2.getGreen(),
				c1.getBlue() + c2.getBlue(), c1.getAlpha() + c2.getAlpha());
	}

	/**
	 * Compare rgb to.
	 *
	 * @param c2
	 *            the c 2
	 * @return the int
	 */
	public int compareRgbTo(final Color c2) {
		return Integer.signum(getRGB() - c2.getRGB());
	}

	/**
	 * Compare luminescence to.
	 *
	 * @param c2
	 *            the c 2
	 * @return the int
	 */
	public int compareLuminescenceTo(final Color c2) {
		return Double.compare(this.getRed() * 0.299d + this.getGreen() * 0.587d + this.getBlue() * 0.114d,
				c2.getRed() * 0.299d + c2.getGreen() * 0.587d + c2.getBlue() * 0.114d);
	}

	/**
	 * Compare brightness to.
	 *
	 * @param c2
	 *            the c 2
	 * @return the int
	 */
	public int compareBrightnessTo(final Color c2) {
		final float[] hsb = RGBtoHSB(getRed(), getGreen(), getBlue(), null);
		final float[] hsb2 = RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getBlue(), null);
		return Float.compare(hsb[2], hsb2[2]);
	}

	/**
	 * Compare luma to.
	 *
	 * @param c2
	 *            the c 2
	 * @return the int
	 */
	public int compareLumaTo(final Color c2) {
		return Double.compare(this.getRed() * 0.21d + this.getGreen() * 0.72d + this.getBlue() * 0.07d,
				c2.getRed() * 0.21d + c2.getGreen() * 0.72d + c2.getBlue() * 0.07d);
	}

	/**
	 * Compare to.
	 *
	 * @param c2
	 *            the c 2
	 * @return the int
	 */
	@Override
	public int compareTo(final Color c2) {
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
	public GamaColor withAlpha(final double d) {
		return GamaColorFactory.getWithDoubleAlpha(getRed(), getGreen(), getBlue(), d);
	}

	/**
	 * Checks if is zero.
	 *
	 * @return true, if is zero
	 */
	public boolean isZero() { return getRed() == 0 && getGreen() == 0 && getBlue() == 0; }

	@Override
	public int intValue(final IScope scope) {
		return super.getRGB();
	}

	@Override
	public JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(), "red", getRed(), "green", getGreen(), "blue", getBlue(), "alpha",
				getAlpha());

	}

}
