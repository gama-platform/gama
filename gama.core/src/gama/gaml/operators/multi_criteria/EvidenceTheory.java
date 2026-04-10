/*******************************************************************************************************
 *
 * EvidenceTheory.java, in gama.core, is part of the source code of the
 * GAMA modeling and simulation platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.gaml.operators.multi_criteria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the <b>Dempster–Shafer Evidence Theory</b> (also called the theory of belief
 * functions) for multi-criteria decision making. Given a set of {@link Candidate} objects and a
 * list of {@link CritereFonctionsCroyances} criteria, this class fuses the basic belief
 * assignments (BBAs) produced by each criterion across all candidates and selects the best
 * alternative by maximising the <em>pignistic probability</em>.
 *
 * <p>The algorithm proceeds in three main phases:
 * <ol>
 *   <li><b>Local fusion per candidate</b>: for each candidate, the BBAs produced by every
 *       criterion are combined using the Dempster combination rule ({@code fusionLesMassesLocales})
 *       to yield a single aggregated BBA for that candidate.</li>
 *   <li><b>Hypothesis fusion</b>: the aggregated BBAs of all candidates are combined across
 *       the hypothesis space, building a {@code Propositions} object that holds a disjunctive
 *       set of propositions with their associated belief masses.</li>
 *   <li><b>Candidate selection</b>: the pignistic probability (BetP) is computed for each
 *       candidate from the fused propositions and the candidate with the highest BetP is
 *       returned as the best choice.</li>
 * </ol>
 *
 * <p>Two modes are available via the {@code simple} flag of {@link #decision}:
 * <ul>
 *   <li><b>Simple mode</b>: each candidate is treated independently; the BBAs are not fused
 *       across candidates (faster but less accurate for large sets).</li>
 *   <li><b>Full mode</b>: full Dempster combination across all candidates (default behaviour).</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * LinkedList<CritereFonctionsCroyances> criteria = new LinkedList<>();
 * criteria.add(new CritereFctCroyancesBasique("cost", 0.0, 0.1, 0.8, 0.1, 0.8, 1.0));
 * criteria.add(new CritereFctCroyancesBasique("quality", 0.0, 0.8, 0.1, 0.8, 0.1, 1.0));
 *
 * LinkedList<Candidate> candidates = new LinkedList<>();
 * // ... populate candidates ...
 *
 * EvidenceTheory et = new EvidenceTheory();
 * Candidate best = et.decision(criteria, candidates, false);
 * }</pre>
 *
 * @author PTaillandier
 * @see CritereFonctionsCroyances
 * @see CritereFctCroyancesBasique
 * @see Candidate
 */
public class EvidenceTheory {

	/**
	 * Combines the BBAs of two independent evidence sources using Dempster's combination rule.
	 * The resulting masses are computed as:
	 * <ul>
	 *   <li>{@code pour12 = m1.pour*m2.pour + m1.pour*m2.ign + m1.ign*m2.pour}</li>
	 *   <li>{@code contre12 = m1.contre*m2.contre + m1.contre*m2.ign + m1.ign*m2.contre}</li>
	 *   <li>{@code ign12 = m1.ign * m2.ign}</li>
	 *   <li>{@code conflit12 = m1.pour*m2.contre + m1.contre*m2.pour + ...}</li>
	 * </ul>
	 *
	 * @param m1
	 *            the BBA of the first evidence source; must not be {@code null}
	 * @param m2
	 *            the BBA of the second evidence source; must not be {@code null}
	 * @return a new {@code MassesCroyances} representing the Dempster combination of {@code m1}
	 *         and {@code m2}
	 */
	private MassesCroyances fusionLesMassesLocalesDeuxSources(final MassesCroyances m1, final MassesCroyances m2) {
		// on instancie de nouvelles masses de croyance correspondant à la fusion de m1 et m2
		final MassesCroyances fusion = new MassesCroyances();
		// on met à jour les valeurs de ces masses de croyances
		fusion.setPour(m1.pour * m2.pour + m1.pour * m2.ignorance + m1.ignorance * m2.pour);
		fusion.setContre(m1.contre * m2.contre + m1.contre * m2.ignorance + m1.ignorance * m2.contre);
		fusion.setIgnorance(m1.ignorance * m2.ignorance);
		fusion.setConflit(m1.pour * m2.contre + m1.contre * m2.pour
				+ m1.conflit * (m2.pour + m2.contre + m2.ignorance + m2.conflit)
				+ m2.conflit * (m1.pour + m1.contre + m1.ignorance));
		return fusion;
	}

