/*******************************************************************************************************
 *
 * Promethee.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.operators.multi_criteria;

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

import gama.api.GAMA;

/**
 * Implementation of the <b>PROMETHEE II</b> (Preference Ranking Organisation Method for
 * Enrichment of Evaluations) multi-criteria decision making algorithm. PROMETHEE II ranks a set
 * of {@link Candidate} objects by computing their <em>net outranking flow</em> (also called the
 * net preference score) and selects the candidate with the highest net flow as the best
 * alternative.
 *
 * <p>For every ordered pair of candidates {@code (A, B)} and for each criterion, the preference
 * degree {@code P(A, B)} is evaluated by the criterion's {@link FonctionPreference} using the
 * performance difference {@code val_A - val_B}. The weighted aggregate preference index is then:
 * <pre>
 *   Π(A, B) = Σ_j  w_j * P_j(A, B)
 * </pre>
 * where {@code w_j} is the weight of criterion {@code j}. The <em>positive outranking flow</em>
 * (how much A dominates others) and the <em>negative outranking flow</em> (how much A is
 * dominated) are averaged over all opponents, and the <em>net flow</em> is their difference. The
 * candidate with the greatest net flow is returned.
 *
 * <p>Three constructors are available for different initialisation scenarios:
 * <ul>
 *   <li>{@link #Promethee(Map, Map)}: explicit weight and preference-function maps.</li>
 *   <li>{@link #Promethee(Collection)}: random weights (from the current GAMA random generator)
 *       and default {@link PreferenceType5}(0.1, 0.8) functions for every criterion.</li>
 *   <li>{@link #Promethee(Promethee)}: deep copy constructor.</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * Map<String, Double> weights = Map.of("cost", 3.0, "quality", 5.0);
 * Map<String, FonctionPreference> prefs = Map.of(
 *     "cost",    new PreferenceType5(0.05, 0.4),
 *     "quality", new PreferenceType6(0.3)
 * );
 * Promethee p = new Promethee(weights, prefs);
 * Candidate best = p.decision(candidates);
 * }</pre>
 *
 * @see Candidate
 * @see FonctionPreference
 * @see PreferenceType5
 * @see PreferenceType6
 */
public class Promethee {

	/**
	 * A map from criterion name to its weight used in the weighted aggregate preference index
	 * computation. Higher weights give more influence to a criterion in the final ranking. Keys
	 * must match those in each {@link Candidate}'s criterion-value map and those in
	 * {@link #fctPrefCrit}.
	 */
	private Map<String, Double> poidsCrit;

	/**
	 * A map from criterion name to its {@link FonctionPreference}. For each criterion the
	 * preference function translates the raw performance difference between two candidates into a
	 * preference degree in {@code [0, 1]}. Keys must match those in each {@link Candidate}'s
	 * criterion-value map and those in {@link #poidsCrit}.
	 */
	private Map<String, FonctionPreference> fctPrefCrit;

	/**
	 * Constructs a {@code Promethee} instance with fully specified weight and preference-function
	 * maps.
	 *
	 * @param poidsCrit
	 *            a map from criterion name to its weight; must not be {@code null} and keys must
	 *            match those in each {@link Candidate}'s value map
	 * @param fctPrefCrit
	 *            a map from criterion name to its {@link FonctionPreference}; must not be
	 *            {@code null} and must share the same keys as {@code poidsCrit}
	 */
	public Promethee(final Map<String, Double> poidsCrit, final Map<String, FonctionPreference> fctPrefCrit) {
		super();
		this.poidsCrit = poidsCrit;
		this.fctPrefCrit = fctPrefCrit;
	}

	/**
	 * Constructs a {@code Promethee} instance with randomly assigned weights and default
	 * {@link PreferenceType5}(0.1, 0.8) preference functions for each supplied criterion name.
	 * Weights are drawn from a uniform integer distribution in {@code [0, 9]} using the current
	 * GAMA random generator.
	 *
	 * @param criteres
	 *            the collection of criterion names for which the instance is initialised; must not
	 *            be {@code null}; each name must later appear as a key in every {@link Candidate}'s
	 *            criterion-value map
	 */
	public Promethee(final Collection<String> criteres) {
		poidsCrit = new Hashtable<>();
		fctPrefCrit = new Hashtable<>();
		for (final String crit : criteres) {
			poidsCrit.put(crit, Double.valueOf(GAMA.getCurrentRandom().between(0, 9)));
			fctPrefCrit.put(crit, new PreferenceType5(0.1, 0.8));
		}
	}

	/**
	 * Deep-copy constructor. Creates a new {@code Promethee} instance that is a fully independent
	 * copy of {@code promethee}, duplicating both the weight map and the preference-function
	 * objects (via {@link FonctionPreference#copie()}).
	 *
	 * @param promethee
	 *            the source {@code Promethee} instance to copy; must not be {@code null}
	 */
	public Promethee(final Promethee promethee) {
		poidsCrit = new Hashtable<>();
		for (final String crit : promethee.poidsCrit.keySet()) {
			poidsCrit.put(crit, promethee.poidsCrit.get(crit));
		}
		fctPrefCrit = new Hashtable<>();
		for (final String crit : promethee.poidsCrit.keySet()) {
			fctPrefCrit.put(crit, promethee.fctPrefCrit.get(crit).copie());
		}
	}

