/*******************************************************************************************************
 *
 * PreferenceType6.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.operators.multi_criteria;

/**
 * PROMETHEE <b>Type VI</b> (Gaussian) preference function. This is one of the six standard
 * preference functions defined in the PROMETHEE methodology. The preference degree rises
 * smoothly from {@code 0} for negative or zero differences and approaches {@code 1} asymptotically
 * for large positive differences, following a Gaussian shape parameterised by the inflection
 * parameter {@code s}:
 *
 * <pre>
 *   P(diff) = 0                              if diff &le; 0
 *   P(diff) = 1 - exp(diff&sup2; / (-2 * s&sup2;))   if diff &gt; 0
 * </pre>
 *
 * <p>The parameter {@code s} controls the steepness of the curve: smaller values of {@code s}
 * produce a sharper rise (stronger discrimination), while larger values produce a more gradual
 * increase (softer preference expression). The value {@code -2s²} is pre-computed as
 * {@link #valSquare} to avoid repeated multiplication at evaluation time.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Gaussian preference with inflection at s = 0.5
 * FonctionPreference f = new PreferenceType6(0.5);
 * double pref = f.valeur(0.5); // returns 1 - exp(0.25 / -0.5) ≈ 0.394
 * }</pre>
 *
 * @see FonctionPreference
 * @see Promethee
 */
public class PreferenceType6 implements FonctionPreference {

	/**
	 * The Gaussian shape parameter (analogous to a standard deviation). It controls the
	 * steepness of the preference curve: a smaller {@code s} produces a sharper rise while a
	 * larger {@code s} gives a smoother, more gradual function. Must be strictly positive.
	 */
	private final double s;

	/**
	 * Pre-computed value of {@code -2 * s²}, used as the denominator of the Gaussian exponent
	 * {@code diff² / (-2s²)}. Stored to avoid redundant arithmetic at each evaluation.
	 */
	private final double valSquare;

	/**
	 * Evaluates the Type VI (Gaussian) preference function for the given performance difference.
	 * Returns {@code 0} for non-positive differences (no preference when the first candidate does
	 * not outperform the second). For positive differences, returns
	 * {@code 1 - exp(diff² / (-2s²))}.
	 *
	 * @param diff
	 *            the signed performance difference {@code val_a - val_b}; a positive value
	 *            indicates that candidate {@code a} performs better than {@code b} on this
	 *            criterion
	 * @return the preference degree in {@code [0, 1)}; approaches {@code 1} as {@code diff → ∞}
	 */
	@Override
	public double valeur(final double diff) {
		if (diff <= 0) { return 0; }

		return 1 - Math.exp(diff * diff / valSquare);
	}

	/**
	 * Constructs a Type VI (Gaussian) preference function with the given shape parameter. The
	 * pre-computed value {@link #valSquare} is initialised to {@code -2 * s²}.
	 *
	 * @param s
	 *            the Gaussian shape parameter; must be strictly positive — larger values produce
	 *            a slower-rising (softer) preference function
	 */
	public PreferenceType6(final double s) {
		super();
		this.s = s;
		this.valSquare = -1 * (2 * s * s);
	}

	/**
	 * Returns a deep copy of this preference function with the same shape parameter {@code s}.
	 *
	 * @return a new {@code PreferenceType6} instance with the same {@code s}
	 */
	@Override
	public FonctionPreference copie() {
		return new PreferenceType6(s);
	}

	/**
	 * Computes a hash code based on the shape parameter {@code s} using exact double bit
	 * representation.
	 *
	 * @return the hash code for this preference function
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(s);
		result = prime * result + (int) (temp ^ temp >>> 32);
		return result;
	}

	/**
	 * Checks equality by comparing the shape parameter {@code s} using exact double bit
	 * comparison.
	 *
	 * @param obj
	 *            the object to compare with this preference function
	 * @return {@code true} if {@code obj} is a {@code PreferenceType6} with the same {@code s}
	 *         value; {@code false} otherwise
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		final PreferenceType6 other = (PreferenceType6) obj;
		return Double.doubleToLongBits(s) == Double.doubleToLongBits(other.s);
	}

}