	/**
	 * Sequentially combines a list of BBAs by applying the two-source Dempster rule
	 * ({@link #fusionLesMassesLocalesDeuxSources}) pairwise from left to right. The result is the
	 * Dempster combination of all sources in the list.
	 *
	 * @param masses
	 *            a non-empty linked list of {@code MassesCroyances} to fuse; must not be
	 *            {@code null}
	 * @return the combined {@code MassesCroyances}, or {@code null} if {@code masses} is empty
	 */
	private MassesCroyances fusionLesMassesLocales(final LinkedList<MassesCroyances> masses) {
		MassesCroyances fusion = null;
		// Parcours de l'ensemble des masses de croyances
		for (final MassesCroyances mc : masses) {
			// si ce n'est pas la première MassesCroyances de l'ensemble, on la fusionne avec les
			// précédentes
			if (fusion != null) {
				fusion = fusionLesMassesLocalesDeuxSources(fusion, mc);
			} else {
				fusion = mc;
			}
		}
		return fusion;
	}

	/**
	 * Builds the full proposition space by fusing the per-candidate BBAs using Dempster's rule
	 * across the candidate hypothesis space.
	 *
	 * @param candidats
	 *            a map from each {@link Candidate} (hypothesis) to its aggregated
	 *            {@code MassesCroyances}; must not be {@code null}
	 * @return a {@code Propositions} object containing the fused propositions and their belief
	 *         masses
	 */
	private Propositions fusionHypotheses(final Map<Candidate, MassesCroyances> candidats) {
		final Propositions fusion = new Propositions(candidats);
		return fusion;
	}

	/**
	 * Builds the proposition space in simple mode, where each candidate's BBA is treated as an
	 * independent, non-fused proposition without cross-candidate combination.
	 *
	 * @param candidats
	 *            a map from each {@link Candidate} to its aggregated {@code MassesCroyances};
	 *            must not be {@code null}
	 * @return a {@code Propositions} object built from the individual candidate BBAs without
	 *         cross-candidate Dempster fusion
	 */
	private Propositions buildPropositions(final Map<Candidate, MassesCroyances> candidats) {
		return new Propositions(candidats, true);
	}

	/**
	 * Fuses the BBAs produced by all criteria for a single candidate into one aggregated BBA. For
	 * each criterion the three masses ({@code pour}, {@code contre}, {@code ignorance}) are read
	 * from the criterion using the candidate's criterion value, assembled into a
	 * {@code MassesCroyances}, and then all criteria masses are combined sequentially with
	 * Dempster's rule.
	 *
	 * @param criteres
	 *            the ordered list of criteria whose BBAs are to be combined; must not be
	 *            {@code null}
	 * @param valeursCourantes
	 *            the map from criterion name to numeric value for the current candidate; must
	 *            contain an entry for every criterion in {@code criteres}
	 * @return the aggregated {@code MassesCroyances} for this candidate after fusing all criteria
	 */
	private MassesCroyances fusionCriteres(final LinkedList<CritereFonctionsCroyances> criteres,
			final Map<String, Double> valeursCourantes) {
		final LinkedList<MassesCroyances> masses = new LinkedList<>();
		// Parcours des critères
		for (final CritereFonctionsCroyances cfc : criteres) {
			final double valC = valeursCourantes.get(cfc.getNom()).doubleValue();

			// Pour chaque critère, on instancie à un objet MassesCroyances. Initialisation des
			// masses de croyances
			final MassesCroyances mc = new MassesCroyances(cfc.masseCroyancePour(valC), cfc.masseCroyanceContre(valC),
					cfc.masseCroyanceIgnorance(valC), 0);
			masses.add(mc);
		}
		// On renvoie les masses de croyances obtenues après fusion des critères
		return fusionLesMassesLocales(masses);
	}

