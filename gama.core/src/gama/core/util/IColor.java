/*******************************************************************************************************
 *
 * IColor.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.core.util;

import java.awt.Color;

import gama.annotations.precompiler.OkForAPI;
import gama.gaml.interfaces.IValue;

/**
 *
 */
@OkForAPI (OkForAPI.Location.INTERFACES)
public interface IColor extends IValue, Comparable<IColor> {

	/**
	 * Red.
	 *
	 * @return the integer
	 */
	Integer red();

	/**
	 * Blue.
	 *
	 * @return the integer
	 */
	Integer blue();

	/**
	 * Green.
	 *
	 * @return the integer
	 */
	Integer green();

	/**
	 * Alpha.
	 *
	 * @return the integer
	 */
	Integer alpha();

	/**
	 * Gets the brighter.
	 *
	 * @return the brighter
	 */
	IColor brighter();

	/**
	 * Gets the darker.
	 *
	 * @return the darker
	 */
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

}