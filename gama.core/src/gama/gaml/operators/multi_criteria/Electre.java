/*******************************************************************************************************
 *
 * Electre.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.operators.multi_criteria;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the <b>ELECTRE III</b> (ELimination Et Choix Traduisant la REalité) outranking
 * multi-criteria decision making algorithm. ELECTRE III selects the best {@link Candidate} from a
 * set of alternatives by computing pairwise <em>credibility indices</em> (also called outranking
 * degrees) and declaring candidate A to <em>outrank</em> candidate B when A's global credibility
 * index against B exceeds a user-defined cutting threshold.
 *
 * <p>The algorithm proceeds in three steps for every ordered pair {@code (A1, A2)}:
 * <ol>
 *   <li><b>Concordance</b>: for each criterion, a concordance index in {@code [0, 1]} measures
 *       how much the evidence supports "A1 is at least as good as A2" on that criterion.</li>
 *   <li><b>Discordance</b>: for each criterion, a discordance index in {@code [0, 1]} measures
 *       how strongly one criterion vetoes the hypothesis "A1 outranks A2".</li>
 *   <li><b>Credibility</b>: the global credibility index of "A1 outranks A2" combines the
 *       weighted average concordance with possible discordance reductions.</li>
 * </ol>
 *
 * <p>The final decision is derived from an outranking matrix: the candidate whose row sum is
 * highest (i.e. it outranks more alternatives than it is outranked by) is returned as the best
 * choice.
 *
 * <p>Example usage:
 * <pre>{@code
 * Electre electre = new Electre(List.of("cost", "distance", "safety"));
 * electre.getPoids().put("cost",     10.0);
 * electre.getPoids().put("distance",  5.0);
 * electre.getPoids().put("safety",    8.0);
 * electre.getPreference().put("cost",   0.2);
 * electre.getVeto().put("cost",         0.6);
 * Candidate best = electre.decision(candidates);
 * }</pre>
 *
 * @see Candidate
 */
public class Electre {

	/**
	 * Per-criterion weights used to compute the weighted concordance index. A higher weight gives
	 * more influence to a criterion when aggregating concordances across all criteria. Keys are
	 * criterion names and must match those in each {@link Candidate}'s value map. Defaults to
	 * {@code 5.0} for every criterion passed to the constructor.
	 */
	private Map<String, Double> poids = new HashMap<>();

	/**
	 * The credibility threshold (also called the "cutting level") used to determine whether an
	 * outranking relation holds. A pair {@code (A1, A2)} is considered an outranking relation
	 * ({@code A1 outranks A2}) when the global credibility index of A1 over A2 is at least this
	 * value. Must be in {@code [0, 1]}; the default value is {@code 0.7}.
	 */
	private double seuilCoupe = 0.7;

	/**
	 * Per-criterion preference thresholds. The preference threshold {@code p} of a criterion
	 * defines the minimum performance difference above which a strict preference is declared for
	 * that criterion. Below the indifference threshold no preference is recorded; between
	 * indifference and preference a partial concordance is computed. Keys are criterion names.
	 * Defaults to {@code 0.3} for every criterion.
	 */
	private Map<String, Double> preference = new HashMap<>();

	/**
	 * Per-criterion indifference thresholds. The indifference threshold {@code q} of a criterion
	 * defines the maximum performance difference below which the two alternatives are considered
	 * indifferent on that criterion (concordance index = 1). Keys are criterion names. Defaults to
	 * {@code 0.1} for every criterion.
	 */
	private Map<String, Double> indifference = new HashMap<>();

	/**
	 * Per-criterion veto thresholds. When the performance of A1 falls below that of A2 by more
	 * than the veto threshold on a given criterion, the discordance index for that criterion
	 * reaches its maximum of 1, potentially blocking the outranking of A1 over A2 entirely. A
	 * value of {@code 0} (the default) effectively disables the veto mechanism for a criterion.
	 * Keys are criterion names.
	 */
	private Map<String, Double> veto = new HashMap<>();

	/**
	 * The list of criterion names in the order they were originally provided. Maintained to ensure
	 * a consistent iteration order when serializing the configuration via {@link #toString()}.
	 */
	private final List<String> critereOrdonnes;