	/**
	 * Selects the best {@link Candidate} from the supplied list by applying Evidence Theory
	 * multi-criteria decision making. First, per-candidate BBAs are computed by fusing all
	 * criteria masses ({@link #fusionCriteres}). Then the propositions are built (either in simple
	 * or full hypothesis-fusion mode). Finally, the candidate that maximises the pignistic
	 * probability is returned.
	 *
	 * @param criteres
	 *            the list of criteria with their belief functions; must not be {@code null}
	 * @param cands
	 *            the list of candidates to evaluate; must not be {@code null} or empty
	 * @param simple
	 *            {@code true} to use the simplified (independent-BBAs) proposition building mode;
	 *            {@code false} to perform full Dempster cross-candidate fusion
	 * @return the candidate with the highest pignistic probability, or {@code null} if
	 *         {@code cands} is empty
	 */
	public Candidate decision(final LinkedList<CritereFonctionsCroyances> criteres, final LinkedList<Candidate> cands,
			final boolean simple) {
		// Parcours de l'ensemble des candidats possibles
		final Map<Candidate, MassesCroyances> candidats = new HashMap<>();
		for (final Candidate cand : cands) {
			// on calcul pour chaque candidat, les masses de croyances obtenues après fusion des
			// critères
			candidats.put(cand, fusionCriteres(criteres, cand.getValCriteria()));
		}

		// on fusionne entre elles les hypothèses
		Propositions propositions = null;
		if (simple) {
			propositions = buildPropositions(candidats);
		} else {
			propositions = fusionHypotheses(candidats);
		}

		// on choisit le meilleur candidat (le meilleur intervalle) : celui qui maximise la
		// probabilité pignistique
		return choixCandidat(propositions, cands);
	}

	/**
	 * Iterates over all candidates and returns the one with the highest pignistic probability
	 * ({@link #probaPignistic}). In case of a tie the first maximising candidate in iteration
	 * order is returned.
	 *
	 * @param propositions
	 *            the set of fused propositions with their belief masses; must not be {@code null}
	 * @param candidates
	 *            the ordered list of candidates to compare; must not be {@code null}
	 * @return the candidate with the highest pignistic probability, or {@code null} if
	 *         {@code candidates} is empty
	 */
	private Candidate choixCandidat(final Propositions propositions, final LinkedList<Candidate> candidates) {
		// Parcours de l'ensemble des candidats
		Candidate bestCand = null;
		double probaMax = -1;
		for (final Candidate cand : candidates) {
			// On calcul pour chacun la probabilité pignistique que ce candidat soit le bon
			final double proba = probaPignistic(cand, propositions);
			if (proba > probaMax) {
				probaMax = proba;
				bestCand = cand;
			}
		}
		// on renvoit le candidat qui maximise la probabilité pignistic
		return bestCand;
	}

	/**
	 * Computes the pignistic probability (BetP) that the given candidate is the best choice,
	 * based on the fused propositions. The pignistic transformation distributes each proposition's
	 * belief mass equally among the hypotheses (candidates) it contains, and then normalises by
	 * the conflict coefficient:
	 * <pre>{@code
	 * BetP(cand) = coeffNorm * Σ { m(A) / |A| : cand ∈ A }
	 * }</pre>
	 * where {@code coeffNorm = 1 / (1 - m(∅))} handles the conflict mass.
	 *
	 * @param cand
	 *            the candidate for which the pignistic probability is computed; must not be
	 *            {@code null}
	 * @param propositions
	 *            the set of fused propositions; must not be {@code null}
	 * @return the pignistic probability that {@code cand} is the best choice, in {@code [0, 1]}
	 */
	private double probaPignistic(final Candidate cand, final Propositions propositions) {
		// on calcul le coefficient de normalisation
		final double coeffNorm = propositions.getCoeffNorm();
		// On parcours la liste des propositions
		double proba = 0;
		for (final Proposition prop : propositions.propositions) {
			// Si la propositions dit que ce candidat peut être le bon, on la prend on compte dans
			// le calcul de la proba pignistic
			if (prop.getHypothese().contains(cand)) {
				proba += prop.getMasseCroyance() / prop.getHypothese().size();
			}
		}
		// on normalise la proba
		return proba * coeffNorm;
	}

