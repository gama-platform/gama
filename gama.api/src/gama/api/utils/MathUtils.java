/*******************************************************************************************************
 *
 * MathUtils.java, in gama.core, is part of the source code of the GAMA modeling and simulation platform (v.2025-03).
 *
 * (c) 2007-2026 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, ESPACE-DEV, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.api.utils;

import org.locationtech.jts.index.quadtree.DoubleBits;
import org.locationtech.jts.index.quadtree.IntervalSize;

import gama.api.gaml.constants.GamlCoreConstants;

/**
 *
 */
public class MathUtils {

	/** The Constant TENS. */
	public static final long[] TENS = new long[100];

	static {
		for (int i = 0; i < TENS.length; i++) { TENS[i] = (long) Math.pow(10, i); }
	}

	/**
	 * @param v
	 * @param precision
	 * @return
	 */
	public static double round(final Double v, final Integer precision) {
		final long t = TENS[precision]; // contains powers of ten.
		return (double) (long) (v > 0 ? v * t + 0.5 : v * t - 0.5) / t;
	}
	
	
	/**
	  * Computes whether the interval [min, max] is effectively zero width.
	  * I.e. the width of the interval is so much less than the
	  * location of the interval that the midpoint of the interval cannot be
	  * represented precisely.
	  */
	 public static boolean isZeroWidth(double min, double max){
	   double width = max - min;
	   if (Math.abs(width) <= GamlCoreConstants.min_float) return true;
	
	   double maxAbs = Math.max(Math.abs(min), Math.abs(max));
	   double scaledInterval = width / maxAbs;
	   int level = DoubleBits.exponent(scaledInterval);
	   return level <= IntervalSize.MIN_BINARY_EXPONENT;
	 }

}
