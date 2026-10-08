/*******************************************************************************************************
 *
 * PreferenceType5.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.operators.multi_criteria;

/**
 * PROMETHEE <b>Type V</b> (linear with indifference zone) preference function. This is one of
 * the six standard preference functions defined in the PROMETHEE methodology. It introduces both
 * an <em>indifference threshold</em> {@code q} and a <em>preference threshold</em> {@code p},
 * with a linear interpolation zone between them:
 *
 * <pre>
 *   P(diff) = 0                       if diff &le; q
 *   P(diff) = (diff - q) / (p - q)   if q &lt; diff &lt; p
 *   P(diff) = 1                       if diff &ge; p
 * </pre>
 *
 * <p>The shape of the function is a ramp that rises linearly from {@code 0} at {@code q} to
 * {@code 1} at {@code p}. A difference at or below {@code q} is treated as indifference, while a
 * difference at or above {@code p} expresses strict preference.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Indifference below 0.1, strict preference above 0.8, linear in between
 * FonctionPreference f = new PreferenceType5(0.1, 0.8);
 * double pref = f.valeur(0.5); // returns (0.5 - 0.1) / (0.8 - 0.1) ≈ 0.571
 * }</pre>
 *
 * @see FonctionPreference
 * @see Promethee
 */
public class PreferenceType5 implements FonctionPreference {

	/**
	 * The indifference threshold. When the performance difference between two candidates is at
	 * most {@code q}, the preference degree is {@code 0} (the two alternatives are treated as
	 * indifferent on this criterion). Must satisfy {@code q < p}.
	 */
	private double q;

	/**
	 * The strict preference threshold. When the performance difference between two candidates is
	 * at least {@code p}, the preference degree reaches its maximum of {@code 1}. Must satisfy
	 * {@code p > q}.
	 */
	private double p;

	/**
	 * Evaluates the Type V preference function for the given performance difference. The result
	 * is:
	 * <ul>
	 *   <li>{@code 0} when {@code diff ≤ q} (indifference zone)</li>
	 *   <li>{@code (diff - q) / (p - q)} when {@code q < diff < p} (linear interpolation)</li>
	 *   <li>{@code 1} when {@code diff ≥ p} (strict preference)</li>
	 * </ul>
	 *
	 * @param diff
	 *            the signed performance difference {@code val_a - val_b}; a positive value
	 *            indicates that candidate {@code a} performs better than {@code b} on this
	 *            criterion
	 * @return the preference degree in {@code [0, 1]}
	 */
	@Override
	public double valeur(double diff) {
		if (diff <= q)
			return 0;
		if (diff <= p)
			return (diff - q) / (p - q);
		return 1;
	}

	/**
	 * Constructs a Type V preference function with the specified thresholds.
	 *
	 * @param q
	 *            the indifference threshold; differences at or below this value yield a preference
	 *            degree of {@code 0}; must be non-negative and less than {@code p}
	 * @param p
	 *            the strict preference threshold; differences at or above this value yield a
	 *            preference degree of {@code 1}; must be greater than {@code q}
	 */
	public PreferenceType5(double q, double p) {
		super();
		this.q = q;
		this.p = p;
	}

	/**
	 * Returns a deep copy of this preference function with identical parameter values {@code q}
	 * and {@code p}.
	 *
	 * @return a new {@code PreferenceType5} instance with the same {@code q} and {@code p}
	 */
	@Override
	public FonctionPreference copie() {
		return new PreferenceType5(q, p);
	}

	/**
	 * Computes a hash code based on both threshold parameters {@code p} and {@code q}.
	 *
	 * @return the hash code for this preference function
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(p);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(q);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * Checks equality by comparing both threshold parameters {@code p} and {@code q} using exact
	 * double bit comparison.
	 *
	 * @param obj
	 *            the object to compare with this preference function
	 * @return {@code true} if {@code obj} is a {@code PreferenceType5} with the same {@code p}
	 *         and {@code q} values; {@code false} otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PreferenceType5 other = (PreferenceType5) obj;
		if (Double.doubleToLongBits(p) != Double.doubleToLongBits(other.p))
			return false;
		if (Double.doubleToLongBits(q) != Double.doubleToLongBits(other.q))
			return false;
		return true;
	}

}