	// -----------------------------------------------------------------------------------------
	// Inner classes
	// -----------------------------------------------------------------------------------------

	/**
	 * Holds the four basic belief assignment (BBA) masses for a single source (criterion or
	 * aggregation of criteria) in the Dempster–Shafer framework:
	 * <ul>
	 *   <li>{@code pour} ("for"): belief that the candidate <em>is</em> the best choice.</li>
	 *   <li>{@code contre} ("against"): belief that the candidate is <em>not</em> the best
	 *       choice.</li>
	 *   <li>{@code ignorance}: residual uncertainty uncommitted to either hypothesis.</li>
	 *   <li>{@code conflit}: mass arising from contradictions between sources during Dempster
	 *       combination; used for the normalisation coefficient.</li>
	 * </ul>
	 * The four values should sum to {@code 1} after a complete combination step.
	 *
	 * @author PTaillandier
	 */
	private class MassesCroyances {

		/**
		 * Belief mass representing the evidence in favour of the hypothesis "this candidate is the
		 * best choice". Must be in {@code [0, 1]}.
		 */
		double pour;

		/**
		 * Belief mass representing the evidence against the hypothesis "this candidate is the best
		 * choice" (equivalently, evidence that some other candidate is better). Must be in
		 * {@code [0, 1]}.
		 */
		double contre;

		/**
		 * Belief mass representing the uncertainty: the source does not know whether the candidate
		 * is the best or not. Must be in {@code [0, 1]}.
		 */
		double ignorance;

		/**
		 * Belief mass representing the conflict accumulated during Dempster combination when two
		 * sources assign mass to mutually exclusive propositions. Must be in {@code [0, 1]}.
		 */
		double conflit;

		/**
		 * Constructs a fully initialised {@code MassesCroyances} with the four specified masses.
		 *
		 * @param pour
		 *            the "for" belief mass; must be in {@code [0, 1]}
		 * @param contre
		 *            the "against" belief mass; must be in {@code [0, 1]}
		 * @param ignorance
		 *            the ignorance mass; must be in {@code [0, 1]}
		 * @param conflit
		 *            the conflict mass arising from Dempster combination; must be in {@code [0, 1]}
		 */
		public MassesCroyances(final double pour, final double contre, final double ignorance, final double conflit) {
			super();
			this.pour = pour;
			this.contre = contre;
			this.ignorance = ignorance;
			this.conflit = conflit;
		}

		/**
		 * Constructs a {@code MassesCroyances} with all masses initialised to zero. Fields should
		 * be set via the individual setters before use.
		 */
		public MassesCroyances() {}

		/**
		 * Returns a string representation showing the four mass values.
		 *
		 * @return a human-readable description of this BBA
		 */
		@Override
		public String toString() {
			return "pour : " + pour + " - contre : " + contre + " - ignorance : " + ignorance + " - conflit : "
					+ conflit;
		}

		/**
		 * Sets the "against" belief mass.
		 *
		 * @param contre
		 *            the new "against" mass; must be in {@code [0, 1]}
		 */
		public void setContre(final double contre) {
			this.contre = contre;
		}

		/**
		 * Sets the ignorance belief mass.
		 *
		 * @param ignorance
		 *            the new ignorance mass; must be in {@code [0, 1]}
		 */
		public void setIgnorance(final double ignorance) {
			this.ignorance = ignorance;
		}

		/**
		 * Sets the "for" belief mass.
		 *
		 * @param pour
		 *            the new "for" mass; must be in {@code [0, 1]}
		 */
		public void setPour(final double pour) {
			this.pour = pour;
		}