	/**
	 * Constructs an {@code Electre} instance for the given set of criteria. Each criterion is
	 * initialised with default parameter values: weight = {@code 5.0}, preference threshold =
	 * {@code 0.3}, indifference threshold = {@code 0.1}, and veto threshold = {@code 0.0}.
	 *
	 * @param critereOrdonnes
	 *            the ordered list of criterion names; each name must later appear as a key in
	 *            every {@link Candidate}'s criterion-value map
	 */
	public Electre(final List<String> critereOrdonnes) {
		super();
		this.critereOrdonnes = critereOrdonnes;
		for (final String param : critereOrdonnes) {
			poids.put(param, 5d);
			preference.put(param, 0.3);
			indifference.put(param, 0.1);
			veto.put(param, 0d);
		}
	}

	// TODO UCdetector: Remove unused code:
	// public Electre(final Electre electre) {
	// ...existing code...
	// }

	/**
	 * Returns the per-criterion weight map. The returned map is mutable and changes are reflected
	 * in the ELECTRE computation.
	 *
	 * @return a mutable map from criterion name to its weight; never {@code null}
	 */
	public Map<String, Double> getPoids() {
		return poids;
	}

	/**
	 * Replaces the per-criterion weight map and rebuilds the ordered criterion list accordingly.
	 * After this call {@link #getCritereOrdonnes()} returns the keys of {@code poids} in sorted
	 * order.
	 *
	 * @param poids
	 *            the new map from criterion name to weight; must not be {@code null}
	 */
	public void setPoids(final Map<String, Double> poids) {
		this.poids = poids;
		critereOrdonnes.clear();
		critereOrdonnes.addAll(poids.keySet());
		Collections.sort(critereOrdonnes);
	}

	/**
	 * Selects the best {@link Candidate} from the given list using the ELECTRE III outranking
	 * procedure. For each ordered pair of candidates the pairwise outranking relation is
	 * determined and encoded in a relation matrix. The candidate whose net outranking score
	 * (number of candidates it outranks minus number of candidates it is outranked by) is highest
	 * is returned.
	 *
	 * @param locations
	 *            the list of candidates to evaluate; must not be {@code null} or empty
	 * @return the candidate with the highest net outranking score, or {@code null} if
	 *         {@code locations} is empty
	 */
	public Candidate decision(final List<Candidate> locations) {
		final int relation[][] = new int[locations.size()][locations.size()];

		for (int i = 0; i < locations.size() - 1; i++) {
			final Candidate act1 = locations.get(i);
			for (int j = i + 1; j < locations.size(); j++) {
				final Candidate act2 = locations.get(j);

				relation[i][j] = 0;
				relation[j][i] = 0;

				final String relationPaire = relation(act1, act2);
				if ("A1_P_A2".equals(relationPaire)) {
					relation[i][j] = 1;
					relation[j][i] = -1;
				} else if ("A2_P_A1".equals(relationPaire)) {
					relation[i][j] = -1;
					relation[j][i] = 1;
				}
			}
		}

		int max = -999999;
		Candidate candMax = null;
		for (int i = 0; i < locations.size(); i++) {
			int val = 0;
			for (int j = 0; j < locations.size(); j++) {
				val += relation[i][j];
			}
			if (val > max) {
				max = val;
				candMax = locations.get(i);
			}
		}
		return candMax;
	}

