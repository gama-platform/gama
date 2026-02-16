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
import gama.api.constants.IKeyword;
import gama.api.gaml.types.IType;
import gama.api.gaml.types.Types;
import gama.api.types.misc.IValue;

/**
 *
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
	 * To AWT color.
	 *
	 * @param c
	 *            the c
	 * @return the java.awt. color
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
	Integer red();

	/**
	 * Blue.
	 *
	 * @return the integer
	 */
	@getter (IKeyword.COLOR_BLUE)
	Integer blue();

	/**
	 * Green.
	 *
	 * @return the integer
	 */
	@getter (IKeyword.COLOR_GREEN)
	Integer green();

	/**
	 * Alpha.
	 *
	 * @return the integer
	 */
	@getter (IKeyword.ALPHA)
	Integer alpha();

	/**
	 * Gets the brighter.
	 *
	 * @return the brighter
	 */
	@getter (IKeyword.BRIGHTER)
	IColor brighter();

	/**
	 * Gets the darker.
	 *
	 * @return the darker
	 */
	@getter (IKeyword.DARKER)
	IColor darker();

	/**
	 * Compare rgb to.
	 *
	 * @param c2
	 *            the c 2
	 * @return the int
	 */
	int compareRgbTo(IColor c2);

	/**
	 * Compare luminescence to.
	 *
	 * @param c2
	 *            the c 2
	 * @return the int
	 */
	int compareLuminescenceTo(IColor c2);

	/**
	 * Compare brightness to.
	 *
	 * @param c2
	 *            the c 2
	 * @return the int
	 */
	int compareBrightnessTo(IColor c2);

	/**
	 * Compare luma to.
	 *
	 * @param c2
	 *            the c 2
	 * @return the int
	 */
	int compareLumaTo(IColor c2);

	/**
	 * With alpha.
	 *
	 * @param d
	 *            the d
	 * @return the gama color
	 */
	IColor withAlpha(double d);

	/**
	 * Checks if is zero.
	 *
	 * @return true, if is zero
	 */
	boolean isZero();

	/**
	 * @return
	 */
	int getRGB();

	/**
	 * Gets the AWT color.
	 *
	 * @return the AWT color
	 */
	Color getAWTColor();

	/**
	 * Gets the gaml type.
	 *
	 * @return the gaml type
	 */
	@Override
	default IType<IColor> getGamlType() { return Types.COLOR; }

}