		/**
		 * Sets the conflict belief mass.
		 *
		 * @param conflit
		 *            the new conflict mass; must be in {@code [0, 1]}
		 */
		public void setConflit(final double conflit) {
			this.conflit = conflit;
		}
	}

	/**
	 * Represents a single disjunctive <em>proposition</em> in the Dempster–Shafer frame of
	 * discernment. A proposition is characterised by:
	 * <ul>
	 *   <li>A <em>hypothesis set</em> ({@code hypothese}): the subset of {@link Candidate}
	 *       objects that are considered compatible with this proposition.</li>
	 *   <li>A <em>belief mass</em> ({@code masseCroyance}): the amount of evidence committed to
	 *       this proposition; the empty set carries the conflict mass.</li>
	 *   <li>A stable numeric identifier ({@code id}) derived from the sorted indices of the
	 *       candidates in the hypothesis set, used for fast equality checks and map look-ups.</li>
	 * </ul>
	 *
	 * @author PTaillandier
	 */
	private class Proposition {

		/**
		 * The set of candidates (hypotheses) that are asserted to potentially be the best choice
		 * by this proposition. An empty list represents the conflict proposition (empty set in
		 * the frame of discernment).
		 */
		final LinkedList<Candidate> hypothese;

		/**
		 * The basic belief mass assigned to this proposition, in {@code [0, 1]}. It is
		 * accumulated across fusions when multiple propositions with the same {@code id} are merged
		 * by {@link Propositions#ajouteProposition}.
		 */
		double masseCroyance;

		/**
		 * A stable integer identifier for this proposition, computed as the hash code of the
		 * sorted list of candidate indices in the hypothesis set. Used for fast equality checks
		 * and as a map key in {@link Propositions#ajouteProposition}.
		 */
		final int id;

		/**
		 * Constructs a {@code Proposition} with the given hypothesis set and initial belief mass.
		 * The {@link #id} is computed from the sorted indices of all candidates in
		 * {@code hypothese}.
		 *
		 * @param hypothese
		 *            the list of candidates that constitute the hypothesis; an empty list denotes
		 *            the conflict (empty-set) proposition
		 * @param masseCroyance
		 *            the initial belief mass assigned to this proposition; must be in {@code [0, 1]}
		 */
		public Proposition(final LinkedList<Candidate> hypothese, final double masseCroyance) {
			super();
			this.hypothese = hypothese;
			this.masseCroyance = masseCroyance;
			final List<Integer> cands = new ArrayList<>();
			for (final Candidate cand : hypothese) {
				cands.add(cand.getIndex());
			}
			Collections.sort(cands);
			id = cands.hashCode();
		}

		/**
		 * Returns a human-readable representation showing the hypothesis set and its belief mass.
		 *
		 * @return a string of the form {@code "[candidate, ...] : mass"}
		 */
		@Override
		public String toString() {
			return this.hypothese.toString() + " : " + masseCroyance;
		}

		/**
		 * Returns the belief mass currently assigned to this proposition.
		 *
		 * @return the belief mass in {@code [0, 1]}
		 */
		public double getMasseCroyance() {
			return masseCroyance;
		}

		/**
		 * Returns the list of candidates that constitute the hypothesis of this proposition.
		 *
		 * @return the hypothesis set; an empty list denotes the conflict proposition
		 */
		public LinkedList<Candidate> getHypothese() {
			return hypothese;
		}