	/**
	 * Computes the concordance index for the hypothesis "alternative {@code a1} is at least as
	 * good as alternative {@code a2}" on the given criterion. The index is:
	 * <ul>
	 *   <li>{@code 1} if the performance difference {@code a1 - a2 > -q} (within indifference)</li>
	 *   <li>{@code (diff + p) / (p - q)} if {@code -p < diff ≤ -q} (partial concordance)</li>
	 *   <li>{@code 0} otherwise (a1 is clearly worse than a2 on this criterion)</li>
	 * </ul>
	 * where {@code q} is the indifference threshold and {@code p} the preference threshold for
	 * the given criterion.
	 *
	 * @param a1
	 *            the performance value of the first alternative on the criterion
	 * @param a2
	 *            the performance value of the second alternative on the criterion
	 * @param crit
	 *            the name of the criterion; must be a key in {@link #preference} and
	 *            {@link #indifference}
	 * @return the concordance index in {@code [0, 1]}
	 */
	private double concordance(final double a1, final double a2, final String crit) {
		double concordance = 0;
		final double prefCrit = preference.get(crit).doubleValue();
		final double indifCrit = indifference.get(crit).doubleValue();
		final double diff = a1 - a2;
		if (diff > -indifCrit) {
			concordance = 1;
		} else if (diff > -prefCrit) {
			concordance = (diff + prefCrit) / (prefCrit - indifCrit);
		}
		return concordance;
	}

	/**
	 * Computes the discordance index for the hypothesis "alternative {@code a1} outranks
	 * alternative {@code a2}" on the given criterion. A high discordance on any criterion can
	 * veto the outranking relation entirely. The index is:
	 * <ul>
	 *   <li>{@code 1} if the performance difference {@code a1 - a2 < -v} (full veto)</li>
	 *   <li>{@code (diff + p) / (p - v)} if {@code -v ≤ diff < -p} (partial discordance)</li>
	 *   <li>{@code 0} otherwise</li>
	 * </ul>
	 * where {@code p} is the preference threshold and {@code v} the veto threshold for the given
	 * criterion.
	 *
	 * @param a1
	 *            the performance value of the first alternative on the criterion
	 * @param a2
	 *            the performance value of the second alternative on the criterion
	 * @param crit
	 *            the name of the criterion; must be a key in {@link #preference} and {@link #veto}
	 * @return the discordance index in {@code [0, 1]}
	 */
	private double discordance(final double a1, final double a2, final String crit) {
		final double prefCrit = preference.get(crit).doubleValue();
		final double vetoCrit = veto.get(crit).doubleValue();
		final double diff = a1 - a2;
		double discordance = 0;
		if (diff < -vetoCrit) {
			discordance = 1;
		} else if (diff < -prefCrit) {
			discordance += (diff + prefCrit) / (prefCrit - vetoCrit);
		}
		return discordance;
	}

	/**
	 * Determines the outranking relation between two candidates by computing their global
	 * credibility indices and comparing them against the cutting threshold {@link #seuilCoupe}.
	 * Returns one of the following string tokens:
	 * <ul>
	 *   <li>{@code "A1_P_A2"}: {@code val1} strictly outranks {@code val2}</li>
	 *   <li>{@code "A2_P_A1"}: {@code val2} strictly outranks {@code val1}</li>
	 *   <li>{@code "A1_I_A2"}: both candidates outrank each other (indifference)</li>
	 *   <li>{@code "A1_R_A2"}: neither candidate outranks the other (incomparability)</li>
	 * </ul>
	 *
	 * @param val1
	 *            the first candidate
	 * @param val2
	 *            the second candidate
	 * @return a string token encoding the outranking relation between {@code val1} and {@code val2}
	 */
	private String relation(final Candidate val1, final Candidate val2) {
		// On commence par calculer pour chaque critère les concordances et les discordances entre
		// le vecteur courant et le vecteur de ref
		double concordGA1A2 = 0;
		double concordGA2A1 = 0;

		double poidsTot = 0;
		for (final String crit : poids.keySet()) {
			final double poidsCrit = poids.get(crit).doubleValue();
			final double a1 = val1.getValCriteria().get(crit).doubleValue();
			final double a2 = val2.getValCriteria().get(crit).doubleValue();
			poidsTot += poidsCrit;
			concordGA1A2 += poidsCrit * concordance(a1, a2, crit);
			concordGA2A1 += poidsCrit * concordance(a2, a1, crit);

		}
		concordGA1A2 /= poidsTot;
		concordGA2A1 /= poidsTot;

		double TA1A2 = 1;
		double TA2A1 = 1;
		for (final String crit : poids.keySet()) {
			final double a1 = val1.getValCriteria().get(crit).doubleValue();
			final double a2 = val2.getValCriteria().get(crit).doubleValue();

			final double discordanceA1A2 = discordance(a1, a2, crit);
			final double discordanceA2A1 = discordance(a2, a1, crit);

			if (discordanceA1A2 > concordGA1A2) {
				TA1A2 *= (1 - discordanceA1A2) / (1 - concordGA1A2);
			}
			if (discordanceA2A1 > concordGA2A1) {
				TA2A1 *= (1 - discordanceA2A1) / (1 - concordGA2A1);
			}
		}

		final double credibiliteGlobaleA1A2 = concordGA1A2 * TA1A2;
		final double credibiliteGlobaleA2A1 = concordGA2A1 * TA2A1;

		// on déduit enfin de ces valeurs la relation existante entre les deux vecteurs de valeurs
		if (credibiliteGlobaleA1A2 < seuilCoupe) {
			if (credibiliteGlobaleA2A1 < seuilCoupe) { return "A1_R_A2"; }
			return "A2_P_A1";
		}
		if (credibiliteGlobaleA2A1 < seuilCoupe) { return "A1_P_A2"; }
		return "A1_I_A2";
	}

