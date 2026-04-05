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

}