		/**
		 * Computes a hash code based on the enclosing {@link EvidenceTheory} instance and the
		 * proposition's {@link #id}.
		 *
		 * @return the hash code for this proposition
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + id;
			return result;
		}

		/**
		 * Two propositions are equal when they belong to the same {@link EvidenceTheory} instance
		 * and have the same {@link #id} (i.e. the same set of candidate indices in their
		 * hypothesis).
		 *
		 * @param obj
		 *            the object to compare
		 * @return {@code true} if {@code obj} is a {@code Proposition} with the same outer
		 *         instance and the same {@code id}; {@code false} otherwise
		 */
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) { return true; }
			if (obj == null) { return false; }
			if (getClass() != obj.getClass()) { return false; }
			final Proposition other = (Proposition) obj;
			if (!getOuterType().equals(other.getOuterType())) { return false; }
			if (id != other.id) { return false; }
			return true;
		}

		/**
		 * Returns the enclosing {@link EvidenceTheory} instance. Required for correct
		 * {@link #hashCode()} and {@link #equals(Object)} behaviour of this non-static inner class.
		 *
		 * @return the enclosing {@code EvidenceTheory} instance
		 */
		private EvidenceTheory getOuterType() {
			return EvidenceTheory.this;
		}

	}

	/**
	 * Manages the complete set of {@link Proposition} objects that result from combining the
	 * per-candidate BBAs across the entire candidate hypothesis space. It also maintains the
	 * normalisation coefficient {@code coeffNorm = 1 / (1 - m(∅))} used in the pignistic
	 * probability transform to redistribute the conflict mass.
	 *
	 * <p>Two construction modes are provided:
	 * <ul>
	 *   <li>{@link #Propositions(Map)}: full Dempster cross-candidate fusion.</li>
	 *   <li>{@link #Propositions(Map, boolean)}: if {@code simple=true}, each candidate's BBA is
	 *       added independently without cross-fusion.</li>
	 * </ul>
	 *
	 * @author PTaillandier
	 */
	private class Propositions {

		/**
		 * The complete list of disjunctive propositions after combining all candidate BBAs. Each
		 * element carries a subset of candidates as its hypothesis and an associated belief mass.
		 * The empty-hypothesis proposition (conflict mass) is also stored here.
		 */
		LinkedList<Proposition> propositions;

		/**
		 * The normalisation coefficient used in the pignistic probability transform:
		 * {@code 1 / (1 - m(∅))}, where {@code m(∅)} is the conflict mass. Equals {@code 1} when
		 * there is no conflict.
		 */
		private double coeffNorm;

		/**
		 * Constructs a {@code Propositions} object by performing full Dempster cross-candidate
		 * fusion. All candidate BBAs are iteratively combined so that every disjunctive subset
		 * of candidates gets a fused belief mass.
		 *
		 * @param candidats
		 *            a map from each {@link Candidate} to its aggregated {@code MassesCroyances};
		 *            must not be {@code null}
		 */
		public Propositions(final Map<Candidate, MassesCroyances> candidats) {
			this.propositions = new LinkedList<>();
			coeffNorm = 1;
			init(candidats, false);
		}

		/**
		 * Constructs a {@code Propositions} object with a selectable combination mode.
		 *
		 * @param candidats
		 *            a map from each {@link Candidate} to its aggregated {@code MassesCroyances};
		 *            must not be {@code null}
		 * @param simple
		 *            when {@code true} each candidate's BBA is added independently (no
		 *            cross-candidate Dempster fusion); when {@code false} full cross-candidate
		 *            fusion is performed (equivalent to calling {@link #Propositions(Map)})
		 */
		public Propositions(final Map<Candidate, MassesCroyances> candidats, final boolean simple) {
			this.propositions = new LinkedList<>();
			coeffNorm = 1;
			init(candidats, simple);
		}

		/**
		 * Initialises the proposition set from the per-candidate BBAs. In simple mode every
		 * candidate is initialised independently via {@link #initPropositions}. In full mode the
		 * first candidate initialises the set and each subsequent candidate's propositions are
		 * fused with the existing set using {@link #fusionPropositions}.
		 *
		 * @param candidats
		 *            the map from candidate to its aggregated BBA; must not be {@code null}
		 * @param simple
		 *            {@code true} for independent initialisation, {@code false} for full fusion
		 */
		private void init(final Map<Candidate, MassesCroyances> candidats, final boolean simple) {
			for (final Candidate cand : candidats.keySet()) {
				final MassesCroyances mc1 = candidats.get(cand);
				if (simple) {
					initPropositions(cand, mc1, candidats);
				} else {
					// si c'est le premier candidat que l'on traite, on initialise les propositions avec
					// celui-ci
					if (propositions.isEmpty()) {
						initPropositions(cand, mc1, candidats);
					} else {
						// cas où des candidats ont déjà été traités -> dans ce cas fusion
						// des propositions précédemment obtenues avec ces nouvelles propositions

						// initialisation proposition pour : il faut apparier le vecteur de valeurs
						// courant avec cet intervalle
						final LinkedList<Candidate> pourSet = new LinkedList<>();
						pourSet.add(cand);
						final Proposition propp = new Proposition(pourSet, mc1.pour);

						// initialisation proposition contre : il ne faut pas apparier le vecteur de
						// valeurs courant avec cet intervalle
						final LinkedList<Candidate> contreSet = new LinkedList<>();
						final LinkedList<Candidate> ignoSet = new LinkedList<>();
						for (final Candidate cand2 : candidats.keySet()) {
							ignoSet.add(cand2);
							if (cand == cand2) {
								continue;
							}
							contreSet.add(cand2);
						}
						final Proposition propc = new Proposition(contreSet, mc1.contre);
						final Proposition propi = new Proposition(ignoSet, mc1.ignorance);

						// initialisation proposition conflit (deux critères qui donne des indications
						// contradictoires)
						final Proposition propConflit = new Proposition(new LinkedList<Candidate>(), mc1.conflit);

						final Map<Integer, Proposition> propositionsTmp = new HashMap<>();
						// on fusionne ces nouvelles propositions avec les propositions déjà
						// présentes dans l'ensemble propositions
						for (final Proposition prop : propositions) {
							final Proposition propFus1 = fusionPropositions(propp, prop);
							ajouteProposition(propositionsTmp, propFus1);
							final Proposition propFus2 = fusionPropositions(propc, prop);
							ajouteProposition(propositionsTmp, propFus2);
							final Proposition propFus3 = fusionPropositions(propi, prop);
							ajouteProposition(propositionsTmp, propFus3);
							final Proposition propFus4 = fusionPropositions(propConflit, prop);
							ajouteProposition(propositionsTmp, propFus4);
						}
						propositions = new LinkedList<>();
						propositions.addAll(propositionsTmp.values());
					}
				}
				computeCoeffNorm();
			}
		}

		/**
		 * Adds a proposition to the accumulation map, merging its belief mass with any
		 * existing proposition that has the same {@link Proposition#id}. If no such proposition
		 * exists yet the new one is inserted directly.
		 *
		 * @param propositionsTmp
		 *            the working map from proposition identifier to proposition object; must not
		 *            be {@code null}
		 * @param propFus
		 *            the proposition to add or merge; must not be {@code null}
		 */
		public void ajouteProposition(final Map<Integer, Proposition> propositionsTmp, final Proposition propFus) {
			// s'il y a déjà une proposition similaire (avec le même nom) dans le dictionnaire
			// propositionsTmp, on la récupère
			final Proposition propExiste = propositionsTmp.get(propFus.id);
			// si il n'y en a pas, on ajoute directement la nouvelle proposition
			if (propExiste == null) {
				propositionsTmp.put(propFus.id, propFus);
			} else {
				propExiste.masseCroyance += propFus.masseCroyance;
				propositionsTmp.put(propExiste.id, propExiste);
			}
		}

		/**
		 * Produces the Dempster combination of two propositions. The hypothesis set of the
		 * resulting proposition is the <em>intersection</em> of the two input hypothesis sets, and
		 * its belief mass is the product of the two input masses.
		 *
		 * @param prop1
		 *            the first proposition; must not be {@code null}
		 * @param prop2
		 *            the second proposition; must not be {@code null}
		 * @return a new {@code Proposition} whose hypothesis is the intersection of the two input
		 *         hypothesis sets and whose mass is {@code prop1.mass * prop2.mass}
		 */
		public Proposition fusionPropositions(final Proposition prop1, final Proposition prop2) {
			Proposition propFus = null;
			final LinkedList<Candidate> fusSet = new LinkedList<>();
			// La proposition obtenue après fusion a pour ensemble d'hypothèses, l'intersection
			// entre l'ensemble d'hypothèse de prop1 et de prop2
			for (final Candidate hyp : prop1.getHypothese()) {
				if (prop2.getHypothese().contains(hyp)) {
					fusSet.add(hyp);
				}
			}
			// On instancie cette nouvelle proposition avec la valeur de masse de croyance
			// correspondante
			propFus = new Proposition(fusSet, prop1.getMasseCroyance() * prop2.getMasseCroyance());
			return propFus;
		}

		/**
		 * Initialises the proposition list for the given candidate by creating four base
		 * propositions:
		 * <ol>
		 *   <li><b>pour</b>: hypothesis = {cand}, mass = {@code mc1.pour}</li>
		 *   <li><b>contre</b>: hypothesis = all other candidates, mass = {@code mc1.contre}</li>
		 *   <li><b>ignorance</b>: hypothesis = all candidates, mass = {@code mc1.ignorance}</li>
		 *   <li><b>conflit</b>: hypothesis = {} (empty), mass = {@code mc1.conflit}</li>
		 * </ol>
		 * The four propositions are added directly to {@link #propositions}.
		 *
		 * @param cand
		 *            the candidate being initialised as a hypothesis
		 * @param mc1
		 *            the aggregated BBA for {@code cand}
		 * @param candidats
		 *            the full candidate map, used to build the "contre" and "ignorance" hypothesis
		 *            sets; must not be {@code null}
		 */
		public void initPropositions(final Candidate cand, final MassesCroyances mc1,
				final Map<Candidate, MassesCroyances> candidats) {
			// initialisation proposition pour : ce candidat est le meilleur
			final LinkedList<Candidate> pourSet = new LinkedList<>();
			pourSet.add(cand);

			final Proposition propp = new Proposition(pourSet, mc1.pour);
			propositions.add(propp);

			// initialisation proposition contre : ce candidat n est pas le meilleur
			// équivalent à proportion : l'un des autres candidats est meilleur
			// initialisation proposition ignorance : l'un des candidats est le meilleur
			final LinkedList<Candidate> contreSet = new LinkedList<>();
			final LinkedList<Candidate> ignoSet = new LinkedList<>();
			for (final Candidate c : candidats.keySet()) {
				ignoSet.add(c);
				if (c != cand) {
					contreSet.add(c);
				}
			}

			final Proposition propc = new Proposition(contreSet, mc1.contre);
			propositions.add(propc);
			final Proposition propi = new Proposition(ignoSet, mc1.ignorance);
			propositions.add(propi);

			// initialisation proposition conflit (deux critères qui donne des indications
			// contradictoires)
			final Proposition propConflit = new Proposition(new LinkedList<Candidate>(), mc1.conflit);
			propositions.add(propConflit);
		}

		/**
		 * Scans the current proposition list to locate the conflict proposition (the one with an
		 * empty hypothesis set) and updates {@link #coeffNorm} accordingly:
		 * {@code coeffNorm = 1 / (1 - m(∅))} when {@code m(∅) < 1}, or {@code 1} when
		 * {@code m(∅) == 1} (total conflict).
		 */
		public void computeCoeffNorm() {
			for (final Proposition prop : propositions) {
				if (prop.hypothese.isEmpty()) {
					coeffNorm = 1.0 == prop.masseCroyance ? 1.0 : 1.0 / (1.0 - prop.masseCroyance);
					return;
				}
			}
		}

		/**
		 * Returns the normalisation coefficient used in the pignistic probability transform.
		 *
		 * @return {@code 1 / (1 - m(∅))}, or {@code 1} when the conflict mass is {@code 1}
		 */
		public double getCoeffNorm() {
			return coeffNorm;
		}

		/**
		 * Returns a human-readable representation of all propositions.
		 *
		 * @return a string of the form {@code "propositions : [prop1, prop2, ...]"}
		 */
		@Override
		public String toString() {
			return "propositions : " + propositions;
		}

	}

}