	/**
	 * Returns a compact string representation of this ELECTRE configuration, consisting of the
	 * cutting threshold followed by a comma-separated list of criterion parameters in the form
	 * {@code name:weight,preference,indifference,veto}.
	 *
	 * @return the string representation of this ELECTRE configuration
	 */
	@Override
	public String toString() {
		String str = this.seuilCoupe + ",";
		for (final String crit : critereOrdonnes) {
			str += crit + ":" + poids.get(crit) + "," + preference.get(crit) + "," + indifference.get(crit) + ","
					+ veto.get(crit);
		}
		return str;
	}

	/**
	 * Returns the credibility cutting threshold that controls when an outranking relation holds.
	 *
	 * @return the cutting threshold in {@code [0, 1]}; default is {@code 0.7}
	 */
	public double getSeuilCoupe() {
		return seuilCoupe;
	}

	/**
	 * Sets the credibility cutting threshold.
	 *
	 * @param seuilCoupe
	 *            the new cutting threshold; must be in {@code [0, 1]}; a higher value makes the
	 *            outranking relation harder to achieve
	 */
	public void setSeuilCoupe(final double seuilCoupe) {
		this.seuilCoupe = seuilCoupe;
	}

	/**
	 * Returns the per-criterion preference threshold map. The returned map is mutable.
	 *
	 * @return a mutable map from criterion name to its preference threshold; never {@code null}
	 */
	public Map<String, Double> getPreference() {
		return preference;
	}

	/**
	 * Replaces the per-criterion preference threshold map.
	 *
	 * @param preference
	 *            the new map from criterion name to preference threshold; must not be {@code null}
	 */
	public void setPreference(final Map<String, Double> preference) {
		this.preference = preference;
	}

	/**
	 * Returns the per-criterion indifference threshold map. The returned map is mutable.
	 *
	 * @return a mutable map from criterion name to its indifference threshold; never {@code null}
	 */
	public Map<String, Double> getIndifference() {
		return indifference;
	}

	/**
	 * Replaces the per-criterion indifference threshold map.
	 *
	 * @param indifference
	 *            the new map from criterion name to indifference threshold; must not be {@code null}
	 */
	public void setIndifference(final Map<String, Double> indifference) {
		this.indifference = indifference;
	}

	/**
	 * Returns the per-criterion veto threshold map. The returned map is mutable.
	 *
	 * @return a mutable map from criterion name to its veto threshold; never {@code null}
	 */
	public Map<String, Double> getVeto() {
		return veto;
	}

	/**
	 * Replaces the per-criterion veto threshold map.
	 *
	 * @param veto
	 *            the new map from criterion name to veto threshold; must not be {@code null}
	 */
	public void setVeto(final Map<String, Double> veto) {
		this.veto = veto;
	}

	/**
	 * Returns the ordered list of criterion names as supplied at construction time (or last
	 * updated by {@link #setPoids(Map)}).
	 *
	 * @return the ordered list of criterion names; never {@code null}
	 */
	public List<String> getCritereOrdonnes() {
		return critereOrdonnes;
	}

}