	/**
	 * Selects the best {@link Candidate} from the given list using the PROMETHEE II net outranking
	 * flow procedure. For each candidate {@code A} the method computes:
	 * <ol>
	 *   <li>The <em>positive flow</em> {@code Φ⁺(A)}: the average weighted aggregate preference
	 *       of {@code A} over all other candidates.</li>
	 *   <li>The <em>negative flow</em> {@code Φ⁻(A)}: the average weighted aggregate preference
	 *       of all other candidates over {@code A}.</li>
	 *   <li>The <em>net flow</em> {@code Φ(A) = Φ⁺(A) - Φ⁻(A)}.</li>
	 * </ol>
	 * The candidate with the maximum net flow is returned as the best alternative.
	 *
	 * @param locations
	 *            the list of candidates to evaluate; must not be {@code null}; must contain at
	 *            least two candidates for a meaningful result
	 * @return the candidate with the highest net outranking flow, or {@code null} if
	 *         {@code locations} is empty
	 */
	public Candidate decision(final LinkedList<Candidate> locations) {
		Candidate meilleureLoc = null;
		double outRankingMax = -Double.MAX_VALUE;
		for (final Candidate loc1 : locations) {
			double outRankingPlus = 0;
			double outRankingMoins = 0;
			for (final Candidate loc2 : locations) {
				if (loc1 == loc2) {
					continue;
				}
				double PiXA = 0;
				double PiAX = 0;
				for (final String crit : fctPrefCrit.keySet()) {
					final double poids = poidsCrit.get(crit).doubleValue();
					final FonctionPreference fctPref = fctPrefCrit.get(crit);
					final double valLoc1 = loc1.getValCriteria().get(crit).doubleValue();
					final double valLoc2 = loc2.getValCriteria().get(crit).doubleValue();
					PiXA += poids * fctPref.valeur(valLoc1 - valLoc2);
					PiAX += poids * fctPref.valeur(valLoc2 - valLoc1);
				}
				outRankingPlus += PiXA;
				outRankingMoins += PiAX;
			}
			outRankingPlus /= locations.size() - 1;
			outRankingMoins /= locations.size() - 1;
			final double outRanking = outRankingPlus - outRankingMoins;
			if (outRanking > outRankingMax) {
				outRankingMax = outRanking;
				meilleureLoc = loc1;
			}
		}
		return meilleureLoc;
	}

	/**
	 * Returns a string representation showing the current per-criterion weight map.
	 *
	 * @return a string of the form {@code "Poids : {criterion=weight, ...}"}
	 */
	@Override
	public String toString() {
		return "Poids : " + poidsCrit;
	}

	/**
	 * Returns the per-criterion preference-function map. The returned map is mutable and changes
	 * will affect subsequent {@link #decision} calls.
	 *
	 * @return a mutable map from criterion name to its {@link FonctionPreference}; never
	 *         {@code null}
	 */
	public Map<String, FonctionPreference> getFctPrefCrit() {
		return fctPrefCrit;
	}

	/**
	 * Replaces the per-criterion preference-function map.
	 *
	 * @param fctPrefCrit
	 *            the new map from criterion name to its {@link FonctionPreference}; must not be
	 *            {@code null} and must share the same keys as {@link #poidsCrit}
	 */
	public void setFctPrefCrit(final Map<String, FonctionPreference> fctPrefCrit) {
		this.fctPrefCrit = fctPrefCrit;
	}

	/**
	 * Returns the per-criterion weight map. The returned map is mutable and changes will affect
	 * subsequent {@link #decision} calls.
	 *
	 * @return a mutable map from criterion name to its weight; never {@code null}
	 */
	public Map<String, Double> getPoidsCrit() {
		return poidsCrit;
	}

	/**
	 * Replaces the per-criterion weight map.
	 *
	 * @param poidsCrit
	 *            the new map from criterion name to weight; must not be {@code null} and must
	 *            share the same keys as {@link #fctPrefCrit}
	 */
	public void setPoidsCrit(final Map<String, Double> poidsCrit) {
		this.poidsCrit = poidsCrit;
	}

	/**
	 * Computes a hash code based on both the preference-function map and the weight map.
	 *
	 * @return the hash code for this {@code Promethee} instance
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (fctPrefCrit == null ? 0 : fctPrefCrit.hashCode());
		result = prime * result + (poidsCrit == null ? 0 : poidsCrit.hashCode());
		return result;
	}

	/**
	 * Checks equality by comparing both the preference-function map and the weight map.
	 *
	 * @param obj
	 *            the object to compare with this instance
	 * @return {@code true} if {@code obj} is a {@code Promethee} with equal {@code fctPrefCrit}
	 *         and {@code poidsCrit} maps; {@code false} otherwise
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		final Promethee other = (Promethee) obj;
		if (fctPrefCrit == null) {
			if (other.fctPrefCrit != null) { return false; }
		} else if (!fctPrefCrit.equals(other.fctPrefCrit)) { return false; }
		if (poidsCrit == null) {
			if (other.poidsCrit != null) { return false; }
		} else if (!poidsCrit.equals(other.poidsCrit)) { return false; }
		return true;
	}

}
