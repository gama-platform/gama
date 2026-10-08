/*******************************************************************************************************
 *
 * FonctionPreference.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.operators.multi_criteria;

/**
 * Contract for a PROMETHEE preference function. In the PROMETHEE (Preference Ranking Organisation
 * Method for Enrichment of Evaluations) family of multi-criteria decision making methods, each
 * criterion is associated with one of six standard preference function types that map the
 * performance difference between two candidates on that criterion to a preference degree in
 * {@code [0, 1]}.
 *
 * <p>The preference degree {@code P(a, b)} for a pair of candidates {@code a} and {@code b} on a
 * criterion is computed as {@code P = f(val_a - val_b)}, where {@code f} is the preference
 * function. A value of {@code 0} means indifference; a value of {@code 1} means strict preference
 * for {@code a} over {@code b}.
 *
 * <p>Concrete implementations available in this package:
 * <ul>
 *   <li>{@link PreferenceType5}: Type V — linear function with both an indifference zone and a
 *       strict preference zone.</li>
 *   <li>{@link PreferenceType6}: Type VI — Gaussian function.</li>
 * </ul>
 *
 * @see Promethee
 * @see PreferenceType5
 * @see PreferenceType6
 */
public interface FonctionPreference {

	/**
	 * Evaluates the preference degree for a performance difference of {@code diff} between two
	 * candidates on the criterion associated with this preference function. The result is always
	 * in {@code [0, 1]}:
	 * <ul>
	 *   <li>{@code 0} — the performance difference is within the indifference zone (no preference
	 *       in favour of the first candidate).</li>
	 *   <li>{@code 1} — the performance difference is large enough to express a strict preference
	 *       for the first candidate.</li>
	 *   <li>Values in {@code (0, 1)} — partial preference, interpolated according to the specific
	 *       function shape.</li>
	 * </ul>
	 *
	 * @param diff
	 *            the signed performance difference {@code val_a - val_b} between the first and
	 *            the second candidate on this criterion; a positive value favours the first
	 *            candidate
	 * @return the preference degree in {@code [0, 1]}
	 */
	public double valeur(double diff);

	/**
	 * Creates and returns a deep copy of this preference function, preserving all parameter
	 * values. Used by the {@link Promethee} copy constructor to avoid shared mutable state.
	 *
	 * @return a new {@code FonctionPreference} instance with the same type and parameters as this
	 *         one
	 */
	public FonctionPreference copie();
}
